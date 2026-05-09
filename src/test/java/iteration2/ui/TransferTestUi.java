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
import static org.assertj.core.api.Assertions.within;

public class TransferTestUi {

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
    @DisplayName("Успешный трансфер между счетами")
    public void transferValidAmount() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);

        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);

        Selenide.open("/deposit");
        Selenide.refresh();
        Selenide.sleep(1000);
        $("select.account-selector").selectOption(1);
        $("input.deposit-input").setValue("500.00");
        $(Selectors.byText("💵 Deposit")).click();
        switchTo().alert().accept();

        Selenide.open("/deposit");
        Selenide.sleep(1000);
        $("select.account-selector").selectOption(2);
        String secondAccount = $("select.account-selector").getSelectedOptionText();
        String accountNumber = secondAccount.split(" ")[0];

        Selenide.open("/transfer");
        Selenide.sleep(2000);
        Selenide.refresh();
        Selenide.sleep(2000);

        $("select.account-selector").selectOption(1);
        $("[placeholder='Enter recipient name']").sendKeys(user.getUsername());
        $("[placeholder='Enter recipient account number']").sendKeys(accountNumber);
        $("[placeholder='Enter amount']").setValue("100.00");
        $("#confirmCheck").click();
        $(Selectors.withText("Send Transfer")).click();

        Alert success = switchTo().alert();
        assertThat(success.getText()).contains("Successfully");
        success.accept();

        // API-проверка
        String authToken = new CrudRequester(RequestSpecs.unauthSpec(), Endpoint.LOGIN, ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract().header("Authorization");
        AccountInfoResponse[] accounts = given()
                .spec(RequestSpecs.authSpec(authToken))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat().statusCode(200)
                .extract().as(AccountInfoResponse[].class);
        assertThat(accounts).hasSize(2);
        assertThat(accounts[0].getBalance()).isCloseTo(400.00, within(0.001));
    }

    @Test
    @DisplayName("Трансфер с недостаточным балансом — ошибка")
    public void transferInsufficientFunds() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);
        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);

        Selenide.open("/deposit");
        Selenide.sleep(1000);
        $("select.account-selector").selectOption(2);
        String accountNumber = $("select.account-selector").getSelectedOptionText().split(" ")[0];

        Selenide.open("/transfer");
        Selenide.sleep(2000);
        Selenide.refresh();
        Selenide.sleep(2000);

        $("select.account-selector").selectOption(1);
        $("[placeholder='Enter recipient name']").sendKeys(user.getUsername());
        $("[placeholder='Enter recipient account number']").sendKeys(accountNumber);
        $("[placeholder='Enter amount']").setValue("100.00");
        $("#confirmCheck").click();
        $(Selectors.withText("Send Transfer")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("insufficient");
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
        assertThat(accounts).hasSize(2);
        assertThat(accounts[0].getBalance()).isZero();
        assertThat(accounts[1].getBalance()).isZero();
    }

    @Test
    @DisplayName("Трансфер без подтверждения — ошибка")
    public void transferWithoutConfirmation() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);
        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);

        Selenide.open("/deposit");
        Selenide.sleep(1000);
        $("select.account-selector").selectOption(1);
        $("input.deposit-input").setValue("500.00");
        $(Selectors.byText("💵 Deposit")).click();
        switchTo().alert().accept();

        Selenide.open("/deposit");
        Selenide.sleep(1000);
        $("select.account-selector").selectOption(2);
        String accountNumber = $("select.account-selector").getSelectedOptionText().split(" ")[0];

        Selenide.open("/transfer");
        Selenide.sleep(2000);
        Selenide.refresh();
        Selenide.sleep(2000);

        $("select.account-selector").selectOption(1);
        $("[placeholder='Enter recipient name']").sendKeys(user.getUsername());
        $("[placeholder='Enter recipient account number']").sendKeys(accountNumber);
        $("[placeholder='Enter amount']").setValue("100.00");
        $(Selectors.withText("Send Transfer")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("confirm");
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
        assertThat(accounts).hasSize(2);
        assertThat(accounts[0].getBalance()).isCloseTo(500.00, within(0.001));
        assertThat(accounts[1].getBalance()).isZero();
    }

    @Test
    @DisplayName("Трансфер с пустой суммой — ошибка")
    public void transferEmptyAmount() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);
        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);

        Selenide.open("/deposit");
        Selenide.sleep(1000);
        $("select.account-selector").selectOption(2);
        String accountNumber = $("select.account-selector").getSelectedOptionText().split(" ")[0];

        Selenide.open("/transfer");
        Selenide.sleep(2000);
        Selenide.refresh();
        Selenide.sleep(2000);

        $("select.account-selector").selectOption(1);
        $("[placeholder='Enter recipient name']").sendKeys(user.getUsername());
        $("[placeholder='Enter recipient account number']").sendKeys(accountNumber);
        $("#confirmCheck").click();
        $(Selectors.withText("Send Transfer")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("fill all fields");
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
        assertThat(accounts).hasSize(2);
        assertThat(accounts[0].getBalance()).isZero();
        assertThat(accounts[1].getBalance()).isZero();
    }
}