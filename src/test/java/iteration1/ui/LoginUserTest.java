package iteration1.ui;

import com.codeborne.selenide.Condition;
import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.ui.pages.AdminPanel;
import requests.ui.pages.LoginPage;
import requests.ui.pages.UserDashboard;

public class LoginUserTest extends BaseUiTest {
    @Test
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class).getAdminPanelText().shouldBe(Condition.visible);
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser();

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).getWelcomeText()
                .shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));
    }
}