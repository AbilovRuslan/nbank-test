package iteration2;

import iteration2.fixtures.TestAccounts;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.LoginUserRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Random;
import java.util.stream.Stream;

import static iteration2.fixtures.TestAccounts.MIN_TRANSFER;

@DisplayName("Переводы между счетами — Senior Level")
public class TransferTest {

    private static final Random RANDOM = new Random();

    private String authToken;
    private TestAccounts accounts;
    private Long senderAccountId;
    private Long receiverAccountId;
    private static Double initialBalance;

    @BeforeEach
    void setup() {
        var user = AdminSteps.createUser();
        authToken = login(user.getUsername(), user.getPassword());

        accounts = new TestAccounts(authToken);
        senderAccountId = accounts.createAccount();
        receiverAccountId = accounts.createAccount();

        initialBalance = generateInitialBalance();
        accounts.deposit(senderAccountId, initialBalance);
    }

    @AfterEach
    void cleanup() {
        accounts.cleanup();
    }

    // ====================== ПОЗИТИВНЫЕ СЦЕНАРИИ ======================

    @ParameterizedTest(name = "Перевод {0} должен пройти успешно")
    @MethodSource("validTransferAmounts")
    void shouldSuccessfullyTransferMoney(double amount) {
        accounts.transfer(senderAccountId, receiverAccountId, amount)
                .expectSuccess();
    }

    private static Stream<Double> validTransferAmounts() {
        return Stream.of(
                MIN_TRANSFER,
                100.50,
                initialBalance * 0.3,
                initialBalance * 0.7
        );
    }

    // ====================== НЕГАТИВНЫЕ СЦЕНАРИИ ======================

    @ParameterizedTest(name = "Сумма {0} должна быть отклонена")
    @ValueSource(doubles = {0.0, -0.01, -100.0, -999.99})
    void shouldRejectInvalidAmounts(double amount) {
        accounts.transfer(senderAccountId, receiverAccountId, amount)
                .expectError("Transfer amount must be at least 0.01");
    }

    @Test
    void shouldRejectTransferExceedingBalance() {
        double transferAmount = initialBalance + 100.0;
        accounts.transfer(senderAccountId, receiverAccountId, transferAmount)
                .expectError("insufficient funds");
    }

    @Test
    void shouldRejectTransferToNonExistingAccount() {
        long nonExistingAccount = 999999L;
        accounts.transfer(senderAccountId, nonExistingAccount, 10.0)
                .expectError("Invalid transfer");
    }

    // ====================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ======================

    private String login(String username, String password) {
        return new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK()
        ).post(
                new models.LoginUserRequest(username, password)
        ).extract().header("Authorization");
    }

    private double generateInitialBalance() {
        double balance = 500.0 + RANDOM.nextDouble() * 4500.0;
        return Math.round(balance * 100.0) / 100.0;
    }
}