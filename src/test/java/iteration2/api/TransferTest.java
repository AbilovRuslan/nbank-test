package iteration2.api;

import dao.AccountDao;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.DataBaseSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Переводы между счетами")
public class TransferTest {

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

    @ParameterizedTest(name = "Перевод {0} должен пройти успешно")
    @MethodSource("validTransferAmounts")
    @DisplayName("Валидные переводы")
    void shouldSuccessfullyTransferMoney(double amount) {
        AccountInfoResponse sender = createAccount();
        AccountInfoResponse receiver = createAccount();

        deposit(sender.getId(), TRANSFER_AMOUNT_LARGE);

        transfer(sender.getId(), receiver.getId(), amount);

        AccountDao senderDao = DataBaseSteps.getAccountById(sender.getId());
        AccountDao receiverDao = DataBaseSteps.getAccountById(receiver.getId());

        assertThat(senderDao.getBalance())
                .isCloseTo(TRANSFER_AMOUNT_LARGE - amount, within(DELTA));
        assertThat(receiverDao.getBalance())
                .isCloseTo(amount, within(DELTA));
    }

    @ParameterizedTest(name = "Сумма {0} должна быть отклонена: {1}")
    @MethodSource("invalidTransferAmounts")
    @DisplayName("Невалидные суммы переводов")
    void shouldRejectInvalidAmounts(double amount, String expectedError) {
        AccountInfoResponse sender = createAccount();
        AccountInfoResponse receiver = createAccount();

        deposit(sender.getId(), TRANSFER_AMOUNT_LARGE);

        String errorResponse = transferAndGetError(sender.getId(), receiver.getId(), amount);
        assertThat(errorResponse).contains(expectedError);

        AccountDao senderDao = DataBaseSteps.getAccountById(sender.getId());
        AccountDao receiverDao = DataBaseSteps.getAccountById(receiver.getId());

        assertThat(senderDao.getBalance()).isCloseTo(TRANSFER_AMOUNT_LARGE, within(DELTA));
        assertThat(receiverDao.getBalance()).isZero();
    }

    @Test
    @DisplayName("Отказ при превышении баланса")
    void shouldRejectTransferExceedingBalance() {
        AccountInfoResponse sender = createAccount();
        AccountInfoResponse receiver = createAccount();

        deposit(sender.getId(), TRANSFER_AMOUNT_LARGE);

        double transferAmount = TRANSFER_AMOUNT_LARGE + EXCEED_BALANCE_AMOUNT;
        String errorResponse = transferAndGetError(sender.getId(), receiver.getId(), transferAmount);
        assertThat(errorResponse).contains(ERROR_INSUFFICIENT_FUNDS);

        AccountDao senderDao = DataBaseSteps.getAccountById(sender.getId());
        AccountDao receiverDao = DataBaseSteps.getAccountById(receiver.getId());

        assertThat(senderDao.getBalance()).isCloseTo(TRANSFER_AMOUNT_LARGE, within(DELTA));
        assertThat(receiverDao.getBalance()).isZero();
    }

    @Test
    @DisplayName("Отказ при переводе на несуществующий счёт")
    void shouldRejectTransferToNonExistingAccount() {
        AccountInfoResponse sender = createAccount();

        deposit(sender.getId(), TRANSFER_AMOUNT_LARGE);

        String errorResponse = transferAndGetError(sender.getId(), NON_EXISTENT_ACCOUNT_ID, TRANSFER_AMOUNT_SMALL);
        assertThat(errorResponse).contains(ERROR_ACCOUNT_NOT_FOUND);

        AccountDao senderDao = DataBaseSteps.getAccountById(sender.getId());
        assertThat(senderDao.getBalance()).isCloseTo(TRANSFER_AMOUNT_LARGE, within(DELTA));

        AccountDao nonExistentDao = DataBaseSteps.getAccountById(NON_EXISTENT_ACCOUNT_ID);
        assertThat(nonExistentDao).isNull();
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

    private void transfer(Long senderId, Long receiverId, double amount) {
        new CrudRequester(
                RequestSpecs.authSpec(authToken),
                Endpoint.TRANSFER,
                ResponseSpecs.transferWasSuccessful()
        ).post(TransferMoneyRequest.builder()
                .fromAccountId(senderId)
                .toAccountId(receiverId)
                .amount(amount)
                .build());
    }

    private String transferAndGetError(Long senderId, Long receiverId, double amount) {
        return new CrudRequester(
                RequestSpecs.authSpec(authToken),
                Endpoint.TRANSFER,
                ResponseSpecs.badRequest()
        ).post(TransferMoneyRequest.builder()
                        .fromAccountId(senderId)
                        .toAccountId(receiverId)
                        .amount(amount)
                        .build())
                .extract()
                .body()
                .asString();
    }

    private static Stream<Arguments> validTransferAmounts() {
        return Stream.of(
                Arguments.of(MIN_TRANSFER),
                Arguments.of(TRANSFER_AMOUNT_MEDIUM),
                Arguments.of(TRANSFER_AMOUNT_LARGE)
        );
    }

    private static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(ZERO_AMOUNT, ERROR_INVALID_AMOUNT),
                Arguments.of(SMALL_NEGATIVE_AMOUNT, ERROR_INVALID_AMOUNT),
                Arguments.of(MEDIUM_NEGATIVE_AMOUNT, ERROR_INVALID_AMOUNT),
                Arguments.of(LARGE_NEGATIVE_AMOUNT, ERROR_INVALID_AMOUNT)
        );
    }

    private static org.assertj.core.data.Offset<Double> within(double epsilon) {
        return org.assertj.core.data.Offset.offset(epsilon);
    }
}