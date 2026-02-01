package iteration2;

import generators.RandomData;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.LoginUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DepositMoney {
    private String authToken;
    private Integer accountId;

    @BeforeEach
    public void setupUserAndAccount() {
        // 1. Создаем пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // 2. Логинимся и получаем токен
        authToken = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(userRequest)
                .extract()
                .header(ResponseSpecs.AUTHORIZATION_HEADER);

        // 3. Создаем счет
        accountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .path("id");
    }

    @Test
    public void customerCanDepositMoneyToOwnAccount() {
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(1000.0)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(depositRequest);
    }

    @Test
    public void cannotDepositNegativeAmount() {
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(-100.0)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(depositRequest);
    }

    @Test
    public void cannotDepositZero() {
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(0.0)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(depositRequest);
    }

    @Test
    public void depositBoundaryValues() {
        // Минимальный депозит (0.01)
        DepositMoneyRequest minDeposit = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(0.01)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(minDeposit);

        // Максимальный депозит (5000.0)
        DepositMoneyRequest maxDeposit = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(5000.0)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(maxDeposit);

        // Превышение лимита (5000.01)
        DepositMoneyRequest exceedDeposit = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(5000.01)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(exceedDeposit);
    }

    @Test
    public void depositUpdatesBalanceCorrectly() {
        // Депозит
        Double depositAmount = 1500.0;
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(depositRequest);

        // Проверка баланса
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + accountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo(depositAmount));
    }
}