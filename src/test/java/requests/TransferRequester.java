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
        // Преобразуем запрос в правильный формат для API
        return given()
                .spec(requestSpec)
                .body(new TransferRequestBody(
                        request.getFromAccountId(),
                        request.getToAccountId(),
                        request.getAmount()
                ))
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .spec(responseSpec);
    }

    // Вспомогательный класс с правильными именами полей
    private static class TransferRequestBody {
        private final Long senderAccountId;
        private final Long receiverAccountId;
        private final Double amount;

        public TransferRequestBody(Long fromAccountId, Long toAccountId, Double amount) {
            this.senderAccountId = fromAccountId;
            this.receiverAccountId = toAccountId;
            this.amount = amount;
        }

        public Long getSenderAccountId() {
            return senderAccountId;
        }

        public Long getReceiverAccountId() {
            return receiverAccountId;
        }

        public Double getAmount() {
            return amount;
        }
    }
}