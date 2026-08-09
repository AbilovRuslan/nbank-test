package iteration2.api;

import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.stream.Stream;

import static constants.TestConstants.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Изменение имени пользователя")
public class UsernameUpdate {

    private RequestSpecification authSpec;

    @BeforeEach
    void setup() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .build();

        String authToken = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK()
        ).post(loginRequest)
                .extract()
                .header("Authorization");

        authSpec = RequestSpecs.authSpec(authToken);
    }

    @Test
    @DisplayName("Пользователь может обновить имя на валидное")
    void shouldUpdateNameWithValidValue() {
        updateName(VALID_NAME_TWO_WORDS);

        UserProfileResponse profile = getProfile();
        assertThat(profile.getName()).isEqualTo(VALID_NAME_TWO_WORDS);
    }

    @ParameterizedTest(name = "Невалидное имя: {0}")
    @MethodSource("invalidNames")
    @DisplayName("Невалидные имена должны отклоняться")
    void shouldRejectInvalidNames(String invalidName, String expectedError) {
        String errorResponse = updateNameAndGetError(invalidName);
        assertThat(errorResponse).contains(expectedError);
    }

    @Test
    @DisplayName("Доступ к профилю без авторизации должен возвращать 401")
    void shouldReturnUnauthorizedWithoutAuth() {
        given()
                .spec(RequestSpecs.unauthSpec())
                .get(Endpoint.CUSTOMER_PROFILE.getUrl())
                .then()
                .statusCode(STATUS_UNAUTHORIZED);
    }

    // ================= HELPER METHODS =================

    private void updateName(String newName) {
        given()
                .spec(authSpec)
                .body(Map.of("name", newName))
                .put(Endpoint.CUSTOMER_PROFILE.getUrl())
                .then()
                .statusCode(STATUS_OK);
    }

    private String updateNameAndGetError(String newName) {
        return given()
                .spec(authSpec)
                .body(Map.of("name", newName))
                .put(Endpoint.CUSTOMER_PROFILE.getUrl())
                .then()
                .statusCode(STATUS_BAD_REQUEST)
                .extract()
                .asString();
    }

    private UserProfileResponse getProfile() {
        return given()
                .spec(authSpec)
                .get(Endpoint.CUSTOMER_PROFILE.getUrl())
                .then()
                .statusCode(STATUS_OK)
                .extract()
                .as(UserProfileResponse.class);
    }

    private static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of(INVALID_NAME_EMPTY, "Name must contain"),
                Arguments.of(INVALID_NAME_ONE_WORD, "must contain two words"),
                Arguments.of(INVALID_NAME_SPECIAL_CHARS, "must contain two words"),
                Arguments.of(INVALID_NAME_NUMBERS, "must contain two words"),
                Arguments.of(INVALID_NAME_SPACES, "Name must contain"),
                Arguments.of(INVALID_NAME_THREE_WORDS, "must contain two words")
        );
    }
}