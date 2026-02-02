package iteration2;

import generators.RandomData;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.TransferMoneyRequest;
import models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.LoginUserRequester;
import requests.TransferRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;

public class TransferTest {

    // ================= TEST DATA =================
    private static final Random RANDOM = new Random();

    private String authToken;
    private Integer senderAccountId;
    private Integer receiverAccountId;
    private Double initialBalance;

    // ================= SETUP =================

    @BeforeEach
    public void setupUserAndAccounts() {
        // 1. Создаем пользователя через админа
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

        // 3. Создаем два счета
        senderAccountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .path("id");

        receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .path("id");

        // 4. Пополняем отправителя случайной суммой
        initialBalance = 500.0 + RANDOM.nextDouble() * 4500.0; // 500-5000
        initialBalance = Math.round(initialBalance * 100.0) / 100.0;

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(senderAccountId.longValue())
                .balance(initialBalance)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated())
                .post(depositRequest);
    }

    // ================= HELPER METHODS =================

    private void verifyBalances(Double expectedSenderBalance, Double expectedReceiverBalance) {
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + senderAccountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", closeTo(expectedSenderBalance.floatValue(), 0.001f));

        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/" + receiverAccountId)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", closeTo(expectedReceiverBalance.floatValue(), 0.001f));
    }

    private Double randomTransferAmount(Double maxAmount) {
        Double amount = 1.0 + RANDOM.nextDouble() * (maxAmount - 1.0);
        return Math.round(amount * 100.0) / 100.0;
    }

    // ================= POSITIVE TESTS =================

    @Test
    public void canTransferMoneyBetweenOwnAccounts() {
        // Подготовка: случайная сумма для перевода
        Double transferAmount = randomTransferAmount(initialBalance - 1.0);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(transferAmount)
                .build();

        // Выполнение перевода
        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.transferWasSuccessful())
                .post(transferRequest);

        // Проверка: балансы изменились корректно
        verifyBalances(
                initialBalance - transferAmount,
                transferAmount
        );
    }

    @Test
    public void canTransferMinimumAmount() {
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(0.01)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.transferWasSuccessful())
                .post(transferRequest);

        verifyBalances(initialBalance - 0.01, 0.01);
    }

    @Test
    public void canTransferMaximumWithinLimit() {
        // Переводим почти весь баланс (оставляем 0.01)
        Double transferAmount = initialBalance - 0.01;

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(transferAmount)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.transferWasSuccessful())
                .post(transferRequest);

        verifyBalances(0.01, transferAmount);
    }

    // ================= NEGATIVE TESTS =================

    @Test
    public void cannotTransferZero() {
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(0.0)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(transferRequest);

        // Проверка: балансы не изменились
        verifyBalances(initialBalance, 0.0);
    }

    @Test
    public void cannotTransferMoreThanBalance() {
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(initialBalance + 100.0)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(transferRequest);

        verifyBalances(initialBalance, 0.0);
    }

    @Test
    public void cannotTransferNegativeAmount() {
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(-50.0)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(transferRequest);

        verifyBalances(initialBalance, 0.0);
    }

    @Test
    public void cannotTransferAboveSystemLimit() {
        // Если в системе есть лимит 5000
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(5000.01)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest())
                .post(transferRequest);

        verifyBalances(initialBalance, 0.0);
    }

    // ================= EDGE CASES =================

    @Test
    public void getAccountUnauthorized() {
        given()
                .spec(RequestSpecs.unauthSpec())
                .when()
                .get("/api/v1/accounts/" + senderAccountId)
                .then()
                .spec(ResponseSpecs.unauthorized());
    }

    @Test
    public void getNonExistingAccount() {
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .when()
                .get("/api/v1/accounts/999999")
                .then()
                .spec(ResponseSpecs.notFound());
    }
}