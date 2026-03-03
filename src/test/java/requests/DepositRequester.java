package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.DepositMoneyRequest;

import static io.restassured.RestAssured.given;

public class DepositRequester {
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public DepositRequester(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ValidatableResponse post(DepositMoneyRequest request) {
        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/accounts/deposit")
                .then()
                .spec(responseSpec);
    }
}