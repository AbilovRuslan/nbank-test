package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferTest {

    private static final Random RANDOM = new Random();
    private static final double MIN_BALANCE = 0.01;
    private static final double MIN_TRANSFER = 0.01;

    private String authToken;
    private Long senderAccountId;
    private Long receiverAccountId;
    private Double initialBalance;

    @BeforeEach
    void setup() {
        String username = RandomData.getUsername();
        String password = RandomData.getPassword();

        // 1. Создание пользователя
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated()
        ).post(userRequest);

        // 2. Логин
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

        // 3. Создание двух счетов через CreateAccountRequester
        senderAccountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated()
        ).post()
                .extract()
                .path("id");

        receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated()
        ).post()
                .extract()
                .path("id");

        // 4. Пополнение счета отправителя
        initialBalance = 500.0 + RANDOM.nextDouble() * 4500.0;
        initialBalance = Math.round(initialBalance * 100.0) / 100.0;

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(initialBalance)
                .build();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated()
        ).post(depositRequest);
    }

    // ================= ПОЗИТИВНЫЕ ТЕСТЫ =================

    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    void shouldSuccessfullyTransferMoney(double transferAmount) {
        // Given
        double senderBalanceBefore = getAccountBalance(senderAccountId);
        double receiverBalanceBefore = getAccountBalance(receiverAccountId);

        // When
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId)
                .toAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.transferWasSuccessful()
        ).post(transferRequest);

        // Then
        double senderBalanceAfter = getAccountBalance(senderAccountId);
        double receiverBalanceAfter = getAccountBalance(receiverAccountId);

        assertEquals(senderBalanceBefore - transferAmount, senderBalanceAfter, 0.001,
                "Sender balance incorrect after transfer");
        assertEquals(receiverBalanceBefore + transferAmount, receiverBalanceAfter, 0.001,
                "Receiver balance incorrect after transfer");
    }

    private static Stream<Arguments> validTransferAmounts() {
        return Stream.of(
                Arguments.of(MIN_TRANSFER),
                Arguments.of(100.50),
                Arguments.of(1000.0),
                Arguments.of(2500.75)
        );
    }

    @ParameterizedTest
    @MethodSource("boundaryTransferAmounts")
    void shouldTransferBoundaryAmounts(double transferAmount) {
        double senderBalanceBefore = getAccountBalance(senderAccountId);
        double receiverBalanceBefore = getAccountBalance(receiverAccountId);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId)
                .toAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.transferWasSuccessful()
        ).post(transferRequest);

        double senderBalanceAfter = getAccountBalance(senderAccountId);
        double receiverBalanceAfter = getAccountBalance(receiverAccountId);

        assertEquals(senderBalanceBefore - transferAmount, senderBalanceAfter, 0.001);
        assertEquals(receiverBalanceBefore + transferAmount, receiverBalanceAfter, 0.001);
    }

    private Stream<Arguments> boundaryTransferAmounts() {
        double maxTransfer = initialBalance - MIN_BALANCE;
        return Stream.of(
                Arguments.of(MIN_TRANSFER),
                Arguments.of(maxTransfer)
        );
    }

    // ================= НЕГАТИВНЫЕ ТЕСТЫ =================

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.01, -100.0, -999.99})
    void shouldRejectInvalidAmounts(double amount) {
        double senderBalanceBefore = getAccountBalance(senderAccountId);
        double receiverBalanceBefore = getAccountBalance(receiverAccountId);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId)
                .toAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(transferRequest);

        double senderBalanceAfter = getAccountBalance(senderAccountId);
        double receiverBalanceAfter = getAccountBalance(receiverAccountId);

        assertEquals(senderBalanceBefore, senderBalanceAfter, 0.001,
                "Sender balance changed after invalid transfer");
        assertEquals(receiverBalanceBefore, receiverBalanceAfter, 0.001,
                "Receiver balance changed after invalid transfer");
    }

    @Test
    void shouldRejectTransferExceedingBalance() {
        double transferAmount = initialBalance + 100.0;
        double senderBalanceBefore = getAccountBalance(senderAccountId);
        double receiverBalanceBefore = getAccountBalance(receiverAccountId);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId)
                .toAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(transferRequest);

        double senderBalanceAfter = getAccountBalance(senderAccountId);
        double receiverBalanceAfter = getAccountBalance(receiverAccountId);

        assertEquals(senderBalanceBefore, senderBalanceAfter, 0.001);
        assertEquals(receiverBalanceBefore, receiverBalanceAfter, 0.001);
    }

    @Test
    void shouldRejectTransferToNonExistingAccount() {
        long nonExistingAccountId = 999999L;
        double senderBalanceBefore = getAccountBalance(senderAccountId);
        double receiverBalanceBefore = getAccountBalance(receiverAccountId);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId)
                .toAccountId(nonExistingAccountId)
                .amount(10.0)
                .build();

        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.notFound()
        ).post(transferRequest);

        double senderBalanceAfter = getAccountBalance(senderAccountId);
        double receiverBalanceAfter = getAccountBalance(receiverAccountId);

        assertEquals(senderBalanceBefore, senderBalanceAfter, 0.001);
        assertEquals(receiverBalanceBefore, receiverBalanceAfter, 0.001);
    }

    // ================= ТЕСТЫ ДОСТУПА =================

    @Test
    void shouldNotAllowUnauthorizedAccessToAccount() {
        new AccountRequests(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.unauthorized()
        ).getAccount(senderAccountId);
    }

    @Test
    void shouldReturnNotFoundForNonExistingAccount() {
        new AccountRequests(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.notFound()
        ).getAccount(999999L);
    }

    // ================= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =================

    private double getAccountBalance(Long accountId) {
        return new AccountRequests(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.requestReturnsOK()
        ).getAccount(accountId)
                .getBalance();
    }
}