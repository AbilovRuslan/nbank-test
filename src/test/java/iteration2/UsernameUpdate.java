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

public class UsernameUpdate {

    @BeforeAll
    public static void setup() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void customerCanUpdateOwnNameWithTwoWords() {
        // ДОБАВИЛ: Проверяем имя ДО обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.notNullValue()); // Проверяем, что имя существует (не null)

        // Существующий код обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"Ivan Ivanov\"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("Ivan Ivanov"));

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("Ivan Ivanov"));
    }

    @Test
    public void cannotUpdateNameWithOneWord() {
        // ДОБАВИЛ: Проверяем имя ДО обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.notNullValue());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"Ivan\"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ неудачного обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.not(Matchers.equalTo("Ivan"))); // Имя не должно быть "Ivan"
    }

    @Test
    public void cannotUpdateNameWithThreeWords() {
        // ДОБАВИЛ: Проверяем имя ДО обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.notNullValue());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"Ivan Ivanov Petrovich\"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ неудачного обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.not(Matchers.equalTo("Ivan Ivanov Petrovich"))); // Имя не должно быть изменено
    }

    @Test
    public void cannotUpdateNameWithOnlySpaces() {
        //  ДОБАВИЛ: Проверяем имя ДО обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.notNullValue());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"  \"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ неудачного обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.not(Matchers.equalTo("  "))); // Имя не должно состоять только из пробелов
    }

    @Test
    public void cannotUpdateNameWithEmptyString() {
        // ДОБАВИЛ: Проверяем имя ДО обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.notNullValue());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"\"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ неудачного обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.not(Matchers.isEmptyOrNullString())); // Имя не должно быть пустым
    }

    @Test
    public void cannotUpdateNameWithSpecialCharacters() {
        // ДОБАВИЛ: Проверяем имя ДО обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.notNullValue());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"Ivan@ Ivanov\"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ неудачного обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.not(Matchers.equalTo("Ivan@ Ivanov"))); // Имя не должно содержать специальные символы
    }

    @Test
    public void canUpdateNameWithDifferentTwoWordFormats() {
        // Проверяем разные варианты двух слов
        // ДОБАВИЛ: Проверяем имя ДО первого обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.notNullValue());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"John Doe\"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("John Doe"));

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ первого обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("John Doe"));

        // ДОБАВИЛ: Проверяем имя ДО второго обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("John Doe")); // Должно быть предыдущее значение

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"Anna-Maria Schmidt\"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("Anna-Maria Schmidt"));

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ второго обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.equalTo("Anna-Maria Schmidt"));
    }

    @Test
    public void cannotUpdateNameWithNumbers() {
        //  ДОБАВИЛ: Проверяем имя ДО обновления
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.notNullValue());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .body("{\"name\":\"Ivan 123\"}")
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // ДОБАВИЛ: Проверяем имя ПОСЛЕ неудачного обновления через GET
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic a2F0ZTIwMDAxMTpLYXRlMjAwMCMh")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", Matchers.not(Matchers.equalTo("Ivan 123"))); // Имя не должно содержать цифры
    }
}