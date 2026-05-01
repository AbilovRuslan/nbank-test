package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyUi {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.103:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @Test
    @DisplayName("Успешный депозит на 500.00")
    public void depositValidAmount() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        Alert alert = switchTo().alert();
        alert.accept();

        Selenide.open("/deposit");
        Selenide.refresh();
        Selenide.sleep(1000);

        $("select.account-selector").selectOption(1);
        $("input.deposit-input").setValue("500.00");
        $(Selectors.byText("💵 Deposit")).click();

        Alert success = switchTo().alert();
        assertThat(success.getText()).contains("Successful");
        success.accept();
    }

    @Test
    @DisplayName("Депозит на минимальную сумму (0.01)")
    public void depositMinAmount() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        Alert alert = switchTo().alert();
        alert.accept();

        Selenide.open("/deposit");
        Selenide.refresh();
        Selenide.sleep(1000);

        $("select.account-selector").selectOption(1);
        $("input.deposit-input").setValue("0.01");
        $(Selectors.byText("💵 Deposit")).click();

        Alert success = switchTo().alert();
        assertThat(success.getText()).contains("Successfully");
        success.accept();
    }

    @Test
    @DisplayName("Депозит на максимальную сумму (5000)")
    public void depositMaxAmount() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        Alert alert = switchTo().alert();
        alert.accept();

        Selenide.open("/deposit");
        Selenide.refresh();
        Selenide.sleep(1000);

        $("select.account-selector").selectOption(1);
        $("input.deposit-input").setValue("5000.00");
        $(Selectors.byText("💵 Deposit")).click();

        Alert success = switchTo().alert();
        assertThat(success.getText()).contains("Successfully");
        success.accept();
    }

    @Test
    @DisplayName("Депозит с пустым полем — ошибка")
    public void depositEmptyAmount() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        Alert alert = switchTo().alert();
        alert.accept();

        Selenide.open("/deposit");
        Selenide.refresh();
        Selenide.sleep(1000);

        $("select.account-selector").selectOption(1);
        $(Selectors.byText("💵 Deposit")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("valid amount");
        error.accept();
    }

    @Test
    @DisplayName("Депозит с отрицательной суммой — ошибка")
    public void depositNegativeAmount() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byText("➕ Create New Account")).click();
        Alert alert = switchTo().alert();
        alert.accept();

        Selenide.open("/deposit");
        Selenide.refresh();
        Selenide.sleep(1000);

        $("select.account-selector").selectOption(1);
        $("input.deposit-input").setValue("-100");
        $(Selectors.byText("💵 Deposit")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("valid amount");
        error.accept();
    }



}