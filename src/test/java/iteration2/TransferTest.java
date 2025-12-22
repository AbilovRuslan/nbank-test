package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
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

        // ДОБАВИЛ: Проверяем балансы ДО перевода
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + senderAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(15000.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiverAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + senderAccountId + ", \"receiverAccountId\": " + receiverAccountId + ", \"amount\": 5000.0}")
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

        // ДОБАВИЛ: Проверяем балансы ДО перевода 9999.99
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + sender1)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(20000.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiver1)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + sender1 + ", \"receiverAccountId\": " + receiver1 + ", \"amount\": 9999.99}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // ДОБАВИЛ: Проверяем балансы ПОСЛЕ перевода 9999.99
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + sender1)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(10000.01f)); // 20000 - 9999.99

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiver1)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(9999.99f));

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

        // ДОБАВИЛ: Проверяем балансы ДО перевода 10000.01
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + sender2)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(20000.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiver2)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + sender2 + ", \"receiverAccountId\": " + receiver2 + ", \"amount\": 10000.01}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // ДОБАВИЛ: Проверяем балансы ПОСЛЕ перевода 10000.01
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + sender2)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(9999.99f)); // 20000 - 10000.01

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiver2)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(10000.01f));

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

        // ДОБАВИЛ: Проверяем балансы ДО перевода 0.01
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + sender3)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(100.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiver3)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + sender3 + ", \"receiverAccountId\": " + receiver3 + ", \"amount\": 0.01}")
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

        // ДОБАВИЛ: Проверяем балансы ДО попытки перевода 0.00
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + senderAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(100.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiverAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f));

        // Попытка перевода 0.00
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + senderAccountId + ", \"receiverAccountId\": " + receiverAccountId + ", \"amount\": 0.00}")
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
        // Создаем пользователя
        String username = "transfertest2";
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

        // Создаем два счета
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

        // Кладем только 100 на sender счет
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + senderAccountId + ", \"balance\": 100.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // ДОБАВИЛ: Проверяем балансы ДО попытки перевода 200
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + senderAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(100.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiverAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f));

        // Пытаемся перевести 200 (больше чем есть)
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + senderAccountId + ", \"receiverAccountId\": " + receiverAccountId + ", \"amount\": 200.0}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем балансы ПОСЛЕ неудачного перевода 200
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
    public void cannotTransferNegativeAmount() {
        // Создаем пользователя
        String username = "transfertest3";
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

        // Создаем два счета
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

        // Кладем деньги
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + senderAccountId + ", \"balance\": 500.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        //  ДОБАВИЛ: Проверяем балансы ДО попытки перевода -100
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + senderAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(500.0f));

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiverAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f));

        // Пытаемся перевести отрицательную сумму
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"senderAccountId\": " + senderAccountId + ", \"receiverAccountId\": " + receiverAccountId + ", \"amount\": -100.0}")
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем балансы ПОСЛЕ неудачного перевода -100
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + senderAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(500.0f)); // остался 500

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + receiverAccountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f)); // остался 0
    }
}