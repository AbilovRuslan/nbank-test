package iteration1.ui;

import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import requests.ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        List<CreateAccountResponse> accountsBefore = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();

        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> accountsAfter = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();

        assertThat(accountsAfter).hasSize(accountsBefore.size() + 1);
        assertThat(accountsAfter.getLast().getBalance()).isZero();
    }
}