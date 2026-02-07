package iteration2;

import constants.Endpoints;
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
                .post(Endpoints.ADMIN_USERS)
                .then()
                .spec(ResponseSpecs.entityWasCreated());

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(username)
                .password(password)
                .build();

        authToken = given()
                .spec(RequestSpecs.unauthSpec())
                .body(loginRequest)
                .post(Endpoints.LOGIN)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .extract()
                .header(ResponseSpecs.AUTHORIZATION_HEADER);

        accountId = given()
                .spec(RequestSpecs.authSpec(authToken))
                .post(Endpoints.ACCOUNTS)
                .then()
                .spec(ResponseSpecs.entityWasCreated())
                .extract()
                .path("id");
    }

    // ================= ALL POSITIVE TESTS (12 проверок) =================

    @Test
    void canDepositMinimumAmount() {
        deposit(MIN_DEPOSIT_AMOUNT);
        verifyBalance(MIN_DEPOSIT_AMOUNT);
    }

    @Test
    void canDepositMaximumAmount() {
        deposit(MAX_DEPOSIT_LIMIT);
        verifyBalance(MAX_DEPOSIT_LIMIT);
    }

    @ParameterizedTest
    @CsvSource({
            "0.01",      // мин
            "100.50",    // обычная
            "2500.75",   // средняя
            "4999.99",   // почти макс
            "5000.0"     // макс
    })
    void customerCanDepositValidAmounts(double amount) {
        deposit(amount);
        verifyBalance(amount);
    }

    @Test
    void customerCanDepositRandomPositiveAmount() {
        double amount = MIN_DEPOSIT_AMOUNT +
                RANDOM.nextDouble() * (MAX_DEPOSIT_LIMIT - MIN_DEPOSIT_AMOUNT);
        amount = Math.round(amount * 100.0) / 100.0;

        deposit(amount);
        verifyBalance(amount);
    }

    @Test
    void multipleDepositsUpdateBalanceCorrectly() {
        double first = 1000.0;
        double second = 500.0;

        deposit(first);
        verifyBalance(first);

        deposit(second);
        verifyBalance(first + second);
    }

    // ================= ALL NEGATIVE TESTS (8 проверок) =================

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.01, -100.0, -999.99})
    void cannotDepositNegativeOrZeroAmount(double amount) {
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .body(DepositMoneyRequest.builder()
                        .id(accountId.longValue())
                        .balance(amount)
                        .build())
                .post(Endpoints.DEPOSIT)
                .then()
                .spec(ResponseSpecs.badRequest());

        verifyBalance(0.0);
    }

    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 5000.1, 6000.0, 10000.0})
    void cannotDepositAboveLimit(double amount) {
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .body(DepositMoneyRequest.builder()
                        .id(accountId.longValue())
                        .balance(amount)
                        .build())
                .post(Endpoints.DEPOSIT)
                .then()
                .spec(ResponseSpecs.badRequest());

        verifyBalance(0.0);
    }

    // ================= HELPER METHODS =================

    private void deposit(double amount) {
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .body(DepositMoneyRequest.builder()
                        .id(accountId.longValue())
                        .balance(amount)
                        .build())
                .post(Endpoints.DEPOSIT)
                .then()
                .spec(ResponseSpecs.balanceWasUpdated());
    }

    private void verifyBalance(double expected) {
        given()
                .spec(RequestSpecs.authSpec(authToken))
                .get(Endpoints.accountById(accountId))
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .body("balance", equalTo((float) expected));
    }
}