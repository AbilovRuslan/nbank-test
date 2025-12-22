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

        // 4. ДОБАВИЛ: Проверяем начальный баланс
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + accountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(0.0f));

        // 5. Кладем деньги
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .body("{\"id\": " + accountId + ", \"balance\": 1000.0}")
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);

        // 6.  ДОБАВИЛ: Проверяем что баланс изменился
        given()
                .header("Authorization", auth)
                .contentType(ContentType.JSON)
                .get("http://localhost:4111/api/v1/accounts/" + accountId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(1000.0f));
    }

}    