package iteration2.ui;

import models.CreateUserRequest;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import requests.ui.pages.BankAlert;
import requests.ui.pages.UserDashboard;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UsernameUpdateUi extends BaseUiTest {

    private UserDashboard openProfile(CreateUserRequest user) {
        authAsUser(user);
        return new UserDashboard()
                .open()
                .openEditProfile();
    }

    @Test
    public void userCanUpdateName() {
        CreateUserRequest user = AdminSteps.createUser();

        openProfile(user)
                .enterNewName(VALID_NAME_TWO_WORDS)
                .saveChanges();

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.NAME_UPDATED.getMessage()
        );

        String actualName = new UserSteps(user.getUsername(), user.getPassword())
                .getProfile()
                .getName();

        assertThat(actualName).isEqualTo(VALID_NAME_TWO_WORDS);
    }

    @Test
    public void shouldRejectEmptyName() {
        CreateUserRequest user = AdminSteps.createUser();

        openProfile(user)
                .enterNewName(INVALID_NAME_EMPTY)
                .saveChanges();

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.VALID_NAME_REQUIRED.getMessage()
        );

        String actualName = new UserSteps(user.getUsername(), user.getPassword())
                .getProfile()
                .getName();

        assertThat(actualName).isNull();
    }

    @Test
    public void shouldRejectOneWordName() {
        CreateUserRequest user = AdminSteps.createUser();

        openProfile(user)
                .enterNewName(INVALID_NAME_ONE_WORD)
                .saveChanges();

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.INVALID_NAME.getMessage()
        );

        String actualName = new UserSteps(user.getUsername(), user.getPassword())
                .getProfile()
                .getName();

        assertThat(actualName).isNull();
    }

    @Test
    public void shouldRejectNameWithSpecialChars() {
        CreateUserRequest user = AdminSteps.createUser();

        openProfile(user)
                .enterNewName(INVALID_NAME_SPECIAL_CHARS)
                .saveChanges();

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.INVALID_NAME.getMessage()
        );

        String actualName = new UserSteps(user.getUsername(), user.getPassword())
                .getProfile()
                .getName();

        assertThat(actualName).isNull();
    }

    @Test
    public void shouldRejectNameWithNumbers() {
        CreateUserRequest user = AdminSteps.createUser();

        openProfile(user)
                .enterNewName(INVALID_NAME_NUMBERS)
                .saveChanges();

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.INVALID_NAME.getMessage()
        );

        String actualName = new UserSteps(user.getUsername(), user.getPassword())
                .getProfile()
                .getName();

        assertThat(actualName).isNull();
    }

    @Test
    public void shouldRejectNameWithOnlySpaces() {
        CreateUserRequest user = AdminSteps.createUser();

        openProfile(user)
                .enterNewName(INVALID_NAME_SPACES)
                .saveChanges();

        new UserDashboard().checkAlertMessageAndAccept(
                BankAlert.VALID_NAME_REQUIRED.getMessage()
        );

        String actualName = new UserSteps(user.getUsername(), user.getPassword())
                .getProfile()
                .getName();

        assertThat(actualName).isNull();
    }
}