package constants;

public class Endpoints {
    public static final String ADMIN_USERS = "/api/v1/admin/users";
    public static final String LOGIN = "/api/v1/auth/login";
    public static final String ACCOUNTS = "/api/v1/accounts";
    public static final String DEPOSIT = "/api/v1/accounts/deposit";
    public static final String TRANSFER = "/api/v1/accounts/transfer";
    public static final String CUSTOMER_PROFILE = "/api/v1/customer/profile";

    public static String accountById(Integer id) {
        return ACCOUNTS + "/" + id;
    }
}
