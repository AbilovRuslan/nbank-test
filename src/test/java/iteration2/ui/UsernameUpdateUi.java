package iteration2.ui;

import com.codeborne.selenide.Condition;
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

        $("[placeholder='Enter new name']").shouldBe(Condition.visible).setValue("John Doe");
        $(Selectors.withText("Save Changes")).click();

        Alert alert = switchTo().alert();
        System.out.println("ALERT TEXT: " + alert.getText());
        assertThat(alert.getText()).contains("updated");
        alert.accept();

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
        $(Selectors.withText("Save Changes")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("must contain two words");
        error.accept();
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
        $(Selectors.withText("Save Changes")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("Name must contain two words with letters only");
        error.accept();
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
        $(Selectors.withText("Save Changes")).click();

        Alert error = switchTo().alert();
        assertThat(error.getText()).contains("valid");
        error.accept();
    }
}