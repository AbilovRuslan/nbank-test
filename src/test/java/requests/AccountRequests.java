package requests;

import models.AccountInfoResponse;
import models.DepositMoneyRequest;
import models.TransferMoneyRequest;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class AccountRequests {
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public AccountRequests(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ValidatableResponse createAccount() {
        return given()
                .spec(requestSpec)
                .post("/accounts")
                .then()
                .spec(responseSpec);
    }

    public ValidatableResponse deposit(DepositMoneyRequest depositRequest) {
        return given()
                .spec(requestSpec)
                .body(depositRequest)
                .post("/accounts/deposit")
                .then()
                .spec(responseSpec);
    }

    public ValidatableResponse transfer(TransferMoneyRequest transferRequest) {
        return given()
                .spec(requestSpec)
                .body(transferRequest)
                .post("/accounts/transfer")
                .then()
                .spec(responseSpec);
    }

    public AccountInfoResponse getAccount(Long accountId) {
        return given()
                .spec(requestSpec)
                .get("/accounts/" + accountId)
                .then()
                .spec(responseSpec)
                .extract()
                .as(AccountInfoResponse.class);
    }
}