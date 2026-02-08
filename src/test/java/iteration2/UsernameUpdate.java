package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import generators.RandomData;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.ApiPaths;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class UsernameUpdate {

    // Константы для тестовых данных
    private static final String VALID_NAME_TWO_WORDS = "Ivan Ivanov";
    private static final String VALID_NAME_WITH_MIDDLE = "Anna Maria";
    private static final String VALID_NAME_ENGLISH = "John Doe";

    private static final String INVALID_NAME_ONE_WORD = "Ivan";
    private static final String INVALID_NAME_THREE_WORDS = "Ivan Ivanov Petrovich";
    private static final String INVALID_NAME_SPACES = "   ";
    private static final String INVALID_NAME_EMPTY = "";
    private static final String INVALID_NAME_SPECIAL_CHARS = "Ivan@ Ivanov";
    private static final String INVALID_NAME_NUMBERS = "Ivan 123";

    // HTTP статусы
    private static final int STATUS_OK = 200;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final int STATUS_UNAUTHORIZED = 401;

    // Конфигурация
    private static final String BASE_URI_PROPERTY = "test.base.uri";
    private static final String DEFAULT_BASE_URI = "http://localhost:4111";

    private static RequestSpecification customerAuthSpec;
    private static String testUsername;
    private static String testPassword;

    @BeforeEach
    public void setup() {
        // Без хардкода - через системную переменную
        RestAssured.baseURI = getBaseUri();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        createTestCustomer();
    }

    private String getBaseUri() {
        return System.getProperty(BASE_URI_PROPERTY, DEFAULT_BASE_URI);
    }

    private void createTestCustomer() {
        // Генерация username и password через RandomData
        testUsername = RandomData.getUsername();
        testPassword = RandomData.getPassword();

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .role(UserRole.USER.toString())
                .build();

        // Создание пользователя - путь из ApiPaths
        given()
                .spec(RequestSpecs.adminSpec())
                .body(userRequest)
                .post(ApiPaths.Admin.CREATE_USER)
                .then()
                .spec(ResponseSpecs.entityWasCreated());

        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .build();

        // Авторизация - путь из ApiPaths
        String token = given()
                .spec(RequestSpecs.unauthSpec())
                .body(loginRequest)
                .post(ApiPaths.Auth.LOGIN)
                .then()
                .spec(ResponseSpecs.requestReturnsOK())
                .extract()
                .header(ResponseSpecs.AUTHORIZATION_HEADER);

        customerAuthSpec = RequestSpecs.authSpec(token);

        // Проверка доступа к профилю - путь из ApiPaths
        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK);
    }

    // ================= HELPER METHODS =================

    private String getCurrentCustomerName() {
        return given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .extract()
                .path("name");
    }

    private void updateCustomerName(String newName, int expectedStatus) {
        given()
                .spec(customerAuthSpec)
                .body(Map.of("name", newName))
                .put(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(expectedStatus);
    }

    // ================= POSITIVE TESTS =================

    @Test
    public void customerCanUpdateOwnNameWithTwoWords() {
        String oldName = getCurrentCustomerName();

        updateCustomerName(VALID_NAME_TWO_WORDS, STATUS_OK);

        // Проверка: имя реально поменялось
        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .body("name", equalTo(VALID_NAME_TWO_WORDS))
                .body("name", not(equalTo(oldName)));
    }

    @ParameterizedTest
    @ValueSource(strings = {VALID_NAME_TWO_WORDS, VALID_NAME_WITH_MIDDLE, VALID_NAME_ENGLISH})
    public void canUpdateNameWithDifferentTwoWordFormats(String name) {
        String oldName = getCurrentCustomerName();

        updateCustomerName(name, STATUS_OK);

        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .body("name", equalTo(name))
                .body("name", not(equalTo(oldName)));
    }

    // ================= NEGATIVE TESTS =================

    @Test
    public void cannotUpdateNameWithOneWord() {
        String oldName = getCurrentCustomerName();

        updateCustomerName(INVALID_NAME_ONE_WORD, STATUS_BAD_REQUEST);

        // Проверка: имя не изменилось
        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithThreeWords() {
        String oldName = getCurrentCustomerName();

        updateCustomerName(INVALID_NAME_THREE_WORDS, STATUS_BAD_REQUEST);

        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithOnlySpaces() {
        String oldName = getCurrentCustomerName();

        updateCustomerName(INVALID_NAME_SPACES, STATUS_BAD_REQUEST);

        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithEmptyString() {
        String oldName = getCurrentCustomerName();

        updateCustomerName(INVALID_NAME_EMPTY, STATUS_BAD_REQUEST);

        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithSpecialCharacters() {
        String oldName = getCurrentCustomerName();

        updateCustomerName(INVALID_NAME_SPECIAL_CHARS, STATUS_BAD_REQUEST);

        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .body("name", equalTo(oldName));
    }

    @Test
    public void cannotUpdateNameWithNumbers() {
        String oldName = getCurrentCustomerName();

        updateCustomerName(INVALID_NAME_NUMBERS, STATUS_BAD_REQUEST);

        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .body("name", equalTo(oldName));
    }

    // ================= EDGE CASE =================

    @Test
    public void testCustomerProfileAccess() {
        given()
                .spec(customerAuthSpec)
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_OK)
                .log().body();
    }

    @Test
    public void getProfileUnauthorized() {
        given()
                .spec(RequestSpecs.unauthSpec())
                .get(ApiPaths.Customer.PROFILE)
                .then()
                .statusCode(STATUS_UNAUTHORIZED);
    }
}