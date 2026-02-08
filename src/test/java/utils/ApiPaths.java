package utils;

public final class ApiPaths {

    private ApiPaths() {
        // Utility class
    }

    public static final class Admin {
        public static final String CREATE_USER = "/api/v1/admin/users";

        private Admin() {}
    }

    public static final class Auth {
        public static final String LOGIN = "/api/v1/auth/login";

        private Auth() {}
    }

    public static final class Account {
        public static final String CREATE_ACCOUNT = "/api/v1/accounts";
        public static final String DEPOSIT = "/api/v1/accounts/deposit";

        private Account() {}
    }

    public static final class Customer {
        public static final String PROFILE = "/api/v1/customer/profile";

        private Customer() {}
    }
}