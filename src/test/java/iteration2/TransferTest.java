package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

public class TransferTest {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void transferMoneyBetweenAccounts() {
        // Основной тест
        String username = "transfertest";
        String password = "Password123$";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        Integer senderAccountId = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiverAccountId = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + senderAccountId + ", \"balance\": 15000.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + senderAccountId + ", \"receiverAccountId\": " + receiverAccountId + ", \"amount\": 5000.0}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void transferBoundaryValues() {
        // Тест граничных значений для перевода
        String username = "transboundary";
        String password = "Password123$";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // Тест 1: 9999.99 (ниже границы 10000)
        Integer sender1 = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiver1 = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + sender1 + ", \"balance\": 20000.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + sender1 + ", \"receiverAccountId\": " + receiver1 + ", \"amount\": 9999.99}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // Тест 2: 10000.01 (выше границы 10000)
        Integer sender2 = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiver2 = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + sender2 + ", \"balance\": 20000.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + sender2 + ", \"receiverAccountId\": " + receiver2 + ", \"amount\": 10000.01}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // Тест 3: 0.01 (минимальный перевод)
        Integer sender3 = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiver3 = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + sender3 + ", \"balance\": 100.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + sender3 + ", \"receiverAccountId\": " + receiver3 + ", \"amount\": 0.01}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void cannotTransferZeroAmount() {
        String username = "transzero";
        String password = "Password123$";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        Integer senderAccountId = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        Integer receiverAccountId = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + senderAccountId + ", \"balance\": 100.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // Попытка перевода 0.00
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + senderAccountId + ", \"receiverAccountId\": " + receiverAccountId + ", \"amount\": 0.00}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void cannotTransferMoreThanBalance() {
        // ... существующий тест
    }

    @Test
    public void cannotTransferNegativeAmount() {
        // ... существующий тест
    }
}