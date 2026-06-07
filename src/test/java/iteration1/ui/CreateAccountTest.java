package iteration1.ui;

import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import requests.ui.pages.BankAlert;
import requests.ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}