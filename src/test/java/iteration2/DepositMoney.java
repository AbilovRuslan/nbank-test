package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepositMoney {

    private static final double MAX_DEPOSIT_LIMIT = 5000.0;
    private static final double MIN_VALID_DEPOSIT = 0.01;
    private static final Random RANDOM = new Random();

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

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(userRequest);

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(username)
                .password(password)
                .build();

        authToken = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK()
        ).post(loginRequest)
                .extract()
                .header("Authorization");

        accountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated()
        ).post()
                .extract()
                .path("id");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multipleDepositsScenarios")
    void multipleDepositsWithinLimit(String scenarioName, List<Double> deposits, double expectedFinalBalance) {
        double balanceBefore = getCurrentBalance();
        double runningExpected = balanceBefore;

        for (int i = 0; i < deposits.size(); i++) {
            double amount = deposits.get(i);
            AccountInfoResponse response = executeDeposit(amount);

            runningExpected += amount;
            assertEquals(runningExpected, response.getBalance(), 0.001,
                    String.format("Balance mismatch after deposit %d: %.2f", i + 1, amount));
        }

        assertEquals(expectedFinalBalance, getCurrentBalance(), 0.001,
                "Final balance verification failed");
    }

    private static Stream<Arguments> multipleDepositsScenarios() {
        return Stream.of(
                Arguments.of("Three deposits", List.of(1000.0, 500.0, 250.75), 1750.75),
                Arguments.of("Two deposits reaching limit", List.of(0.01, 4999.99), 5000.0),
                Arguments.of("Four deposits", List.of(100.0, 200.0, 300.0, 400.0), 1000.0)
        );
    }

    @Test
    void shouldRejectDepositWhenTotalExceedsLimit() {
        // Given: баланс близок к лимиту
        executeDeposit(4999.99);
        double balanceBefore = getCurrentBalance();

        // When: пытаемся внести депозит, который превысит лимит
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(0.02)
                .build();

        ErrorResponse errorResponse = new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(request)
                .extract()
                .as(ErrorResponse.class);

        // Then: проверяем код ошибки
        assertEquals("DEPOSIT_LIMIT_EXCEEDED", errorResponse.getCode());

        // Then: проверяем что баланс не изменился
        double balanceAfter = getCurrentBalance();
        assertEquals(balanceBefore, balanceAfter, 0.001);
    }

    @RepeatedTest(3)
    void repeatedDepositTest() {
        double amount = generateRandomDepositAmount();
        double balanceBefore = getCurrentBalance();

        AccountInfoResponse response = executeDeposit(amount);

        double expectedBalance = balanceBefore + amount;
        assertEquals(expectedBalance, response.getBalance(), 0.001);
        assertEquals(expectedBalance, getCurrentBalance(), 0.001);
    }

    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    void shouldSuccessfullyDepositValidAmount(double amount) {
        double balanceBefore = getCurrentBalance();

        AccountInfoResponse response = executeDeposit(amount);

        double expectedBalance = balanceBefore + amount;
        assertEquals(expectedBalance, response.getBalance(), 0.001);
        assertEquals(expectedBalance, getCurrentBalance(), 0.001);
        assertTrue(response.getBalance() <= MAX_DEPOSIT_LIMIT);
    }

    private static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(0.01),
                Arguments.of(100.50),
                Arguments.of(2500.75),
                Arguments.of(4999.99)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAmountsWithExpectedErrors")
    void shouldRejectInvalidDepositAmount(double amount, String expectedErrorCode) {
        double balanceBefore = getCurrentBalance();

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(amount)
                .build();

        ErrorResponse errorResponse = new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(request)
                .extract()
                .as(ErrorResponse.class);

        assertEquals(expectedErrorCode, errorResponse.getCode());

        // Проверяем что баланс не изменился
        double balanceAfter = getCurrentBalance();
        assertEquals(balanceBefore, balanceAfter, 0.001);
    }

    private static Stream<Arguments> invalidDepositAmountsWithExpectedErrors() {
        return Stream.of(
                // Негативные и нулевые значения
                Arguments.of(0.0, "INVALID_AMOUNT"),
                Arguments.of(-0.01, "INVALID_AMOUNT"),
                Arguments.of(-100.0, "INVALID_AMOUNT"),
                Arguments.of(-999.99, "INVALID_AMOUNT"),
                // Значения выше лимита
                Arguments.of(5000.01, "DEPOSIT_LIMIT_EXCEEDED"),
                Arguments.of(5000.1, "DEPOSIT_LIMIT_EXCEEDED"),
                Arguments.of(6000.0, "DEPOSIT_LIMIT_EXCEEDED"),
                Arguments.of(10000.0, "DEPOSIT_LIMIT_EXCEEDED")
        );
    }

    @Test
    void depositWithTwoDecimalPlaces() {
        double amount = 1234.56;
        double balanceBefore = getCurrentBalance();

        AccountInfoResponse response = executeDeposit(amount);

        double expectedBalance = balanceBefore + amount;
        assertEquals(expectedBalance, response.getBalance(), 0.001);
        assertEquals(expectedBalance, getCurrentBalance(), 0.001);

        // Проверяем точность десятичных знаков
        String balanceStr = String.valueOf(response.getBalance());
        int decimalDigits = balanceStr.contains(".") ?
                balanceStr.split("\\.")[1].length() : 0;
        assertTrue(decimalDigits <= 2,
                "Balance has more than 2 decimal places: " + balanceStr);
    }

    // ================= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =================
    private AccountInfoResponse executeDeposit(double amount) {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId.longValue())
                .balance(amount)
                .build();

        return new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated()
        ).post(request)
                .extract()
                .as(AccountInfoResponse.class);
    }

    private double getCurrentBalance() {
        return new AccountRequests(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.requestReturnsOK()
        ).getAccount(accountId)
                .extract()
                .as(AccountInfoResponse.class)
                .getBalance();
    }

    private double generateRandomDepositAmount() {
        double amount = MIN_VALID_DEPOSIT + RANDOM.nextDouble() * (MAX_DEPOSIT_LIMIT - MIN_VALID_DEPOSIT);
        return Math.round(amount * 100.0) / 100.0;
    }
}