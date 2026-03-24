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
                .post("/api/v1/accounts")  // 👈 ИСПРАВЛЕНО!
                .then()
                .spec(responseSpec);
    }

    public ValidatableResponse deposit(DepositMoneyRequest depositRequest) {
        return given()
                .spec(requestSpec)
                .body(depositRequest)
                .post("/api/v1/accounts/deposit")  // 👈 ИСПРАВЛЕНО!
                .then()
                .spec(responseSpec);
    }

    public ValidatableResponse transfer(TransferMoneyRequest transferRequest) {
        return given()
                .spec(requestSpec)
                .body(transferRequest)
                .post("/api/v1/accounts/transfer")  // 👈 ИСПРАВЛЕНО!
                .then()
                .spec(responseSpec);
    }

    public AccountInfoResponse getAccount(Long accountId) {
        return given()
                .spec(requestSpec)
                .get("/api/v1/accounts/" + accountId)  // ✅ УЖЕ ПРАВИЛЬНО!
                .then()
                .spec(responseSpec)
                .extract()
                .as(AccountInfoResponse.class);
    }
}