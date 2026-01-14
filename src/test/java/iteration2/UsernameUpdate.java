package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static specs.RequestSpecs.*;

public class UsernameUpdate {

    private static RequestSpecification customerAuthSpec;
    private static String testUsername;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:4111";

        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));

        // Создаем тестового пользователя
        createTestCustomer();
    }

    private static void createTestCustomer() {
        // Создаем уникальные учетные данные (3-15 символов)
        long timestamp = System.currentTimeMillis() % 10000;
        testUsername = "cust_" + timestamp;
        String testPassword = "Test123!";

        // Убедимся, что username не длиннее 15 символов
        if (testUsername.length() > 15) {
            testUsername = testUsername.substring(0, 15);
        }

        System.out.println("=== Создание тестового пользователя ===");
        System.out.println("Username: " + testUsername);
        System.out.println("Password: " + testPassword);

        // Создаем пользователя через админа (ожидаем 201 Created)
        String createUserJson = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"USER\"}",
                testUsername, testPassword);

        // Используем админа для создания пользователя
        given()
                .spec(adminSpec())
                .body(createUserJson)
                .post("/api/v1/admin/users")
                .then()
                .statusCode(201); // Ожидаем 201 Created

        System.out.println("Пользователь успешно создан");

        // Создаем токен авторизации вручную
        String credentials = testUsername + ":" + testPassword;
        String authToken = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        customerAuthSpec = unauthSpec()
                .header("Authorization", "Basic " + authToken);

        // Проверяем, что авторизация работает
        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200);

        System.out.println("Пользователь успешно авторизован");
    }

    @Test
    public void customerCanUpdateOwnNameWithTwoWords() {
        String jsonRequest = "{\"name\":\"Ivan Ivanov\"}";

        given()
                .spec(customerAuthSpec)
                .body(jsonRequest)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("customer.name", org.hamcrest.Matchers.equalTo("Ivan Ivanov"));
    }

    @Test
    public void cannotUpdateNameWithOneWord() {
        String jsonRequest = "{\"name\":\"Ivan\"}";

        given()
                .spec(customerAuthSpec)
                .body(jsonRequest)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(400);
    }

    @Test
    public void cannotUpdateNameWithThreeWords() {
        String jsonRequest = "{\"name\":\"Ivan Ivanov Petrovich\"}";

        given()
                .spec(customerAuthSpec)
                .body(jsonRequest)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(400);
    }

    @Test
    public void cannotUpdateNameWithOnlySpaces() {
        String jsonRequest = "{\"name\":\"  \"}";

        given()
                .spec(customerAuthSpec)
                .body(jsonRequest)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(400);
    }

    @Test
    public void cannotUpdateNameWithEmptyString() {
        String jsonRequest = "{\"name\":\"\"}";

        given()
                .spec(customerAuthSpec)
                .body(jsonRequest)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(400);
    }

    @Test
    public void cannotUpdateNameWithSpecialCharacters() {
        String jsonRequest = "{\"name\":\"Ivan@ Ivanov\"}";

        given()
                .spec(customerAuthSpec)
                .body(jsonRequest)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(400);
    }

    @Test

    public void canUpdateNameWithDifferentTwoWordFormats() {
        // Тест должен проверять ТОЛЬКО валидные форматы
        List<String> validNames = Arrays.asList(
                "John Doe",
                "Anna Maria",     // Без дефиса!
                "Ivan Ivanov"
        );

        for (String name : validNames) {
            String jsonRequest = String.format("{\"name\":\"%s\"}", name);

            given()
                    .spec(customerAuthSpec)
                    .body(jsonRequest)
                    .put("/api/v1/customer/profile")
                    .then()
                    .statusCode(200)
                    .body("customer.name", org.hamcrest.Matchers.equalTo(name));
        }
    }

    @Test
    public void cannotUpdateNameWithNumbers() {
        String jsonRequest = "{\"name\":\"Ivan 123\"}";

        given()
                .spec(customerAuthSpec)
                .body(jsonRequest)
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCustomerProfileAccess() {
        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .log().body();
    }
}