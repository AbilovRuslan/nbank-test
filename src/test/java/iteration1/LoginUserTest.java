package iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.LoginUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        // NOTE: config.properties created in src/test/resources/
        // Using hardcoded values from iteration 1 template
        // TODO: Read from config in next iteration

        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username("admin")  // Hardcoded in iteration 1 template
                .password("admin")  // TODO: Move to config in next iteration
                .build();

        new LoginUserRequester(RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        new LoginUserRequester(RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .header(ResponseSpecs.AUTHORIZATION_HEADER, Matchers.notNullValue());
    }
}