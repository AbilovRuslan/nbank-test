package requests.steps;


import models.CreateAccountResponse;
import models.UserProfileResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

import static io.restassured.RestAssured.given;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public  List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()).getAll(CreateAccountResponse[].class);
    }

    public UserProfileResponse getProfile() {
        return given()
                .spec(RequestSpecs.authAsUser(username, password))
                .get("/api/v1/customer/profile")
                .then().assertThat()
                .statusCode(200)
                .extract().as(UserProfileResponse.class);
    }
}