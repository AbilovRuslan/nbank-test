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
            models.CreateAccountResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "/api/v1/customer/accounts",
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
    ),

    TRANSFER(
            "/api/v1/accounts/transfer",
            TransferMoneyRequest.class,
            AccountInfoResponse.class
    ),

    CUSTOMER_PROFILE(
            "/api/v1/customer/profile",
            BaseModel.class,
            UserProfileResponse.class
    );

    private final String url;
    private final Class<?> requestModel;
    private final Class<?> responseModel;
}