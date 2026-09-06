package iteration1.ui;

import common.annotations.UserSession;
import common.storage.SessionStorage;
import dao.AccountDao;
import requests.steps.DataBaseSteps;
import models.CreateAccountResponse;
import org.junit.jupiter.api.Test;
import requests.ui.pages.BankAlert;
import requests.ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();

        // БД-проверка: аккаунт существует и баланс 0
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccounts.getFirst().getAccountNumber());
        assertThat(accountDao).isNotNull();
        assertThat(accountDao.getBalance()).isZero();
    }
}