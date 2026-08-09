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

    private CreateUserRequest user;
    private CreateAccountResponse senderAccount;
    private CreateAccountResponse receiverAccount;

    private void setupUserAndAccounts() {
        user = AdminSteps.createUser();
        authAsUser(user);
        createTwoAccounts();
        List<CreateAccountResponse> accounts = getAccounts();
        senderAccount = accounts.getFirst();
        receiverAccount = accounts.get(1);
    }

    private void createTwoAccounts() {
        new UserDashboard()
                .open()
                .createNewAccount()
                .createNewAccount();
    }

    private void makeDeposit(double amount) {
        new UserDashboard()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(amount)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());
    }

    private UserDashboard fillTransferForm() {
        return new UserDashboard()
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiverAccount.getAccountNumber());
    }

    private List<CreateAccountResponse> getAccounts() {
        return new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
    }

    private CreateAccountResponse getAccountByNumber(List<CreateAccountResponse> accounts, String accountNumber) {
        return accounts.stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow();
    }

    private void assertBalances(double senderBalance, double receiverBalance) {
        List<CreateAccountResponse> accountsAfter = getAccounts();
        CreateAccountResponse senderAfter = getAccountByNumber(accountsAfter, senderAccount.getAccountNumber());
        CreateAccountResponse receiverAfter = getAccountByNumber(accountsAfter, receiverAccount.getAccountNumber());

        assertThat(senderAfter.getBalance()).isCloseTo(senderBalance, within(DELTA));
        assertThat(receiverAfter.getBalance()).isCloseTo(receiverBalance, within(DELTA));
    }

    @Test
    @DisplayName("User can transfer money")
    public void userCanTransferMoney() {
        setupUserAndAccounts();
        makeDeposit(TRANSFER_AMOUNT_LARGE);

        fillTransferForm()
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFUL.getMessage());

        assertBalances(TRANSFER_AMOUNT_LARGE - TRANSFER_AMOUNT_SMALL, TRANSFER_AMOUNT_SMALL);
    }

    @Test
    @DisplayName("Should reject transfer with insufficient funds")
    public void shouldRejectTransferWithInsufficientFunds() {
        setupUserAndAccounts();

        fillTransferForm()
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .confirm()
                .submitTransfer();

        assertBalances(0.0, 0.0);
    }

    @Test
    @DisplayName("Should reject transfer without confirmation")
    public void shouldRejectTransferWithoutConfirmation() {
        setupUserAndAccounts();
        makeDeposit(TRANSFER_AMOUNT_LARGE);

        fillTransferForm()
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .submitTransfer();

        assertBalances(TRANSFER_AMOUNT_LARGE, 0.0);
    }

    @Test
    @DisplayName("Should reject transfer with empty amount")
    public void shouldRejectTransferWithEmptyAmount() {
        setupUserAndAccounts();

        fillTransferForm()
                .confirm()
                .submitTransfer();

        assertBalances(0.0, 0.0);
    }
}