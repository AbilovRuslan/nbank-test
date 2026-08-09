package iteration2.api;

import models.AccountInfoResponse;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.LoginUserRequest;
import models.TransferMoneyRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.LoginUserRequester;
import requests.TransferRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Переводы между счетами")
public class TransferTest {

    private String authToken;
    private Long senderAccountId;
    private Long receiverAccountId;

    @BeforeEach
    void setup() {
        CreateUserRequest userRequest = AdminSteps.createUser();

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

        senderAccountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated()
        ).post()
                .extract()
                .as(AccountInfoResponse.class)
                .getId();

        receiverAccountId = new CreateAccountRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.entityWasCreated()
        ).post()
                .extract()
                .as(AccountInfoResponse.class)
                .getId();

        new DepositRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.balanceWasUpdated()
        ).post(DepositMoneyRequest.builder()
                .id(senderAccountId)
                .balance(TRANSFER_AMOUNT_LARGE)
                .build());
    }

    @ParameterizedTest(name = "Перевод {0} должен пройти успешно")
    @MethodSource("validTransferAmounts")
    @DisplayName("Валидные переводы")
    void shouldSuccessfullyTransferMoney(double amount) {
        new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.transferWasSuccessful()
        ).post(buildRequest(senderAccountId, receiverAccountId, amount));
    }

    @ParameterizedTest(name = "Сумма {0} должна быть отклонена: {1}")
    @MethodSource("invalidTransferAmounts")
    @DisplayName("Невалидные суммы переводов")
    void shouldRejectInvalidAmounts(double amount, String expectedError) {
        String errorResponse = new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(buildRequest(senderAccountId, receiverAccountId, amount))
                .extract()
                .asString();

        assertThat(errorResponse).contains(expectedError);
    }

    @Test
    @DisplayName("Отказ при превышении баланса")
    void shouldRejectTransferExceedingBalance() {
        double transferAmount = TRANSFER_AMOUNT_LARGE + EXCEED_BALANCE_AMOUNT;

        String errorResponse = new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(buildRequest(senderAccountId, receiverAccountId, transferAmount))
                .extract()
                .asString();

        assertThat(errorResponse).contains(ERROR_INSUFFICIENT_FUNDS);
    }

    @Test
    @DisplayName("Отказ при переводе на несуществующий счёт")
    void shouldRejectTransferToNonExistingAccount() {
        String errorResponse = new TransferRequester(
                RequestSpecs.authSpec(authToken),
                ResponseSpecs.badRequest()
        ).post(buildRequest(senderAccountId, NON_EXISTENT_ACCOUNT_ID, TRANSFER_AMOUNT_SMALL))
                .extract()
                .asString();

        assertThat(errorResponse).contains(ERROR_ACCOUNT_NOT_FOUND);
    }

    // ================= HELPER METHODS =================

    private TransferMoneyRequest buildRequest(Long from, Long to, double amount) {
        return TransferMoneyRequest.builder()
                .fromAccountId(from)
                .toAccountId(to)
                .amount(amount)
                .build();
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
}