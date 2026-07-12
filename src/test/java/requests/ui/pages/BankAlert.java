package requests.ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    DEPOSIT_SUCCESSFUL("Successfully deposited"),
    INVALID_DEPOSIT_AMOUNT("valid amount"),
    DEPOSIT_EXCEEDS_LIMIT("less or equal to 5000"),
    TRANSFER_SUCCESSFUL("Successfully transferred"),
    INSUFFICIENT_FUNDS("insufficient"),
    CONFIRM_TRANSFER("confirm"),
    FILL_ALL_FIELDS("fill all fields"),
    NAME_UPDATED("updated"),
    INVALID_NAME("Name must contain"),
    VALID_NAME_REQUIRED("valid name");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}