package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/api/v1/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/api/v1/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/api/v1/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    ACCOUNT_BY_ID(
            "/api/v1/accounts/{id}",
            null,
            AccountInfoResponse.class
    ),

    DEPOSIT(
            "/api/v1/accounts/deposit",
            DepositMoneyRequest.class,
            AccountInfoResponse.class
    );

    private final String url;
    private final Class<?> requestModel;
    private final Class<?> responseModel;
}