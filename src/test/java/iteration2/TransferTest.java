package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import models.DepositMoneyRequest;
import models.TransferMoneyRequest;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class TransferTest {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    private String createUserAndLogin() {
        String username = "user" + new Random().nextInt(10000);
        String password = "Password123$";

        // Создание пользователя
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // Логин и возврат токена
        return given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
    }

    private Integer createAccount(String auth) {
        return given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");
    }

    private void depositToAccount(String auth, Integer accountId, Double amount) {
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .accountId(accountId.longValue())  // конвертируем Integer → Long
                .amount(amount)
                .build();

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body(depositRequest)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void transferMoneyBetweenAccounts() {
        // 1. Создание пользователя и получение токена
        String auth = createUserAndLogin();

        // 2. Создание счетов
        Integer senderAccountId = createAccount(auth);
        Integer receiverAccountId = createAccount(auth);

        // 3. Депозит на отправителя
        depositToAccount(auth, senderAccountId, 1000.0);

        // 4. Перевод
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())  // Integer → Long
                .toAccountId(receiverAccountId.longValue())  // Integer → Long
                .amount(500.0)
                .build();

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // ДОБАВИЛ: Проверяем балансы ПОСЛЕ перевода
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + senderAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(10000.0f)); // 15000 - 5000

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiverAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(5000.0f)); // 0 + 5000
    }

    @Test
    public void transferMinimumAmount() {
        String auth = createUserAndLogin();
        Integer senderAccountId = createAccount(auth);
        Integer receiverAccountId = createAccount(auth);

        depositToAccount(auth, senderAccountId, 100.0);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(0.01)
                .build();

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // ДОБАВИЛ: Проверяем балансы ПОСЛЕ перевода 0.01
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + sender3)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(99.99f)); // 100 - 0.01

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiver3)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.01f));
    }

    @Test
    public void transferBelowLimit() {
        String auth = createUserAndLogin();
        Integer senderAccountId = createAccount(auth);
        Integer receiverAccountId = createAccount(auth);

        depositToAccount(auth, senderAccountId, 5000.0);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(4999.99)
                .build();

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void transferAboveLimit() {
        String auth = createUserAndLogin();
        Integer senderAccountId = createAccount(auth);
        Integer receiverAccountId = createAccount(auth);

        depositToAccount(auth, senderAccountId, 5000.0);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(5000.01)
                .build();

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void cannotTransferZero() {
        String auth = createUserAndLogin();
        Integer senderAccountId = createAccount(auth);
        Integer receiverAccountId = createAccount(auth);

        depositToAccount(auth, senderAccountId, 100.0);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(0.0)
                .build();

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем балансы ПОСЛЕ неудачного перевода 0.00
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + senderAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(100.0f)); // остался 100

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiverAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f)); // остался 0
    }

    @Test
    public void cannotTransferMoreThanBalance() {
        String auth = createUserAndLogin();
        Integer senderAccountId = createAccount(auth);
        Integer receiverAccountId = createAccount(auth);

        depositToAccount(auth, senderAccountId, 100.0);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(150.0)
                .build();

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void cannotTransferNegative() {
        String auth = createUserAndLogin();
        Integer senderAccountId = createAccount(auth);
        Integer receiverAccountId = createAccount(auth);

        depositToAccount(auth, senderAccountId, 100.0);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .fromAccountId(senderAccountId.longValue())
                .toAccountId(receiverAccountId.longValue())
                .amount(-50.0)
                .build();

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}