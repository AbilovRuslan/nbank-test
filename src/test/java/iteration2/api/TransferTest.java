package iteration2.api;

import iteration2.fixtures.TestAccounts;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.LoginUserRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Random;
import java.util.stream.Stream;

import static constants.TestConstants.*;
import static iteration2.fixtures.TestAccounts.MIN_TRANSFER;

@DisplayName("Переводы между счетами — Senior Level")
public class TransferTest {

    private static final Random RANDOM = new Random();

    private String authToken;
    private TestAccounts accounts;
    private Long senderAccountId;
    private Long receiverAccountId;
    private Long nonExistingAccountId;
    private Double initialBalance;

    @BeforeEach
    void setup() {
        var user = AdminSteps.createUser();
        authToken = login(user.getUsername(), user.getPassword());

        accounts = new TestAccounts(authToken);
        senderAccountId = accounts.createAccount();
        receiverAccountId = accounts.createAccount();
        nonExistingAccountId = NON_EXISTENT_ACCOUNT_ID;

        initialBalance = generateInitialBalance();
        accounts.deposit(senderAccountId, initialBalance);
    }

    @AfterEach
    void cleanup() {
        accounts.cleanup();
    }

    @ParameterizedTest(name = "Перевод {0} должен пройти успешно")
    @MethodSource("validTransferAmounts")
    void shouldSuccessfullyTransferMoney(double amount) {
        accounts.transfer(senderAccountId, receiverAccountId, amount)
                .expectSuccess();
    }

    private static Stream<Double> validTransferAmounts() {
        return Stream.of(
                MIN_TRANSFER,
                TRANSFER_AMOUNT_MEDIUM,
                TRANSFER_AMOUNT_LARGE,
                TRANSFER_AMOUNT_MAX
        );
    }

    @ParameterizedTest(name = "Сумма {0} должна быть отклонена")
    @MethodSource("invalidTransferAmounts")
    void shouldRejectInvalidAmounts(double amount) {
        accounts.transfer(senderAccountId, receiverAccountId, amount)
                .expectError(ERROR_INVALID_AMOUNT);
    }

    private static Stream<Double> invalidTransferAmounts() {
        return Stream.of(
                ZERO_AMOUNT,
                SMALL_NEGATIVE_AMOUNT,
                MEDIUM_NEGATIVE_AMOUNT,
                LARGE_NEGATIVE_AMOUNT
        );
    }

    @Test
    void shouldRejectTransferExceedingBalance() {
        double transferAmount = initialBalance + EXCEED_BALANCE_AMOUNT;

        accounts.transfer(senderAccountId, receiverAccountId, transferAmount)
                .expectError(ERROR_INSUFFICIENT_FUNDS);
    }

    @Test
    void shouldRejectTransferToNonExistingAccount() {
        accounts.transfer(senderAccountId, nonExistingAccountId, TRANSFER_AMOUNT_SMALL)
                .expectError(ERROR_ACCOUNT_NOT_FOUND);
    }

    private String login(String username, String password) {
        return new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK()
        ).post(
                new models.LoginUserRequest(username, password)
        ).extract().header("Authorization");
    }

    private double generateInitialBalance() {
        double balance = MIN_INITIAL_BALANCE +
                RANDOM.nextDouble() * (MAX_INITIAL_BALANCE - MIN_INITIAL_BALANCE);
        return Math.round(balance * PRECISION_MULTIPLIER) / PRECISION_MULTIPLIER;
    }
}