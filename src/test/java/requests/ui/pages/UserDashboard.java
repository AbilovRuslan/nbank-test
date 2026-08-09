package requests.ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

@Getter
public class UserDashboard extends requests.ui.pages.BasePage<UserDashboard> {

    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement accountSelector = $("select.account-selector");
    private SelenideElement depositInput = $("input.deposit-input");
    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));
    private SelenideElement recipientNameInput = $("[placeholder='Enter recipient name']");
    private SelenideElement recipientAccountInput = $("[placeholder='Enter recipient account number']");
    private SelenideElement transferAmountInput = $("[placeholder='Enter amount']");
    private SelenideElement confirmCheckbox = $("#confirmCheck");
    private SelenideElement transferButton = $(Selectors.byText("🚀 Send Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard openDeposit() {
        Selenide.open("/deposit");
        Selenide.refresh();
        Selenide.sleep(500);
        return this;
    }

    public UserDashboard selectFirstAccount() {
        accountSelector.selectOption(1);
        return this;
    }

    public UserDashboard enterAmount(double amount) {
        depositInput.setValue(String.valueOf(amount));
        return this;
    }

    public UserDashboard submitDeposit() {
        depositButton.click();
        return this;
    }

    public UserDashboard openTransfer() {
        Selenide.open("/transfer");
        Selenide.refresh();
        Selenide.sleep(500);
        return this;
    }

    public UserDashboard enterRecipientName(String name) {
        recipientNameInput.setValue(name);
        return this;
    }

    public UserDashboard enterRecipientAccount(String account) {
        recipientAccountInput.setValue(account);
        return this;
    }

    public UserDashboard enterTransferAmount(double amount) {
        transferAmountInput.setValue(String.valueOf(amount));
        return this;
    }

    public UserDashboard confirm() {
        confirmCheckbox.click();
        return this;
    }

    public UserDashboard submitTransfer() {
        transferButton.click();
        return this;
    }

    @Override
    public UserDashboard checkAlertMessageAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return this;
    }

    private SelenideElement newNameInput = $("[placeholder='Enter new name']");
    private SelenideElement saveChangesButton = $(Selectors.byText("💾 Save Changes"));

    public UserDashboard openEditProfile() {
        Selenide.open("/edit-profile");
        Selenide.sleep(500);
        return this;
    }

    public UserDashboard enterNewName(String name) {
        newNameInput.setValue(name);
        return this;
    }

    public UserDashboard saveChanges() {
        saveChangesButton.click();
        return this;
    }
}