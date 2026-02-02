package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.TransferMoneyRequest;

import static io.restassured.RestAssured.given;

public class TransferRequester {
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public TransferRequester(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ValidatableResponse post(TransferMoneyRequest request) {
        return given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .spec(responseSpec);
    }
}