package iteration2.ui;

import dao.AccountDao;
import requests.steps.DataBaseSteps;
import iteration1.ui.BaseUiTest;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import requests.ui.pages.BankAlert;
import requests.ui.pages.UserDashboard;

import java.util.List;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyUi extends BaseUiTest {

    private CreateUserRequest createUser() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        return user;
    }

    private List<CreateAccountResponse> getAccounts(CreateUserRequest user) {
        return new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();
    }

    @Test
    @DisplayName("User can deposit money")
    public void userCanDepositMoney() {
        CreateUserRequest user = createUser();
        double amount = TRANSFER_AMOUNT_MEDIUM;

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(amount)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());

        List<CreateAccountResponse> accounts = getAccounts(user);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isCloseTo(amount, within(DELTA));

        // БД-проверка
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accounts.getFirst().getAccountNumber());
        assertThat(accountDao.getBalance()).isCloseTo(amount, within(DELTA));
    }

    @ParameterizedTest
    @ValueSource(doubles = {MIN_VALID_DEPOSIT, TRANSFER_AMOUNT_SMALL, MAX_DEPOSIT_LIMIT})
    @DisplayName("User can deposit valid amounts")
    public void userCanDepositValidAmounts(double amount) {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(amount)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());

        List<CreateAccountResponse> accounts = getAccounts(user);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isCloseTo(amount, within(DELTA));

        // БД-проверка
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accounts.getFirst().getAccountNumber());
        assertThat(accountDao.getBalance()).isCloseTo(amount, within(DELTA));
    }

    @Test
    @DisplayName("User can make multiple deposits")
    public void userCanMakeMultipleDeposits() {
        CreateUserRequest user = createUser();
        double firstAmount = TRANSFER_AMOUNT_LARGE;
        double secondAmount = TRANSFER_AMOUNT_MEDIUM;
        double expectedBalance = firstAmount + secondAmount;

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(firstAmount)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());

        new UserDashboard()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(secondAmount)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFUL.getMessage());

        List<CreateAccountResponse> accounts = getAccounts(user);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isCloseTo(expectedBalance, within(DELTA));

        // БД-проверка
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accounts.getFirst().getAccountNumber());
        assertThat(accountDao.getBalance()).isCloseTo(expectedBalance, within(DELTA));
    }

    @Test
    @DisplayName("Should reject empty deposit amount")
    public void shouldRejectEmptyDepositAmount() {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.INVALID_DEPOSIT_AMOUNT.getMessage());

        List<CreateAccountResponse> accounts = getAccounts(user);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isZero();

        // БД-проверка
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accounts.getFirst().getAccountNumber());
        assertThat(accountDao.getBalance()).isZero();
    }

    @Test
    @DisplayName("Should reject negative deposit amount")
    public void shouldRejectNegativeDepositAmount() {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(SMALL_NEGATIVE_AMOUNT)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.INVALID_DEPOSIT_AMOUNT.getMessage());

        List<CreateAccountResponse> accounts = getAccounts(user);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isZero();

        // БД-проверка
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accounts.getFirst().getAccountNumber());
        assertThat(accountDao.getBalance()).isZero();
    }

    @Test
    @DisplayName("Should reject deposit exceeding limit")
    public void shouldRejectDepositExceedingLimit() {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(FAR_ABOVE_LIMIT)
                .submitDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_EXCEEDS_LIMIT.getMessage());

        List<CreateAccountResponse> accounts = getAccounts(user);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isZero();

        // БД-проверка
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accounts.getFirst().getAccountNumber());
        assertThat(accountDao.getBalance()).isZero();
    }

    private static org.assertj.core.data.Offset<Double> within(double epsilon) {
        return org.assertj.core.data.Offset.offset(epsilon);
    }
}