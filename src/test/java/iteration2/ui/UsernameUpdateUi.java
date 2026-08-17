package iteration2.ui;

import iteration1.ui.BaseUiTest;
import models.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import requests.ui.pages.BankAlert;
import requests.ui.pages.UserDashboard;

import java.util.stream.Stream;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UsernameUpdateUi extends BaseUiTest {

    private CreateUserRequest createUser() {
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);
        return user;
    }

    private String getActualName(CreateUserRequest user) {
        return new UserSteps(user.getUsername(), user.getPassword())
                .getProfile()
                .getName();
    }

    @Test
    @DisplayName("User can update name")
    public void userCanUpdateName() {
        CreateUserRequest user = createUser();

        new UserDashboard()
                .open()
                .openEditProfile()
                .enterNewName(VALID_NAME_TWO_WORDS)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED.getMessage());

        assertThat(getActualName(user)).isEqualTo(VALID_NAME_TWO_WORDS);
    }

    @ParameterizedTest
    @MethodSource("invalidRequiredNames")
    @DisplayName("Should reject empty or whitespace names")
    public void shouldRejectEmptyOrWhitespaceNames(String name, BankAlert expectedAlert) {
        CreateUserRequest user = createUser();
        String nameBefore = getActualName(user);

        new UserDashboard()
                .open()
                .openEditProfile()
                .enterNewName(name)
                .saveChanges()
                .checkAlertMessageAndAccept(expectedAlert.getMessage());

        assertThat(getActualName(user)).isEqualTo(nameBefore);
    }

    @ParameterizedTest
    @MethodSource("invalidFormatNames")
    @DisplayName("Should reject names with invalid format")
    public void shouldRejectInvalidFormatNames(String name, BankAlert expectedAlert) {
        CreateUserRequest user = createUser();
        String nameBefore = getActualName(user);

        new UserDashboard()
                .open()
                .openEditProfile()
                .enterNewName(name)
                .saveChanges()
                .checkAlertMessageAndAccept(expectedAlert.getMessage());

        assertThat(getActualName(user)).isEqualTo(nameBefore);
    }

    static Stream<Arguments> invalidRequiredNames() {
        return Stream.of(
                Arguments.of(INVALID_NAME_EMPTY, BankAlert.VALID_NAME_REQUIRED),
                Arguments.of(INVALID_NAME_SPACES, BankAlert.VALID_NAME_REQUIRED)
        );
    }

    static Stream<Arguments> invalidFormatNames() {
        return Stream.of(
                Arguments.of(INVALID_NAME_ONE_WORD, BankAlert.INVALID_NAME),
                Arguments.of(INVALID_NAME_SPECIAL_CHARS, BankAlert.INVALID_NAME),
                Arguments.of(INVALID_NAME_NUMBERS, BankAlert.INVALID_NAME)
        );
    }
}