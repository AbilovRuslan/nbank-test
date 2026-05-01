package iteration2.api;

import models.AccountInfoResponse;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.LoginUserRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Депозиты на счет")
public class DepositMoney {

    private static final Random RANDOM = new Random();

    private String authToken;
    private Long accountId;

    @BeforeEach
    void setup() {
        // Используем AdminSteps как в iteration1!
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Логинимся созданным пользователем
        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .build();

        authToken = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK()
        ).post(loginRequest)
                .extract()
                .header("Authorization");

        AccountInfoResponse accountResponse = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated()
        ).post()
                .extract()
                .as(AccountInfoResponse.class);

        accountId = accountResponse.getId();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multipleDepositsScenarios")
    @DisplayName("Несколько депозитов подряд")
    void shouldMaintainCorrectBalanceAfterMultipleDeposits(String scenarioName, List<Double> deposits, double expectedFinalBalance) {
        double runningBalance = 0.0;

        for (double amount : deposits) {
            AccountInfoResponse response = executeDeposit(amount);
            runningBalance = runningBalance + amount;

            assertThat(response.getBalance())
                    .as("Balance after deposit of %.2f", amount)
                    .isCloseTo(runningBalance, within(DELTA));
        }

        assertThat(runningBalance).isCloseTo(expectedFinalBalance, within(DELTA));
    }
    private static Stream<Arguments> multipleDepositsScenarios() {
        return Stream.of(
                Arguments.of("Three deposits", List.of(1000.0, 500.0, 250.75), 1750.75),
                Arguments.of("Two deposits reaching limit", List.of(MIN_VALID_DEPOSIT, MAX_DEPOSIT_LIMIT - DELTA), MAX_DEPOSIT_LIMIT + 0.009),
                Arguments.of("Four deposits", List.of(100.0, 200.0, 300.0, 400.0), 1000.0)
        );
    }

    @Test
    @Disabled("Лимит депозита работает с округлением, тест требует доработки под новую версию API")
    @DisplayName("Отказ при превышении лимита")
    void shouldRejectDepositWhenTotalExceedsLimit() {
        executeDeposit(MAX_DEPOSIT_LIMIT - DELTA);

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(EXCEED_LIMIT_AMOUNT)
                .build();

        String errorResponse = new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(request)
                .extract()
                .asString();

        assertThat(errorResponse).contains(ERROR_MAX_DEPOSIT);
    }

    @RepeatedTest(RANDOM_TEST_REPETITIONS)
    @DisplayName("Случайные суммы депозитов")
    void shouldHandleRandomDepositAmounts() {
        double amount = generateRandomDepositAmount();
        double balanceBefore = getCurrentBalance();

        AccountInfoResponse response = executeDeposit(amount);

        assertThat(response.getBalance())
                .as("Balance after random deposit %.2f", amount)
                .isCloseTo(balanceBefore + amount, within(DELTA));
    }

    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    @DisplayName("Валидные суммы депозитов")
    void shouldAcceptValidDepositAmounts(double amount) {
        double balanceBefore = getCurrentBalance();

        AccountInfoResponse response = executeDeposit(amount);

        assertAll(
                () -> assertThat(response.getBalance())
                        .isCloseTo(balanceBefore + amount, within(DELTA)),
                () -> assertThat(response.getBalance())
                        .isLessThanOrEqualTo(MAX_DEPOSIT_LIMIT + DELTA),
                () -> assertThat(response.getId()).isEqualTo(accountId)
        );
    }

    private static Stream<Double> validDepositAmounts() {
        return Stream.of(VALID_DEPOSIT_AMOUNTS);
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAmountsProvider")
    @DisplayName("Невалидные суммы депозитов")
    void shouldRejectInvalidDepositAmounts(InvalidDepositTestData testData) {
        double balanceBefore = getCurrentBalance();

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(testData.amount)
                .build();

        String errorResponse = new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(request)
                .extract()
                .asString();

        double balanceAfter = getCurrentBalance();

        assertAll(
                () -> assertThat(errorResponse).isEqualTo(testData.expectedMessage),
                () -> assertThat(balanceAfter).isCloseTo(balanceBefore, within(DELTA))
        );
    }

    private static Stream<InvalidDepositTestData> invalidDepositAmountsProvider() {
        return Stream.of(
                new InvalidDepositTestData(ZERO_AMOUNT, ERROR_MIN_DEPOSIT),
                new InvalidDepositTestData(SMALL_NEGATIVE_AMOUNT, ERROR_MIN_DEPOSIT),
                new InvalidDepositTestData(MEDIUM_NEGATIVE_AMOUNT, ERROR_MIN_DEPOSIT),
                new InvalidDepositTestData(LARGE_NEGATIVE_AMOUNT, ERROR_MIN_DEPOSIT),
                new InvalidDepositTestData(SLIGHTLY_ABOVE_LIMIT, ERROR_MAX_DEPOSIT),
                new InvalidDepositTestData(MODERATELY_ABOVE_LIMIT, ERROR_MAX_DEPOSIT),
                new InvalidDepositTestData(FAR_ABOVE_LIMIT, ERROR_MAX_DEPOSIT),
                new InvalidDepositTestData(EXTREME_ABOVE_LIMIT, ERROR_MAX_DEPOSIT)
        );
    }

    @Test
    @DisplayName("Проверка точности до двух знаков")
    void shouldPreservePrecisionForTwoDecimalPlaces() {
        AccountInfoResponse response = executeDeposit(PRECISION_TEST_AMOUNT);

        assertThat(response.getBalance())
                .as("Balance should have max %d decimal places", MAX_DECIMAL_PLACES)
                .satisfies(balance -> {
                    String balanceStr = String.valueOf(balance);
                    if (balanceStr.contains(".")) {
                        int decimalDigits = balanceStr.split("\\.")[1].length();
                        assertThat(decimalDigits).isLessThanOrEqualTo(MAX_DECIMAL_PLACES);
                    }
                });
    }

    @Test
    @DisplayName("Депозит на несуществующий счет")
    void shouldThrowWhenDepositingToNonExistentAccount() {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(NON_EXISTENT_ACCOUNT_ID)
                .balance(TRANSFER_AMOUNT_SMALL)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.forbidden()
        ).post(request);
    }

    @Test
    @DisplayName("Депозит без авторизации")
    void shouldThrowWhenDepositingWithoutAuth() {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(TRANSFER_AMOUNT_SMALL)
                .build();

        new DepositRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.unauthorized()
        ).post(request);
    }

    // ================= HELPER METHODS =================

    private AccountInfoResponse executeDeposit(double amount) {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
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
        return 0.0; // TODO: реализовать получение баланса через API
    }

    private double generateRandomDepositAmount() {
        return MIN_VALID_DEPOSIT + (MAX_DEPOSIT_LIMIT - MIN_VALID_DEPOSIT) * RANDOM.nextDouble();
    }

    private static org.assertj.core.data.Offset<Double> within(double epsilon) {
        return org.assertj.core.data.Offset.offset(epsilon);
    }

    // Внутренний класс для тестовых данных
    private static class InvalidDepositTestData {
        final double amount;
        final String expectedMessage;

        InvalidDepositTestData(double amount, String expectedMessage) {
            this.amount = amount;
            this.expectedMessage = expectedMessage;
        }
    }
}