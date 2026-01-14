package requests;

import models.UpdateUsernameRequest;
import models.UserProfileResponse;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class UserProfileRequests {
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public UserProfileRequests(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ValidatableResponse updateUsername(UpdateUsernameRequest updateRequest) {
        return given()
                .spec(requestSpec)
                .body(updateRequest)
                .put("/users/profile")
                .then()
                .spec(responseSpec);
    }

    public UserProfileResponse getProfile() {
        return given()
                .spec(requestSpec)
                .get("/users/profile")
                .then()
                .spec(responseSpec)
                .extract()
                .as(UserProfileResponse.class);
    }
}