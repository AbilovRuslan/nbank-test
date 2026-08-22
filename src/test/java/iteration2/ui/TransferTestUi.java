package iteration2.ui;

import dao.AccountDao;
import requests.steps.DataBaseSteps;
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

public class TransferTestUi extends BaseUiTest {

    private CreateUserRequest createUser() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        return user;
    }

    private List<CreateAccountResponse> getAccounts(CreateUserRequest user) {
        return new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
    }

    @Test
    @DisplayName("User can transfer money")
    public void userCanTransferMoney() {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .createNewAccount()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(TRANSFER_AMOUNT_LARGE)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = accounts.getFirst();
        CreateAccountResponse receiver = accounts.get(1);

        new UserDashboard()
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFUL.getMessage());

        // БД-проверка отправителя
        AccountDao senderDao = DataBaseSteps.getAccountByAccountNumber(sender.getAccountNumber());
        assertThat(senderDao.getBalance())
                .isCloseTo(TRANSFER_AMOUNT_LARGE - TRANSFER_AMOUNT_SMALL, within(DELTA));

        // БД-проверка получателя
        AccountDao receiverDao = DataBaseSteps.getAccountByAccountNumber(receiver.getAccountNumber());
        assertThat(receiverDao.getBalance())
                .isCloseTo(TRANSFER_AMOUNT_SMALL, within(DELTA));
    }

    @Test
    @DisplayName("Should reject transfer with insufficient funds")
    public void shouldRejectTransferWithInsufficientFunds() {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .createNewAccount()
                .createNewAccount();

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = accounts.getFirst();
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

        // БД-проверка: балансы не изменились
        AccountDao senderDao = DataBaseSteps.getAccountByAccountNumber(sender.getAccountNumber());
        AccountDao receiverDao = DataBaseSteps.getAccountByAccountNumber(receiver.getAccountNumber());

        assertThat(senderDao.getBalance()).isZero();
        assertThat(receiverDao.getBalance()).isZero();
    }

    @Test
    @DisplayName("Should reject transfer without confirmation")
    public void shouldRejectTransferWithoutConfirmation() {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .createNewAccount()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(TRANSFER_AMOUNT_LARGE)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = accounts.getFirst();
        CreateAccountResponse receiver = accounts.get(1);

        new UserDashboard()
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .enterTransferAmount(TRANSFER_AMOUNT_SMALL)
                .submitTransfer()
                .checkAlertMessageAndAccept(BankAlert.CONFIRM_TRANSFER.getMessage());

        // БД-проверка: балансы не изменились
        AccountDao senderDao = DataBaseSteps.getAccountByAccountNumber(sender.getAccountNumber());
        AccountDao receiverDao = DataBaseSteps.getAccountByAccountNumber(receiver.getAccountNumber());

        assertThat(senderDao.getBalance()).isCloseTo(TRANSFER_AMOUNT_LARGE, within(DELTA));
        assertThat(receiverDao.getBalance()).isZero();
    }

    @Test
    @DisplayName("Should reject transfer with empty amount")
    public void shouldRejectTransferWithEmptyAmount() {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .createNewAccount()
                .createNewAccount();

        List<CreateAccountResponse> accounts = getAccounts(user);
        CreateAccountResponse sender = accounts.getFirst();
        CreateAccountResponse receiver = accounts.get(1);

        new UserDashboard()
                .openTransfer()
                .selectFirstAccount()
                .enterRecipientName(user.getUsername())
                .enterRecipientAccount(receiver.getAccountNumber())
                .confirm()
                .submitTransfer()
                .checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS.getMessage());

        // БД-проверка: балансы не изменились
        AccountDao senderDao = DataBaseSteps.getAccountByAccountNumber(sender.getAccountNumber());
        AccountDao receiverDao = DataBaseSteps.getAccountByAccountNumber(receiver.getAccountNumber());

        assertThat(senderDao.getBalance()).isZero();
        assertThat(receiverDao.getBalance()).isZero();
    }

    private static org.assertj.core.data.Offset<Double> within(double epsilon) {
        return org.assertj.core.data.Offset.offset(epsilon);
    }
}