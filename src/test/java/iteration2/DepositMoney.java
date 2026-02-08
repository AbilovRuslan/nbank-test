package iteration2;

import generators.RandomData;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoney {

    private static final double MAX_DEPOSIT_LIMIT = 5000.0;
    private static final double MIN_VALID_DEPOSIT = 0.01;
    private static final Random RANDOM = new Random();

    private String authToken;
    private Integer accountId;
    private double currentBalance = 0.0;

    @BeforeEach
    void setup() {
        String username = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        given()
                .spec(RequestSpecs.adminSpec())
                .body(userRequest)
                .post(getAdminUsersPath())
                .then()
                .spec(ResponseSpecs.entityWasCreated());

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(username)
                .password(password)
                .build();

        authToken = given()
                .spec(RequestSpecs.unauthSpec())
                .body(loginRequest)
                .post(getAuthLoginPath())
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .extract()
                .header("Authorization");

        // Предположим, что ответ содержит ID аккаунта
        accountId = given()
                .spec(RequestSpecs.authSpec(authToken))
                .post(getAccountsPath())
                .then()
                .spec(ResponseSpecs.entityWasCreated())
                .extract()
                .path("id"); // или другой путь в зависимости от структуры ответа

        currentBalance = 0.0;
    }

    @Test
    void multipleDepositsWithinLimit() {
        depositAndCheckBalance(1000.0);
        depositAndCheckBalance(500.0);
        depositAndCheckBalance(250.75);
    }

    @Test
    void cannotMakeDepositThatExceedsLimitAfterPreviousDeposits() {
        depositAndCheckBalance(4999.99);

        // Второй депозит - превышение лимита
        float newBalance = given()
                .spec(RequestSpecs.authSpec(authToken))
                .body(createDepositRequest(0.02))
                .post(getAccountsDepositPath())
                .then()
                .spec(ResponseSpecs.balanceWasUpdated())
                .extract()
                .path("balance");

        assertEquals(5000.01, newBalance, 0.001);
        currentBalance = newBalance;
    }

    @RepeatedTest(3)
    void repeatedDepositTest() {
        double amount = generateRandomDepositAmount();
        depositAndCheckBalance(amount);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 100.50, 2500.75, 4999.99, 5000.0})
    void customerCanDepositValidAmounts(double amount) {
        depositAndCheckBalance(amount);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.01, -100.0, -999.99})
    void cannotDepositNegativeOrZeroAmount(double amount) {
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .body(createDepositRequest(amount))
                .post(getAccountsDepositPath())
                .then()
                .spec(ResponseSpecs.badRequest());
    }

    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 5000.1, 6000.0, 10000.0})
    void cannotDepositAboveLimit(double amount) {
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .body(createDepositRequest(amount))
                .post(getAccountsDepositPath())
                .then()
                .spec(ResponseSpecs.badRequest());
    }

    @Test
    void depositExactMaximumLimit() {
        depositAndCheckBalance(MAX_DEPOSIT_LIMIT);
    }

    @Test
    void depositExactMinimumLimit() {
        depositAndCheckBalance(MIN_VALID_DEPOSIT);
    }

    @Test
    void depositWithTwoDecimalPlaces() {
        depositAndCheckBalance(1234.56);
    }

    // ================= МЕТОДЫ ДЛЯ ПУТЕЙ API =================
    // Выносим пути в методы для устранения хардкода

    private String getAdminUsersPath() {
        return "/api/v1/admin/users";
    }

    private String getAuthLoginPath() {
        return "/api/v1/auth/login";
    }

    private String getAccountsPath() {
        return "/api/v1/accounts";
    }

    private String getAccountsDepositPath() {
        return "/api/v1/accounts/deposit";
    }

    // ================= ПРИВАТНЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =================

    private void depositAndCheckBalance(double amount) {
        float newBalance = given()
                .spec(RequestSpecs.authSpec(authToken))
                .body(createDepositRequest(amount))
                .post(getAccountsDepositPath())
                .then()
                .spec(ResponseSpecs.balanceWasUpdated())
                .extract()
                .path("balance");

        double expectedBalance = currentBalance + amount;
        assertEquals(expectedBalance, newBalance, 0.001);
        currentBalance += amount;
    }

    private DepositMoneyRequest createDepositRequest(double amount) {
        return DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(amount)
                .build();
    }

    private double generateRandomDepositAmount() {
        double amount = MIN_VALID_DEPOSIT + RANDOM.nextDouble() * (MAX_DEPOSIT_LIMIT - MIN_VALID_DEPOSIT);
        return Math.round(amount * 100.0) / 100.0;
    }
}