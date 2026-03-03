package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class CreateAccountRequester {
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public CreateAccountRequester(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }


    public ValidatableResponse post(Object body) {
        return given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/v1/accounts")
                .then()
                .spec(responseSpec);
    }
    public ValidatableResponse post() {
        return post(null);
    }
}