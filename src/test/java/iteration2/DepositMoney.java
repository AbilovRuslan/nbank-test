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

public class DepositMoney {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void depositMoneyToAccount() {
        // 1. Создаем пользователя
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"testuser1\", \"password\": \"Password123$\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // 2. Логинимся
        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"testuser1\", \"password\": \"Password123$\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // 3. Создаем счет
        Integer accountId = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        // 4. Кладем деньги
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + accountId + ", \"balance\": 1000.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void depositBoundaryValues() {
        // 1. Создаем пользователя
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"testuser2\", \"password\": \"Password123$\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // 2. Логинимся
        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"testuser2\", \"password\": \"Password123$\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // 3. Тест 4999.99 - должен пройти
        Integer account1 = given()
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
                .body("{\"id\": " + account1 + ", \"balance\": 4999.99}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // 4. Тест 5000.01 - должен упасть
        Integer account2 = given()
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
                .body("{\"id\": " + account2 + ", \"balance\": 5000.01}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // 5. Тест 0.01 - должен пройти
        Integer account3 = given()
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
                .body("{\"id\": " + account3 + ", \"balance\": 0.01}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void cannotDepositZero() {
        // 1. Создаем пользователя
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"testuser3\", \"password\": \"Password123$\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // 2. Логинимся
        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"testuser3\", \"password\": \"Password123$\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // 3. Создаем счет
        Integer accountId = given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");

        // 4. Тест 0.00 - должен упасть
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + accountId + ", \"balance\": 0.00}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}