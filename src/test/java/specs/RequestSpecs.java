package specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginUserRequest;
import requests.LoginUserRequester;

import java.util.List;

public class RequestSpecs {
    private RequestSpecs(){}

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri("http://localhost:4111");
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        String userAuthHeader = new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(username).password(password).build())
                .extract()
                .header("Authorization");

        return authSpec(userAuthHeader);
    }

    public static RequestSpecification authSpec(String authToken) {
        if (authToken == null) {
            throw new IllegalArgumentException("Auth token cannot be null");
        }

        // Очищаем токен от возможных дублирований "Basic "
        String cleanToken = cleanAuthToken(authToken);

        return defaultRequestBuilder()
                .addHeader("Authorization", cleanToken)
                .build();
    }

    private static String cleanAuthToken(String authToken) {
        if (authToken == null || authToken.trim().isEmpty()) {
            return authToken;
        }

        String trimmed = authToken.trim();

        // Удаляем все вхождения "Basic " в начале строки
        while (trimmed.startsWith("Basic ")) {
            trimmed = trimmed.substring(6).trim();
        }

        // Добавляем "Basic " один раз
        return "Basic " + trimmed;
    }

    // Метод для извлечения чистого токена (без "Basic ")
    public static String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "";
        }

        String trimmed = authHeader.trim();
        // Удаляем все вхождения "Basic " в начале строки
        while (trimmed.startsWith("Basic ")) {
            trimmed = trimmed.substring(6).trim();
        }
        return trimmed;
    }
}