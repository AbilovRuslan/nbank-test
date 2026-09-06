package requests.steps;

import models.*;
import requests.skelethon.requesters.CrudRequester;

import static io.restassured.RestAssured.given;

public class AccountSteps {

    private final String username;
    private final String password;

    public AccountSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public CreateAccountResponse createAccount() {
        return given()
                .auth().basic(username, password)
                .post("/api/accounts")
                .then()
                .statusCode(201)
                .extract()
                .as(CreateAccountResponse.class);
    }

    public DepositResponse depositToAccount(String accountId, double amount) {
        DepositMoneyRequest request = new DepositMoneyRequest();
        request.setAmount(amount);

        return given()
                .auth().basic(username, password)
                .body(request)
                .post("/api/accounts/" + accountId + "/deposit")
                .then()
                .statusCode(200)
                .extract()
                .as(DepositResponse.class);
    }

    public TransferResponse transferWithFraudCheck(String fromAccountId,
                                                   String toAccountId,
                                                   double amount) {
        TransferMoneyRequest request = new TransferMoneyRequest();
        request.setFromAccountId(fromAccountId);
        request.setToAccountId(toAccountId);
        request.setAmount(amount);

        return given()
                .auth().basic(username, password)
                .body(request)
                .post("/api/transfers/fraud-check")
                .then()
                .statusCode(200)
                .extract()
                .as(TransferResponse.class);
    }

    public AccountInfoResponse getAccount(String accountId) {
        return given()
                .auth().basic(username, password)
                .get("/api/accounts/" + accountId)
                .then()
                .statusCode(200)
                .extract()
                .as(AccountInfoResponse.class);
    }
}