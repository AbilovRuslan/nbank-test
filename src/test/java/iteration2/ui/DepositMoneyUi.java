package iteration2.ui;

import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.UserSteps;
import requests.ui.pages.UserDashboard;

import java.util.List;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DepositMoneyUi extends BaseUiTest {

    private CreateUserRequest currentUser() {
        return SessionStorage.getUser(0);
    }

    private void createAccount() {
        new UserDashboard()
                .open()
                .createNewAccount();
    }

    private void makeDeposit(double amount) {
        new UserDashboard()
                .openDeposit()
                .selectFirstAccount()
                .enterAmount(amount)
                .submitDeposit();
    }

    private double getSingleAccountBalance(CreateUserRequest user) {
        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(accounts).hasSize(1);
        return accounts.getFirst().getBalance();
    }

    @Test
    @UserSession
    @DisplayName("User can deposit money")
    public void userCanDepositMoney() {
        CreateUserRequest user = currentUser();
        createAccount();
        double amount = TRANSFER_AMOUNT_MEDIUM;

        makeDeposit(amount);

        assertThat(getSingleAccountBalance(user))
                .isCloseTo(amount, within(DELTA));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 100.0, 5000.0})
    @UserSession
    @DisplayName("User can deposit valid amounts")
    public void userCanDepositValidAmounts(double amount) {
        CreateUserRequest user = currentUser();
        createAccount();

        makeDeposit(amount);

        assertThat(getSingleAccountBalance(user))
                .isCloseTo(amount, within(DELTA));
    }

    @Test
    @UserSession
    @DisplayName("User can make multiple deposits")
    public void userCanMakeMultipleDeposits() {
        CreateUserRequest user = currentUser();
        createAccount();
        double firstAmount = TRANSFER_AMOUNT_LARGE;
        double secondAmount = TRANSFER_AMOUNT_MEDIUM;
        double expectedBalance = firstAmount + secondAmount;

        makeDeposit(firstAmount);
        makeDeposit(secondAmount);

        assertThat(getSingleAccountBalance(user))
                .isCloseTo(expectedBalance, within(DELTA));
    }

    @Test
    @UserSession
    @DisplayName("Should reject empty deposit amount")
    public void shouldRejectEmptyDepositAmount() {
        CreateUserRequest user = currentUser();
        createAccount();

        new UserDashboard()
                .openDeposit()
                .selectFirstAccount()
                .submitDeposit();

        assertThat(getSingleAccountBalance(user)).isZero();
    }

    @Test
    @UserSession
    @DisplayName("Should reject negative deposit amount")
    public void shouldRejectNegativeDepositAmount() {
        CreateUserRequest user = currentUser();
        createAccount();

        makeDeposit(SMALL_NEGATIVE_AMOUNT);

        assertThat(getSingleAccountBalance(user)).isZero();
    }

    @Test
    @UserSession
    @DisplayName("Should reject deposit exceeding limit")
    public void shouldRejectDepositExceedingLimit() {
        CreateUserRequest user = currentUser();
        createAccount();

        makeDeposit(FAR_ABOVE_LIMIT);

        assertThat(getSingleAccountBalance(user)).isZero();
    }
}