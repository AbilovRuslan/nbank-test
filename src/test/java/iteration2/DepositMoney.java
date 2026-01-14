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
import java.util.Random;

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
        // 1. Создаем пользователя с коротким именем (3-15 символов)
        String username = "user" + new Random().nextInt(10000);
        String password = "Password123$";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // 2. Логинимся
        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");

        // 3. Создаем счет
        Integer accountId = given()
                .header("Authorization", auth)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // 4. Пополняем счет (максимум 5000 по ограничениям API)
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + accountId + ", \"balance\": 1000.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(200);
    }

    @Test
    public void depositBoundaryValues() {
        // 1. Создаем пользователя
        String username = "user" + new Random().nextInt(10000);
        String password = "Password123$";

        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // 2. Логинимся
        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");

        // 3. Создаем счет
        Integer accountId = given()
                .header("Authorization", auth)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // 4. Тест минимального депозита (0.01)
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + accountId + ", \"balance\": 0.01}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(200);

        // 5. Тест максимального депозита (5000)
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + accountId + ", \"balance\": 5000.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(200);

        // 6. Тест превышения лимита (5000.01 - должен упасть)
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + accountId + ", \"balance\": 5000.01}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(400);
    }

    @Test
    public void cannotDepositZero() {
        // 1. Создаем пользователя
        String username = "user" + new Random().nextInt(10000);
        String password = "Password123$";

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"role\": \"USER\"}")
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // 2. Логинимся
        String auth = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .header("Authorization");

        // 3. Создаем счет
        Integer accountId = given()
                .header("Authorization", auth)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // 4. Попытка депозита 0 (должен упасть)
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + accountId + ", \"balance\": 0.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(400);
    }
}