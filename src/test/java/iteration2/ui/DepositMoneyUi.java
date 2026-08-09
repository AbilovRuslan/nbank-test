package iteration2.ui;

import iteration1.ui.BaseUiTest;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import requests.ui.pages.UserDashboard;

import java.util.List;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DepositMoneyUi extends BaseUiTest {

    private CreateUserRequest user;

    private UserDashboard prepareAccountForDeposit() {
        user = AdminSteps.createUser();
        authAsUser(user);
        return new UserDashboard()
                .open()
                .createNewAccount()
                .openDeposit()
                .selectFirstAccount();
    }

    private UserDashboard openDepositForExistingAccount() {
        return new UserDashboard()
                .openDeposit()
                .selectFirstAccount();
    }

    private void assertSingleAccountBalance(double expectedBalance) {
        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getBalance()).isCloseTo(expectedBalance, within(DELTA));
    }

    @Test
    @DisplayName("User can deposit money")
    public void userCanDepositMoney() {
        double amount = TRANSFER_AMOUNT_MEDIUM;

        prepareAccountForDeposit()
                .enterAmount(amount)
                .submitDeposit();

        assertSingleAccountBalance(amount);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 100.0, 5000.0})
    @DisplayName("User can deposit valid amounts")
    public void userCanDepositValidAmounts(double amount) {
        prepareAccountForDeposit()
                .enterAmount(amount)
                .submitDeposit();

        assertSingleAccountBalance(amount);
    }

    @Test
    @DisplayName("User can make multiple deposits")
    public void userCanMakeMultipleDeposits() {
        double firstAmount = TRANSFER_AMOUNT_LARGE;
        double secondAmount = TRANSFER_AMOUNT_MEDIUM;
        double expectedBalance = firstAmount + secondAmount;

        prepareAccountForDeposit()
                .enterAmount(firstAmount)
                .submitDeposit();

        openDepositForExistingAccount()
                .enterAmount(secondAmount)
                .submitDeposit();

        assertSingleAccountBalance(expectedBalance);
    }

    @Test
    @DisplayName("Should reject empty deposit amount")
    public void shouldRejectEmptyDepositAmount() {
        prepareAccountForDeposit()
                .submitDeposit();

        assertSingleAccountBalance(0.0);
    }

    @Test
    @DisplayName("Should reject negative deposit amount")
    public void shouldRejectNegativeDepositAmount() {
        prepareAccountForDeposit()
                .enterAmount(SMALL_NEGATIVE_AMOUNT)
                .submitDeposit();

        assertSingleAccountBalance(0.0);
    }

    @Test
    @DisplayName("Should reject deposit exceeding limit")
    public void shouldRejectDepositExceedingLimit() {
        prepareAccountForDeposit()
                .enterAmount(FAR_ABOVE_LIMIT)
                .submitDeposit();

        assertSingleAccountBalance(0.0);
    }
}