package iteration2.api;

import dao.AccountDao;
import dao.compration.DaoAndModelAssertions;
import requests.steps.DataBaseSteps;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import models.AccountInfoResponse;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Депозиты на счет")
public class DepositMoney {

    private CreateUserRequest userRequest;
    private String authToken;

    @BeforeEach
    void setup() {
        userRequest = AdminSteps.createUser();

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .build();

        authToken = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK()
        ).post(loginRequest)
                .extract()
                .header("Authorization");
    }

    @ParameterizedTest(name = "Несколько депозитов: {0}")
    @MethodSource("multipleDepositsScenarios")
    @DisplayName("Несколько депозитов подряд")
    void shouldMaintainCorrectBalanceAfterMultipleDeposits(String scenarioName, List<Double> deposits, double expectedFinalBalance) {
        AccountInfoResponse account = createAccount();

        for (double amount : deposits) {
            deposit(account.getId(), amount);
        }

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());
        DaoAndModelAssertions.assertThat(account, accountDao).match();
        assertThat(accountDao.getBalance()).isCloseTo(expectedFinalBalance, within(DELTA));
    }

    @ParameterizedTest(name = "Валидная сумма: {0}")
    @MethodSource("validDepositAmounts")
    @DisplayName("Валидные суммы депозитов")
    void shouldAcceptValidDepositAmounts(double amount) {
        AccountInfoResponse account = createAccount();

        deposit(account.getId(), amount);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());
        assertThat(accountDao.getBalance()).isCloseTo(amount, within(DELTA));
    }

    @ParameterizedTest(name = "Невалидная сумма: {0} -> {1}")
    @MethodSource("invalidDepositAmounts")
    @DisplayName("Невалидные суммы депозитов")
    void shouldRejectInvalidDepositAmounts(double amount, String expectedMessage) {
        AccountInfoResponse account = createAccount();

        String errorResponse = depositAndGetError(account.getId(), amount);
        assertThat(errorResponse).contains(expectedMessage);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());
        assertThat(accountDao.getBalance()).isZero();
    }

    @Test
    @DisplayName("Депозит на несуществующий счет")
    void shouldThrowWhenDepositingToNonExistentAccount() {
        String errorResponse = depositAndGetError(NON_EXISTENT_ACCOUNT_ID, TRANSFER_AMOUNT_SMALL);
        assertThat(errorResponse).contains("Unauthorized access to account");

        AccountDao accountDao = DataBaseSteps.getAccountById(NON_EXISTENT_ACCOUNT_ID);
        assertThat(accountDao).isNull();
    }

    @Test
    @DisplayName("Депозит без авторизации")
    void shouldThrowWhenDepositingWithoutAuth() {
        AccountInfoResponse account = createAccount();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.DEPOSIT,
                ResponseSpecs.unauthorized()
        ).post(DepositMoneyRequest.builder()
                .id(account.getId())
                .balance(TRANSFER_AMOUNT_SMALL)
                .build());

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(account.getAccountNumber());
        assertThat(accountDao.getBalance()).isZero();
    }

    // ================= HELPER METHODS =================

    private AccountInfoResponse createAccount() {
        return new CrudRequester(
                RequestSpecs.authSpec(authToken),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        ).post(null)
                .extract()
                .as(AccountInfoResponse.class);
    }

    private void deposit(Long accountId, double amount) {
        new CrudRequester(
                RequestSpecs.authSpec(authToken),
                Endpoint.DEPOSIT,
                ResponseSpecs.balanceWasUpdated()
        ).post(DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build());
    }

    private String depositAndGetError(Long accountId, double amount) {
        return new CrudRequester(
                RequestSpecs.authSpec(authToken),
                Endpoint.DEPOSIT,
                ResponseSpecs.badRequest()
        ).post(DepositMoneyRequest.builder()
                        .id(accountId)
                        .balance(amount)
                        .build())
                .extract()
                .body()
                .asString();
    }

    private static Stream<Arguments> multipleDepositsScenarios() {
        return Stream.of(
                Arguments.of("Three deposits", List.of(1000.0, 500.0, 250.75), 1750.75),
                Arguments.of("Accumulated balance above deposit limit", List.of(MIN_VALID_DEPOSIT, MAX_DEPOSIT_LIMIT - DELTA), MAX_DEPOSIT_LIMIT + 0.009),
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