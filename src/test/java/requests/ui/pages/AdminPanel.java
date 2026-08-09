package requests.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;
import requests.ui.pages.BasePage;
import requests.ui.pages.UserBage;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    private SelenideElement adminPanelText =  $(Selectors.byText("Admin Panel"));
    private SelenideElement addUserButton = $("button.btn-primary");

    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password) {
        usernameInput.shouldBe(Condition.visible, Duration.ofSeconds(10)).sendKeys(username);
        passwordInput.shouldBe(Condition.visible, Duration.ofSeconds(10)).sendKeys(password);
        addUserButton.shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        return this;
    }

    public List<UserBage> getAllUsers() {
        ElementsCollection elementsCollection =  $(Selectors.byText("All Users")).parent().findAll("li");
        return generatePageElements(elementsCollection, UserBage::new);
    }

    public UserBage findUserByUsername(String username) {
        return RetryUtils.retry(
                () -> getAllUsers().stream().filter(it -> it.getUsername().equals(username)).findAny().orElse(null),
                result -> result != null,
                3,
                1000
        );
    }
}