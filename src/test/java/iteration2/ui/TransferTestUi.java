package iteration2.ui;

import models.CreateAccountResponse;
import models.CreateUserRequest;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import requests.ui.pages.BankAlert;
import requests.ui.pages.UserDashboard;

import java.util.List;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class TransferTestUi extends BaseUiTest {

    private List<CreateAccountResponse> getAccounts(CreateUserRequest user) {
        return new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
    }

    private CreateAccountResponse getSender(List<CreateAccountResponse> accounts) {
        assertThat(accounts).hasSize(2);
        return accounts.getFirst();
    }

    private CreateAccountResponse getReceiver(List<CreateAccountResponse> accounts) {
        assertThat(accounts).hasSize(2);
        return accounts.get(1);
    }

    private CreateAccountResponse getAccountByNumber(
            List<CreateAccountResponse> accounts,
            String accountNumber
    ) {
        return accounts.stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow();
    }

    private void createTwoAccounts(UserDashboard dashboard) {
        dashboard.open()
                .createNewAccount()
                .createNewAccount();
    }

    private UserDashboard depositToFirstAccount(
            UserDashboard dashboard,
            double amount
    ) {
        return dashboard.openDeposit()
                .selectFirstAccount()
                .enterAmount(amount)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.DEPOSIT_SUCCESSFUL.getMessage()
                );
    }

    @Test
    public void userCanTransferMoney() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        UserDashboard dashboard = new UserDashboard();

        createTwoAccounts(dashboard);

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = getSender(accounts);
        CreateAccountResponse receiver = getReceiver(accounts);

        depositToFirstAccount(dashboard, TRANSFER_AMOUNT_LARGE)
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(
                        BankAlert.TRANSFER_SUCCESSFUL.getMessage()
                );

        List<CreateAccountResponse> accountsAfter = getAccounts(user);

        CreateAccountResponse senderAfter =
                getAccountByNumber(accountsAfter, sender.getAccountNumber());
        CreateAccountResponse receiverAfter =
                getAccountByNumber(accountsAfter, receiver.getAccountNumber());

        assertThat(senderAfter.getBalance())
                .isCloseTo(TRANSFER_AMOUNT_LARGE - TRANSFER_AMOUNT_SMALL, within(DELTA));
        assertThat(receiverAfter.getBalance())
                .isCloseTo(TRANSFER_AMOUNT_SMALL, within(DELTA));
    }

    @Test
    public void shouldRejectTransferWithInsufficientFunds() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        UserDashboard dashboard = new UserDashboard();

        createTwoAccounts(dashboard);

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = getSender(accounts);
        CreateAccountResponse receiver = getReceiver(accounts);

        dashboard.openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(
                        BankAlert.INSUFFICIENT_FUNDS.getMessage()
                );

        List<CreateAccountResponse> accountsAfter = getAccounts(user);

        CreateAccountResponse senderAfter =
                getAccountByNumber(accountsAfter, sender.getAccountNumber());
        CreateAccountResponse receiverAfter =
                getAccountByNumber(accountsAfter, receiver.getAccountNumber());

        assertThat(senderAfter.getBalance()).isZero();
        assertThat(receiverAfter.getBalance()).isZero();
    }

    @Test
    public void shouldRejectTransferWithoutConfirmation() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        UserDashboard dashboard = new UserDashboard();

        createTwoAccounts(dashboard);

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = getSender(accounts);
        CreateAccountResponse receiver = getReceiver(accounts);

        depositToFirstAccount(dashboard, TRANSFER_AMOUNT_LARGE)
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .submitTransfer()
                .checkAlertMessageAndAccept(
                        BankAlert.CONFIRM_TRANSFER.getMessage()
                );

        List<CreateAccountResponse> accountsAfter = getAccounts(user);

        CreateAccountResponse senderAfter =
                getAccountByNumber(accountsAfter, sender.getAccountNumber());
        CreateAccountResponse receiverAfter =
                getAccountByNumber(accountsAfter, receiver.getAccountNumber());

        assertThat(senderAfter.getBalance())
                .isCloseTo(TRANSFER_AMOUNT_LARGE, within(DELTA));
        assertThat(receiverAfter.getBalance()).isZero();
    }

    @Test
    public void shouldRejectTransferWithEmptyAmount() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        UserDashboard dashboard = new UserDashboard();

        createTwoAccounts(dashboard);

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = getSender(accounts);
        CreateAccountResponse receiver = getReceiver(accounts);

        dashboard.openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(
                        BankAlert.FILL_ALL_FIELDS.getMessage()
                );

        List<CreateAccountResponse> accountsAfter = getAccounts(user);

        CreateAccountResponse senderAfter =
                getAccountByNumber(accountsAfter, sender.getAccountNumber());
        CreateAccountResponse receiverAfter =
                getAccountByNumber(accountsAfter, receiver.getAccountNumber());

        assertThat(senderAfter.getBalance()).isZero();
        assertThat(receiverAfter.getBalance()).isZero();
    }
}
