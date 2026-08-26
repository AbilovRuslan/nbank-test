package iteration1;

import dao.UserDao;
import iteration1.api.BaseTest;
import requests.steps.DataBaseSteps;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.LoginUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username(ADMIN_USERNAME)
                .password(ADMIN_PASSWORD)
                .build();

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(userRequest);

        UserDao adminDao = DataBaseSteps.getUserByUsername(ADMIN_USERNAME);
        assertThat(adminDao).isNotNull();
        assertThat(adminDao.getRole()).isEqualTo(ADMIN_ROLE);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(VALID_NAME_TWO_WORDS)
                .password(VALID_NAME_ENGLISH)
                .build();

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.authSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder()
                        .username(userRequest.getUsername())
                        .password(userRequest.getPassword())
                        .build())
                .header("Authorization", Matchers.notNullValue());

        UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
        assertThat(userDao).isNotNull();
        assertThat(userDao.getUsername()).isEqualTo(userRequest.getUsername());
    }

    @Test
    public void userCannotLoginWithInvalidCredentialsTest() {
        LoginUserRequest invalidRequest = LoginUserRequest.builder()
                .username(VALID_NAME_TWO_WORDS)
                .password(INVALID_NAME_ONE_WORD)
                .build();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.unauthorized())
                .post(invalidRequest);
    }

    @Test
    public void userCannotLoginWithInvalidUsernameTest() {
        for (String invalidName : INVALID_USERNAMES) {
            LoginUserRequest invalidRequest = LoginUserRequest.builder()
                    .username(invalidName)
                    .password(VALID_NAME_ENGLISH)
                    .build();

            new CrudRequester(RequestSpecs.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpecs.badRequest())
                    .post(invalidRequest);
        }
    }

    @Test
    public void createUserWithAllValidNamesTest() {
        for (String validName : VALID_USERNAMES) {
            CreateUserRequest userRequest = CreateUserRequest.builder()
                    .username(validName)
                    .password(VALID_NAME_ENGLISH)
                    .build();

            CreateUserResponse response = new ValidatedCrudRequester<CreateUserResponse>(
                    RequestSpecs.authSpec(),
                    Endpoint.ADMIN_USER,
                    ResponseSpecs.entityWasCreated())
                    .post(userRequest)
                    .as(CreateUserResponse.class);

            assertThat(response.getId()).isNotNull();

            new CrudRequester(RequestSpecs.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpecs.requestReturnsOK())
                    .post(LoginUserRequest.builder()
                            .username(userRequest.getUsername())
                            .password(userRequest.getPassword())
                            .build())
                    .header("Authorization", Matchers.notNullValue());

            UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
            assertThat(userDao).isNotNull();
            assertThat(userDao.getUsername()).isEqualTo(userRequest.getUsername());
        }
    }
}