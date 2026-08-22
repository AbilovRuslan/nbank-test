package iteration1.ui;

import com.codeborne.selenide.Condition;
import dao.UserDao;
import requests.steps.DataBaseSteps;
import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.ui.pages.AdminPanel;
import requests.ui.pages.LoginPage;
import requests.ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginUserTest extends BaseUiTest {
    @Test
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class).getAdminPanelText().shouldBe(Condition.visible);

        // БД-проверка: админ существует
        UserDao adminDao = DataBaseSteps.getUserByUsername(admin.getUsername());
        assertThat(adminDao).isNotNull();
        assertThat(adminDao.getRole()).isEqualTo("ADMIN");
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser();

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).getWelcomeText()
                .shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // БД-проверка: пользователь существует
        UserDao userDao = DataBaseSteps.getUserByUsername(user.getUsername());
        assertThat(userDao).isNotNull();
        assertThat(userDao.getUsername()).isEqualTo(user.getUsername());
    }
}