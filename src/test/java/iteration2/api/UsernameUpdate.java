package iteration2.api;

import dao.UserDao;
import requests.steps.DataBaseSteps;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UpdateUsernameRequest;
import models.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Изменение имени пользователя")
public class UsernameUpdate {

    private CreateUserRequest userRequest;
    private String authToken;

    @BeforeEach
    void setup() {
        userRequest = AdminSteps.createUser();

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .build();

        authToken = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK()
        ).post(loginRequest)
                .extract()
                .header("Authorization");
    }

    @Test
    @DisplayName("Пользователь может обновить имя на валидное")
    void shouldUpdateNameWithValidValue() {
        updateName(VALID_NAME_TWO_WORDS);

        UserProfileResponse profile = getProfile();
        assertThat(profile.getName()).isEqualTo(VALID_NAME_TWO_WORDS);

        UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
        assertThat(userDao.getName()).isEqualTo(VALID_NAME_TWO_WORDS);
    }

    @ParameterizedTest(name = "Невалидное имя: {0} -> {1}")
    @MethodSource("invalidNames")
    @DisplayName("Невалидные имена должны отклоняться")
    void shouldRejectInvalidNames(String invalidName, String expectedError) {
        String errorResponse = updateNameAndGetError(invalidName);
        assertThat(errorResponse).contains(expectedError);

        UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
        assertThat(userDao.getName()).isNull();
    }

    @Test
    @DisplayName("Доступ к профилю без авторизации должен возвращать 401")
    void shouldReturnUnauthorizedWithoutAuth() {
        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.unauthorized()
        ).get(DEFAULT_USER_ID);
    }

    // ================= HELPER METHODS =================

    private void updateName(String newName) {
        new CrudRequester(
                RequestSpecs.authSpec(authToken),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).update(DEFAULT_USER_ID, new UpdateUsernameRequest(newName));
    }

    private String updateNameAndGetError(String newName) {
        return new CrudRequester(
                RequestSpecs.authSpec(authToken),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.badRequest()
        ).update(DEFAULT_USER_ID, new UpdateUsernameRequest(newName))
                .extract()
                .body()
                .asString();
    }

    private UserProfileResponse getProfile() {
        return new CrudRequester(
                RequestSpecs.authSpec(authToken),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get(DEFAULT_USER_ID)
                .extract()
                .as(UserProfileResponse.class);
    }

    private static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of(INVALID_NAME_EMPTY, ERROR_NAME_REQUIRED),
                Arguments.of(INVALID_NAME_ONE_WORD, ERROR_NAME_FORMAT),
                Arguments.of(INVALID_NAME_SPECIAL_CHARS, ERROR_NAME_FORMAT),
                Arguments.of(INVALID_NAME_NUMBERS, ERROR_NAME_FORMAT),
                Arguments.of(INVALID_NAME_SPACES, ERROR_NAME_REQUIRED),
                Arguments.of(INVALID_NAME_THREE_WORDS, ERROR_NAME_FORMAT)
        );
    }
}