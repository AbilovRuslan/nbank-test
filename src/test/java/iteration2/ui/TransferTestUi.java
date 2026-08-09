package iteration2.ui;

import iteration1.ui.BaseUiTest;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import requests.ui.pages.BankAlert;
import requests.ui.pages.UserDashboard;

import java.util.List;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class TransferTestUi extends BaseUiTest {

    private CreateUserRequest createUser() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        return user;
    }

    private List<CreateAccountResponse> getAccounts(CreateUserRequest user) {
        return new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
    }

    private CreateAccountResponse getAccountByNumber(List<CreateAccountResponse> accounts, String accountNumber) {
        return accounts.stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow();
    }

    private void createTwoAccounts() {
        new UserDashboard()
                .open()
                .createNewAccount()
                .createNewAccount();
    }

    private void makeDeposit() {
        new UserDashboard()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(TRANSFER_AMOUNT_LARGE)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());
    }

    @Test
    @DisplayName("User can transfer money")
    public void userCanTransferMoney() {
        CreateUserRequest user = createUser();
        createTwoAccounts();

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = accounts.getFirst();
        CreateAccountResponse receiver = accounts.get(1);

        makeDeposit();

        new UserDashboard()
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFUL.getMessage());

        List<CreateAccountResponse> accountsAfter = getAccounts(user);
        CreateAccountResponse senderAfter = getAccountByNumber(accountsAfter, sender.getAccountNumber());
        CreateAccountResponse receiverAfter = getAccountByNumber(accountsAfter, receiver.getAccountNumber());

        assertThat(senderAfter.getBalance())
                .isCloseTo(TRANSFER_AMOUNT_LARGE - TRANSFER_AMOUNT_SMALL, within(DELTA));
        assertThat(receiverAfter.getBalance())
                .isCloseTo(TRANSFER_AMOUNT_SMALL, within(DELTA));
    }

    @Test
    @DisplayName("Should reject transfer with insufficient funds")
    public void shouldRejectTransferWithInsufficientFunds() {
        CreateUserRequest user = createUser();
        createTwoAccounts();

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse receiver = accounts.get(1);

        new UserDashboard()
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(BankAlert.INSUFFICIENT_FUNDS.getMessage());

        List<CreateAccountResponse> accountsAfter = getAccounts(user);
        assertThat(accountsAfter.getFirst().getBalance()).isZero();
        assertThat(accountsAfter.get(1).getBalance()).isZero();
    }

    @Test
    @DisplayName("Should reject transfer without confirmation")
    public void shouldRejectTransferWithoutConfirmation() {
        CreateUserRequest user = createUser();
        createTwoAccounts();

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse receiver = accounts.get(1);

        makeDeposit();

        new UserDashboard()
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .submitTransfer()
                .checkAlertMessageAndAccept(BankAlert.CONFIRM_TRANSFER.getMessage());

        List<CreateAccountResponse> accountsAfter = getAccounts(user);
        assertThat(accountsAfter.getFirst().getBalance())
                .isCloseTo(TRANSFER_AMOUNT_LARGE, within(DELTA));
        assertThat(accountsAfter.get(1).getBalance()).isZero();
    }

    @Test
    @DisplayName("Should reject transfer with empty amount")
    public void shouldRejectTransferWithEmptyAmount() {
        CreateUserRequest user = createUser();
        createTwoAccounts();

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse receiver = accounts.get(1);

        new UserDashboard()
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS.getMessage());

        List<CreateAccountResponse> accountsAfter = getAccounts(user);
        assertThat(accountsAfter.getFirst().getBalance()).isZero();
        assertThat(accountsAfter.get(1).getBalance()).isZero();
    }
}