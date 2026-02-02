package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.LoginUserRequest;  // ← ДОБАВЛЕН ЭТОТ ИМПОРТ
import models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.LoginUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class UsernameUpdate {

    private static RequestSpecification customerAuthSpec;
    private static String testUsername;
    private static String testPassword;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost:4111";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Создаем нового тестового пользователя для каждого теста
        createTestCustomer();
    }

    private void createTestCustomer() {
        // Генерация уникального username
        long timestamp = System.currentTimeMillis() % 100000;
        testUsername = "cust_" + timestamp;
        testPassword = "Test123!";

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .role(UserRole.USER.toString())
                .build();

        // Создание пользователя через админа
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // Логин и получение auth токена
        LoginUserRequest loginRequest = LoginUserRequest.builder()  // ← СОЗДАЕМ LoginUserRequest
                .username(testUsername)
                .password(testPassword)
                .build();

        String token = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(loginRequest)  // ← ПЕРЕДАЕМ loginRequest, а НЕ userRequest
                .extract()
                .header(ResponseSpecs.AUTHORIZATION_HEADER);

        customerAuthSpec = RequestSpecs.authSpec(token);

        // Проверка, что авторизация работает
        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200);
    }

    // ================= HELPER METHODS =================

    private String getCurrentCustomerName() {
        return given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .extract()
                .path("name");
    }

    private void updateCustomerName(String newName, int expectedStatus) {
        given()
                .spec(customerAuthSpec)
                .body(Map.of("name", newName))
                .put("/api/v1/customer/profile")
                .then()
                .statusCode(expectedStatus);
    }

    // ================= POSITIVE TESTS =================

    @Test
    public void customerCanUpdateOwnNameWithTwoWords() {
        String oldName = getCurrentCustomerName();
        String newName = "Ivan Ivanov";

        updateCustomerName(newName, 200);

        // Проверка: имя реально поменялось
        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .body("name", not(equalTo(oldName)));
    }

    @Test
    public void canUpdateNameWithDifferentTwoWordFormats() {
        List<String> validNames = Arrays.asList(
                "John Doe",
                "Anna Maria",
                "Ivan Ivanov"
        );

        for (String name : validNames) {
            String oldName = getCurrentCustomerName();

            updateCustomerName(name, 200);

            given()
                    .spec(customerAuthSpec)
                    .get("/api/v1/customer/profile")
                    .then()
                    .statusCode(200)
                    .body("name", equalTo(name))
                    .body("name", not(equalTo(oldName)));
        }
    }

    // ================= NEGATIVE TESTS =================

    @Test
    public void cannotUpdateNameWithOneWord() {
        String oldName = getCurrentCustomerName();
        String newName = "Ivan";

        updateCustomerName(newName, 400);

        // Проверка: имя не изменилось
        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithThreeWords() {
        String oldName = getCurrentCustomerName();
        String newName = "Ivan Ivanov Petrovich";

        updateCustomerName(newName, 400);

        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithOnlySpaces() {
        String oldName = getCurrentCustomerName();
        String newName = "   ";

        updateCustomerName(newName, 400);

        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithEmptyString() {
        String oldName = getCurrentCustomerName();
        String newName = "";

        updateCustomerName(newName, 400);

        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithSpecialCharacters() {
        String oldName = getCurrentCustomerName();
        String newName = "Ivan@ Ivanov";

        updateCustomerName(newName, 400);

        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithNumbers() {
        String oldName = getCurrentCustomerName();
        String newName = "Ivan 123";

        updateCustomerName(newName, 400);

        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .body("name", equalTo(oldName));
    }

    // ================= EDGE CASE =================

    @Test
    public void testCustomerProfileAccess() {
        given()
                .spec(customerAuthSpec)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(200)
                .log().body();
    }

    @Test
    public void getProfileUnauthorized() {
        given()
                .spec(RequestSpecs.unauthSpec())
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(401);
    }
}