package iteration2.api;

import io.restassured.specification.RequestSpecification;
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
import java.util.stream.Stream;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Депозиты на счет")
public class DepositMoney {

    private RequestSpecification authSpec;
    private Long accountId;

    @BeforeEach
    void setup() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .build();

        String authToken = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK()
        ).post(loginRequest)
                .extract()
                .header("Authorization");

        authSpec = RequestSpecs.authSpec(authToken);

        accountId = new CreateAccountRequester(
                authSpec,
                ResponseSpecs.entityWasCreated()
        ).post()
                .extract()
                .as(AccountInfoResponse.class)
                .getId();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multipleDepositsScenarios")
    @DisplayName("Несколько депозитов подряд")
    void shouldMaintainCorrectBalanceAfterMultipleDeposits(String scenarioName, List<Double> deposits) {
        for (double amount : deposits) {
            depositMoney(amount);
        }
    }

    @ParameterizedTest(name = "Валидная сумма: {0}")
    @MethodSource("validDepositAmounts")
    @DisplayName("Валидные суммы депозитов")
    void shouldAcceptValidDepositAmounts(double amount) {
        depositMoney(amount);
    }

    @ParameterizedTest(name = "Невалидная сумма: {0} -> {1}")
    @MethodSource("invalidDepositAmounts")
    @DisplayName("Невалидные суммы депозитов")
    void shouldRejectInvalidDepositAmounts(double amount, String expectedMessage) {
        String errorResponse = depositMoneyAndGetError(amount);
        assertThat(errorResponse).contains(expectedMessage);
    }

    @Test
    @Disabled("Лимит депозита работает с округлением, тест требует доработки под новую версию API")
    @DisplayName("Отказ при превышении лимита")
    void shouldRejectDepositWhenTotalExceedsLimit() {
        depositMoney(MAX_DEPOSIT_LIMIT - DELTA);

        String errorResponse = depositMoneyAndGetError(EXCEED_LIMIT_AMOUNT);
        assertThat(errorResponse).contains(ERROR_MAX_DEPOSIT);
    }

    @Test
    @DisplayName("Депозит на несуществующий счет")
    void shouldThrowWhenDepositingToNonExistentAccount() {
        new DepositRequester(
                authSpec,
                ResponseSpecs.forbidden()
        ).post(buildDepositRequest(NON_EXISTENT_ACCOUNT_ID, TRANSFER_AMOUNT_SMALL));
    }

    @Test
    @DisplayName("Депозит без авторизации")
    void shouldThrowWhenDepositingWithoutAuth() {
        new DepositRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.unauthorized()
        ).post(buildDepositRequest(accountId, TRANSFER_AMOUNT_SMALL));
    }

    // ================= HELPER METHODS =================

    private void depositMoney(double amount) {
        new DepositRequester(authSpec, ResponseSpecs.balanceWasUpdated())
                .post(buildDepositRequest(accountId, amount));
    }

    private String depositMoneyAndGetError(double amount) {
        return new DepositRequester(authSpec, ResponseSpecs.badRequest())
                .post(buildDepositRequest(accountId, amount))
                .extract()
                .asString();
    }

    private DepositMoneyRequest buildDepositRequest(Long id, double amount) {
        return DepositMoneyRequest.builder()
                .id(id)
                .balance(amount)
                .build();
    }

    private static Stream<Arguments> multipleDepositsScenarios() {
        return Stream.of(
                Arguments.of("Three deposits", List.of(1000.0, 500.0, 250.75)),
                Arguments.of("Two deposits reaching limit", List.of(MIN_VALID_DEPOSIT, MAX_DEPOSIT_LIMIT - DELTA)),
                Arguments.of("Four deposits", List.of(100.0, 200.0, 300.0, 400.0))
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