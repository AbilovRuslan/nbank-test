package iteration1.api;

import dao.UserDao;
import requests.steps.DataBaseSteps;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.LoginUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(userRequest);

        // БД-проверка: админ существует
        UserDao adminDao = DataBaseSteps.getUserByUsername("admin");
        assertThat(adminDao).isNotNull();
        assertThat(adminDao.getRole()).isEqualTo("ADMIN");
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .header("Authorization", Matchers.notNullValue());

        // БД-проверка: пользователь существует
        UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
        assertThat(userDao).isNotNull();
        assertThat(userDao.getUsername()).isEqualTo(userRequest.getUsername());
    }
}