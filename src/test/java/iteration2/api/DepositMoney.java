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
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import models.CreateAccountResponse;

import java.util.List;
import java.util.stream.Stream;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Депозиты на счет")
public class DepositMoney {

    private CreateUserRequest user;
    private String authToken;
    private Long accountId;

    @BeforeEach
    void setup() {
        user = AdminSteps.createUser();

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(user.getUsername())
                .password(user.getPassword())
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
                .as(AccountInfoResponse.class)
                .getId();
    }

    @ParameterizedTest(name = "Несколько депозитов: {0}")
    @MethodSource("multipleDepositsScenarios")
    @DisplayName("Несколько депозитов подряд")
    void shouldMaintainCorrectBalanceAfterMultipleDeposits(String scenarioName, List<Double> deposits, double expectedFinalBalance) {
        for (double amount : deposits) {
            new DepositRequester(
                    RequestSpecs.authSpec(authToken),
                    ResponseSpecs.balanceWasUpdated()
            ).post(buildDepositRequest(accountId, amount));
        }

        assertThat(getBalance())
                .as("Итоговый баланс после сценария: " + scenarioName)
                .isCloseTo(expectedFinalBalance, within(DELTA));
    }

    @ParameterizedTest(name = "Валидная сумма: {0}")
    @MethodSource("validDepositAmounts")
    @DisplayName("Валидные суммы депозитов")
    void shouldAcceptValidDepositAmounts(double amount) {
        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated()
        ).post(buildDepositRequest(accountId, amount));
    }

    @ParameterizedTest(name = "Невалидная сумма: {0} -> {1}")
    @MethodSource("invalidDepositAmounts")
    @DisplayName("Невалидные суммы депозитов")
    void shouldRejectInvalidDepositAmounts(double amount, String expectedMessage) {
        String errorResponse = new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(buildDepositRequest(accountId, amount))
                .extract()
                .asString();

        assertThat(errorResponse).contains(expectedMessage);
    }

    // ================= HELPER METHODS =================

    private DepositMoneyRequest buildDepositRequest(Long id, double amount) {
        return DepositMoneyRequest.builder()
                .id(id)
                .balance(amount)
                .build();
    }

    private double getBalance() {
        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(accounts).hasSize(1);
        return accounts.getFirst().getBalance();
    }
    private static Stream<Arguments> multipleDepositsScenarios() {
        return Stream.of(
                Arguments.of("Three deposits", List.of(1000.0, 500.0, 250.75), 1750.75),
                Arguments.of("Two deposits reaching limit", List.of(MIN_VALID_DEPOSIT, MAX_DEPOSIT_LIMIT - DELTA), MAX_DEPOSIT_LIMIT + 0.009),
                Arguments.of("Four deposits", List.of(100.0, 200.0, 300.0, 400.0), 1000.0)
        );
    }

    private static Stream<Arguments> validDepositAmounts() {
        return Stream.of(
                Arguments.of(MIN_VALID_DEPOSIT),
                Arguments.of(100.50),
                Arguments.of(MAX_DEPOSIT_LIMIT - DELTA),
                Arguments.of(MAX_DEPOSIT_LIMIT)
        );
    }

    private static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(ZERO_AMOUNT, ERROR_MIN_DEPOSIT),
                Arguments.of(SMALL_NEGATIVE_AMOUNT, ERROR_MIN_DEPOSIT),
                Arguments.of(MEDIUM_NEGATIVE_AMOUNT, ERROR_MIN_DEPOSIT),
                Arguments.of(LARGE_NEGATIVE_AMOUNT, ERROR_MIN_DEPOSIT),
                Arguments.of(SLIGHTLY_ABOVE_LIMIT, ERROR_MAX_DEPOSIT),
                Arguments.of(MODERATELY_ABOVE_LIMIT, ERROR_MAX_DEPOSIT),
                Arguments.of(FAR_ABOVE_LIMIT, ERROR_MAX_DEPOSIT),
                Arguments.of(EXTREME_ABOVE_LIMIT, ERROR_MAX_DEPOSIT)
        );
    }

    private static org.assertj.core.data.Offset<Double> within(double epsilon) {
        return org.assertj.core.data.Offset.offset(epsilon);
    }
}