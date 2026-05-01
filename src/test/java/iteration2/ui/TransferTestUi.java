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

        // Создать два счёта через UI
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
    }
    @Test
    @DisplayName("Трансфер с недостаточным балансом — ошибка")
    public void transferInsufficientFunds() {
        CreateUserRequest user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // Создать два счёта
        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);
        $(Selectors.byText("➕ Create New Account")).click();
        switchTo().alert().accept();
        Selenide.sleep(500);

        // Узнать номер второго счёта
        Selenide.open("/deposit");
        Selenide.sleep(1000);
        $("select.account-selector").selectOption(2);
        String accountNumber = $("select.account-selector").getSelectedOptionText().split(" ")[0];

        // Трансфер БЕЗ депозита (баланс 0)
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
        // НЕ ставим чекбокс!
        $(Selectors.withText("Send Transfer")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("confirm");
        error.accept();
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
        // НЕ вводим сумму!
        $("#confirmCheck").click();
        $(Selectors.withText("Send Transfer")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("fill all fields");
        error.accept();
    }
}