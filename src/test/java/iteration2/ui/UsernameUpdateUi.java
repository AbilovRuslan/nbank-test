package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.AccountInfoResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class UsernameUpdateUi {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.103:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));
    }

    @Test
    @DisplayName("Успешное изменение имени")
    public void updateNameValid() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();
        Selenide.sleep(1000);

        Selenide.open("/dashboard");
        Selenide.sleep(2000);

        Selenide.open("/edit-profile");
        Selenide.sleep(2000);

        $("[placeholder='Enter new name']").setValue("John Doe");
        $(Selectors.byText("💾 Save Changes")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("updated");
        alert.accept();

        // API-проверка
        String authToken = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract().header("Authorization");
        AccountInfoResponse[] accounts = given()
                .spec(RequestSpecs.authSpec(authToken))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat().statusCode(200)
                .extract().as(AccountInfoResponse[].class);
        assertThat(accounts).isNotEmpty();
    }

    @Test
    @DisplayName("Изменение имени на пустое — ошибка")
    public void updateNameEmpty() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();
        Selenide.sleep(1000);

        Selenide.open("/dashboard");
        Selenide.sleep(2000);

        Selenide.open("/edit-profile");
        Selenide.sleep(2000);

        $("[placeholder='Enter new name']").setValue("");
        $(Selectors.byText("💾 Save Changes")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("valid");
        error.accept();

        // API-проверка
        String authToken = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract().header("Authorization");
        AccountInfoResponse[] accounts = given()
                .spec(RequestSpecs.authSpec(authToken))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat().statusCode(200)
                .extract().as(AccountInfoResponse[].class);
        assertThat(accounts).isNotEmpty();
    }

    @Test
    @DisplayName("Изменение имени на слишком короткое — ошибка")
    public void updateNameTooShort() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();
        Selenide.sleep(1000);

        Selenide.open("/dashboard");
        Selenide.sleep(2000);

        Selenide.open("/edit-profile");
        Selenide.sleep(2000);

        $("[placeholder='Enter new name']").setValue("A");
        $(Selectors.byText("💾 Save Changes")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("must contain two words");
        error.accept();

        // API-проверка
        String authToken = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract().header("Authorization");
        AccountInfoResponse[] accounts = given()
                .spec(RequestSpecs.authSpec(authToken))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat().statusCode(200)
                .extract().as(AccountInfoResponse[].class);
        assertThat(accounts).isNotEmpty();
    }

    @Test
    @DisplayName("Изменение имени на спецсимволы — ошибка")
    public void updateNameSpecialChars() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();
        Selenide.sleep(1000);

        Selenide.open("/dashboard");
        Selenide.sleep(2000);

        Selenide.open("/edit-profile");
        Selenide.sleep(2000);

        $("[placeholder='Enter new name']").setValue("John@Doe");
        $(Selectors.byText("💾 Save Changes")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("must contain two words");
        error.accept();

        // API-проверка
        String authToken = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract().header("Authorization");
        AccountInfoResponse[] accounts = given()
                .spec(RequestSpecs.authSpec(authToken))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat().statusCode(200)
                .extract().as(AccountInfoResponse[].class);
        assertThat(accounts).isNotEmpty();
    }

    @Test
    @DisplayName("Изменение имени на пробелы — ошибка")
    public void updateNameOnlySpaces() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();
        Selenide.sleep(1000);

        Selenide.open("/dashboard");
        Selenide.sleep(2000);

        Selenide.open("/edit-profile");
        Selenide.sleep(2000);

        $("[placeholder='Enter new name']").setValue("   ");
        $(Selectors.byText("💾 Save Changes")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("must contain two words");
        error.accept();

        // API-проверка
        String authToken = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract().header("Authorization");
        AccountInfoResponse[] accounts = given()
                .spec(RequestSpecs.authSpec(authToken))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat().statusCode(200)
                .extract().as(AccountInfoResponse[].class);
        assertThat(accounts).isNotEmpty();
    }
}