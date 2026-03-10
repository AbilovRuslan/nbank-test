package iteration2;

import models.AccountInfoResponse;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Депозиты на счет")
public class DepositMoney {

    private static final double MAX_DEPOSIT_LIMIT = 5000.0;
    private static final double MIN_VALID_DEPOSIT = 0.01;
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

        accountId = accountResponse.getId();  //
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multipleDepositsScenarios")
    @DisplayName("Несколько депозитов подряд")
    void shouldMaintainCorrectBalanceAfterMultipleDeposits(String scenarioName, List<Double> deposits, double expectedFinalBalance) {
        AtomicReference<Double> runningBalance = new AtomicReference<>(getCurrentBalance());

        List<AccountInfoResponse> responses = deposits.stream()
                .map(amount -> {
                    AccountInfoResponse response = executeDeposit(amount);
                    runningBalance.set(runningBalance.get() + amount);

                    assertThat(response.getBalance())
                            .as("Balance after deposit of %.2f", amount)
                            .isCloseTo(runningBalance.get(), within(0.001));

                    return response;
                })
                .collect(Collectors.toList());

        assertAll(
                () -> assertThat(responses).hasSize(deposits.size()),
                () -> assertThat(getCurrentBalance())
                        .as("Final balance")
                        .isCloseTo(expectedFinalBalance, within(0.001))
        );
    }

    private static Stream<Arguments> multipleDepositsScenarios() {
        return Stream.of(
                Arguments.of("Three deposits", List.of(1000.0, 500.0, 250.75), 1750.75),
                Arguments.of("Two deposits reaching limit", List.of(0.01, 4999.99), 5000.0),
                Arguments.of("Four deposits", List.of(100.0, 200.0, 300.0, 400.0), 1000.0)
        );
    }

    @Test
    @DisplayName("Отказ при превышении лимита")
    void shouldRejectDepositWhenTotalExceedsLimit() {
        // Given
        executeDeposit(4999.99);
        double balanceBefore = getCurrentBalance();

        // When
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(0.02)
                .build();

        String errorResponse = new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(request)
                .extract()
                .asString();

        // Then
        double balanceAfter = getCurrentBalance();

        assertAll(
                () -> assertThat(errorResponse).isEqualTo("Deposit amount cannot exceed 5000"),
                () -> assertThat(balanceAfter).isCloseTo(balanceBefore, within(0.001)),
                () -> assertThat(balanceAfter).isLessThanOrEqualTo(MAX_DEPOSIT_LIMIT + 0.001)
        );
    }

    @RepeatedTest(5)
    @DisplayName("Случайные суммы депозитов")
    void shouldHandleRandomDepositAmounts() {
        double amount = generateRandomDepositAmount();
        double balanceBefore = getCurrentBalance();

        AccountInfoResponse response = executeDeposit(amount);

        assertThat(response.getBalance())
                .as("Balance after random deposit %.2f", amount)
                .isCloseTo(balanceBefore + amount, within(0.001));
    }

    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    @DisplayName("Валидные суммы депозитов")
    void shouldAcceptValidDepositAmounts(double amount) {
        double balanceBefore = getCurrentBalance();

        AccountInfoResponse response = executeDeposit(amount);

        assertAll(
                () -> assertThat(response.getBalance())
                        .isCloseTo(balanceBefore + amount, within(0.001)),
                () -> assertThat(response.getBalance())
                        .isLessThanOrEqualTo(MAX_DEPOSIT_LIMIT + 0.001),
                () -> assertThat(response.getId()).isEqualTo(accountId)
        );
    }

    private static Stream<Double> validDepositAmounts() {
        return Stream.of(0.01, 100.50, 2500.75, 4999.99, 5000.0);
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
                () -> assertThat(balanceAfter).isCloseTo(balanceBefore, within(0.001))
        );

    }

    private static Stream<InvalidDepositTestData> invalidDepositAmountsProvider() {
        return Stream.of(
                new InvalidDepositTestData(0.0, "Deposit amount must be at least 0.01"),
                new InvalidDepositTestData(-0.01, "Deposit amount must be at least 0.01"),
                new InvalidDepositTestData(-100.0, "Deposit amount must be at least 0.01"),
                new InvalidDepositTestData(-999.99, "Deposit amount must be at least 0.01"),
                new InvalidDepositTestData(5000.01, "Deposit amount cannot exceed 5000"),
                new InvalidDepositTestData(5000.1, "Deposit amount cannot exceed 5000"),
                new InvalidDepositTestData(6000.0, "Deposit amount cannot exceed 5000"),
                new InvalidDepositTestData(10000.0, "Deposit amount cannot exceed 5000")
        );
    }

    @Test
    @DisplayName("Проверка точности до двух знаков")
    void shouldPreservePrecisionForTwoDecimalPlaces() {
        double amount = 1234.56;

        AccountInfoResponse response = executeDeposit(amount);

        assertThat(response.getBalance())
                .as("Balance should have max 2 decimal places")
                .satisfies(balance -> {
                    String balanceStr = String.valueOf(balance);
                    if (balanceStr.contains(".")) {
                        int decimalDigits = balanceStr.split("\\.")[1].length();
                        assertThat(decimalDigits).isLessThanOrEqualTo(2);
                    }
                });
    }

    @Test
    @DisplayName("Депозит на несуществующий счет")
    void shouldThrowWhenDepositingToNonExistentAccount() {
        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(999999L)
                .balance(100.0)
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
                .balance(100.0)
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

        return 0.0;
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