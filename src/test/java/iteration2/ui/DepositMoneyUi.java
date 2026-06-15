package iteration2.ui;

import iteration1.ui.BaseUiTest;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import requests.ui.pages.BankAlert;
import requests.ui.pages.UserDashboard;

import java.util.List;
import java.util.Random;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DepositMoneyUi extends BaseUiTest {

    private static final Random RANDOM = new Random();

    @Test
    public void userCanDepositMoney() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(TRANSFER_AMOUNT_MEDIUM)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.DEPOSIT_SUCCESSFUL.getMessage()
                );

        List<CreateAccountResponse> accounts =
                new UserSteps(user.getUsername(), user.getPassword())
                        .getAllAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance())
                .isCloseTo(TRANSFER_AMOUNT_MEDIUM, within(DELTA));
    }

    @Test
    public void userCanDepositMinimumAmount() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(MIN_VALID_DEPOSIT)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.DEPOSIT_SUCCESSFUL.getMessage()
                );

        List<CreateAccountResponse> accounts =
                new UserSteps(user.getUsername(), user.getPassword())
                        .getAllAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance())
                .isCloseTo(MIN_VALID_DEPOSIT, within(DELTA));
    }

    @Test
    public void userCanDepositMaximumAmount() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(MAX_DEPOSIT_LIMIT)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.DEPOSIT_SUCCESSFUL.getMessage()
                );

        List<CreateAccountResponse> accounts =
                new UserSteps(user.getUsername(), user.getPassword())
                        .getAllAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance())
                .isCloseTo(MAX_DEPOSIT_LIMIT, within(DELTA));
    }

    @RepeatedTest(RANDOM_TEST_REPETITIONS)
    public void userCanDepositRandomAmount() {
        double amount = MIN_VALID_DEPOSIT + (MAX_DEPOSIT_LIMIT - MIN_VALID_DEPOSIT) * RANDOM.nextDouble();
        amount = Math.round(amount * PRECISION_MULTIPLIER) / PRECISION_MULTIPLIER;

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(amount)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.DEPOSIT_SUCCESSFUL.getMessage()
                );

        List<CreateAccountResponse> accounts =
                new UserSteps(user.getUsername(), user.getPassword())
                        .getAllAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance())
                .isCloseTo(amount, within(DELTA));
    }

    @Test
    public void userCanMakeMultipleDeposits() {
        double firstAmount = TRANSFER_AMOUNT_LARGE;
        double secondAmount = TRANSFER_AMOUNT_MEDIUM;
        double expectedBalance = firstAmount + secondAmount;

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        UserDashboard dashboard = new UserDashboard();

        dashboard.open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(firstAmount)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.DEPOSIT_SUCCESSFUL.getMessage()
                );

        dashboard.openDeposit()
                .selectFirstAccount()
                .enterAmount(secondAmount)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.DEPOSIT_SUCCESSFUL.getMessage()
                );

        List<CreateAccountResponse> accounts =
                new UserSteps(user.getUsername(), user.getPassword())
                        .getAllAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance())
                .isCloseTo(expectedBalance, within(DELTA));
    }

    @Test
    public void shouldRejectEmptyDepositAmount() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.INVALID_DEPOSIT_AMOUNT.getMessage()
                );

        List<CreateAccountResponse> accounts =
                new UserSteps(user.getUsername(), user.getPassword())
                        .getAllAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isZero();
    }

    @Test
    public void shouldRejectNegativeDepositAmount() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(SMALL_NEGATIVE_AMOUNT)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.INVALID_DEPOSIT_AMOUNT.getMessage()
                );

        List<CreateAccountResponse> accounts =
                new UserSteps(user.getUsername(), user.getPassword())
                        .getAllAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isZero();
    }

    @Test
    public void shouldRejectDepositExceedingLimit() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(FAR_ABOVE_LIMIT)
                .submitDeposit()
                .checkAlertMessageAndAccept(
                        BankAlert.DEPOSIT_EXCEEDS_LIMIT.getMessage()
                );

        List<CreateAccountResponse> accounts =
                new UserSteps(user.getUsername(), user.getPassword())
                        .getAllAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isZero();
    }
}