package iteration2;

import generators.RandomData;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.LoginUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DepositMoney {

    private static final Random RANDOM = new Random();
    private static final double MAX_DEPOSIT_LIMIT = 5000.0;
    private static final double MIN_DEPOSIT_AMOUNT = 0.01;

    private String authToken;
    private Integer accountId;
    private String testUsername;
    private String testPassword;

    @BeforeEach
    public void setupUserAndAccount() {
        // 1. Генерируем учетные данные
        testUsername = RandomData.getUsername();
        testPassword = RandomData.getPassword();

        // 2. Создаем пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // 3. Логинимся и получаем токен
        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .build();

        authToken = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(loginRequest)  // ← ПРАВИЛЬНО: передаем loginRequest, а не userRequest
                .extract()
                .header(ResponseSpecs.AUTHORIZATION_HEADER);

        // 4. Создаем счет
        accountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .path("id");
    }

    // ================= POSITIVE TESTS (валидные суммы) =================

    @ParameterizedTest(name = "Customer can deposit valid amount: {0}")
    @CsvSource({
            "0.01",      // минимальный депозит
            "100.50",    // обычная сумма
            "2500.75",   // средняя сумма
            "4999.99",   // почти максимум
            "5000.0"     // максимальный депозит
    })
    public void customerCanDepositValidAmounts(double depositAmount) {
        // Округляем до 2 знаков
        depositAmount = Math.round(depositAmount * 100.0) / 100.0;

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(depositRequest);

        // Проверяем, что баланс обновился
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + accountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo((float) depositAmount));
    }

    @Test
    public void customerCanDepositRandomPositiveAmount() {
        // Генерация случайной суммы в пределах лимита
        double depositAmount = MIN_DEPOSIT_AMOUNT +
                RANDOM.nextDouble() * (MAX_DEPOSIT_LIMIT - MIN_DEPOSIT_AMOUNT);
        depositAmount = Math.round(depositAmount * 100.0) / 100.0;

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(depositAmount)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(depositRequest);

        // Проверяем баланс
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + accountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo((float) depositAmount));
    }

    // ================= NEGATIVE TESTS (невалидные суммы) =================

    @ParameterizedTest(name = "Cannot deposit negative or zero amount: {0}")
    @ValueSource(doubles = {0.0, -0.01, -100.0, -999.99})
    public void cannotDepositNegativeOrZeroAmount(double invalidAmount) {
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(invalidAmount)
                .build();

        // Ожидаем ошибку BAD_REQUEST
        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(depositRequest);

        // Проверяем что баланс не изменился (остался 0.0)
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + accountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo(0.0f));
    }

    @ParameterizedTest(name = "Cannot deposit above limit {0}")
    @ValueSource(doubles = {5000.01, 5000.1, 6000.0, 10000.0})
    public void cannotDepositAboveLimit(double amountAboveLimit) {
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(amountAboveLimit)
                .build();

        // Ожидаем ошибку BAD_REQUEST
        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(depositRequest);

        // Проверяем что баланс не изменился
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + accountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo(0.0f));
    }

    // ================= EDGE CASES =================

    @Test
    public void multipleDepositsUpdateBalanceCorrectly() {
        // Первый депозит
        double firstDeposit = 1000.0;
        DepositMoneyRequest firstRequest = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(firstDeposit)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(firstRequest);

        // Проверяем первый депозит
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + accountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo((float) firstDeposit));

        // Второй депозит
        double secondDeposit = 500.0;
        DepositMoneyRequest secondRequest = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(secondDeposit)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(secondRequest);

        // Проверяем суммарный баланс
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + accountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo((float) (firstDeposit + secondDeposit)));
    }

    @Test
    public void depositUpdatesBalanceCorrectlyWithRandomAmounts() {
        // Генерируем 3 случайных депозита
        double totalDeposited = 0.0;

        for (int i = 0; i < 3; i++) {
            double depositAmount = 10.0 + RANDOM.nextDouble() * 990.0; // 10-1000
            depositAmount = Math.round(depositAmount * 100.0) / 100.0;
            totalDeposited += depositAmount;

            // Проверяем что не превысим лимит
            if (totalDeposited > MAX_DEPOSIT_LIMIT) {
                break;
            }

            DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                    .id(accountId.longValue())
                    .balance(depositAmount)
                    .build();

            new DepositRequester(
                    RequestSpecs.authSpec(authToken),
                    ResponseSpecs.balanceWasUpdated())
                    .post(depositRequest);
        }

        // Проверяем итоговый баланс
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + accountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo((float) totalDeposited));
    }
}