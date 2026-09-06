package generators;



import models.CreateAccountResponse;
import models.CreateUserRequest;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;

import static constants.TestConstants.MAX_DEPOSIT_LIMIT;
import static constants.TestConstants.MIN_VALID_DEPOSIT;

public final class TestDataGenerator {

    private TestDataGenerator() {}

    public static double generateDepositAmount() {
        return MIN_VALID_DEPOSIT + Math.random() * (MAX_DEPOSIT_LIMIT - MIN_VALID_DEPOSIT);
    }

    public static TestUserData createTestUserAndAccount() {
        CreateUserRequest user = AdminSteps.createUser();
        AccountSteps accountSteps = new AccountSteps(user.getUsername(), user.getPassword());
        CreateAccountResponse account = accountSteps.createAccount();

        double depositAmount = generateDepositAmount();
        accountSteps.depositToAccount(account.getId(), depositAmount);

        return new TestUserData(user, accountSteps, account, depositAmount);
    }

    public static class TestUserData {
        private final CreateUserRequest user;
        private final AccountSteps accountSteps;
        private final CreateAccountResponse account;
        private final double depositAmount;

        public TestUserData(CreateUserRequest user, AccountSteps accountSteps,
                            CreateAccountResponse account, double depositAmount) {
            this.user = user;
            this.accountSteps = accountSteps;
            this.account = account;
            this.depositAmount = depositAmount;
        }

        public CreateUserRequest getUser() { return user; }
        public AccountSteps getAccountSteps() { return accountSteps; }
        public CreateAccountResponse getAccount() { return account; }
        public double getDepositAmount() { return depositAmount; }
    }
}