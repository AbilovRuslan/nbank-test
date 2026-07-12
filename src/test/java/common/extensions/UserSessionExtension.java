package common.extensions;

import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import models.CreateUserRequest;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import requests.steps.AdminSteps;

import java.util.LinkedList;
import java.util.List;

public class UserSessionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation == null) {
            return;
        }

        int userCount = annotation.value();
        SessionStorage.clear();

        List<CreateUserRequest> users = new LinkedList<>();
        for (int i = 0; i < userCount; i++) {
            users.add(AdminSteps.createUser());
        }
        SessionStorage.addUsers(users);

        int authIndex = annotation.auth();
        CreateUserRequest userToAuth = SessionStorage.getUser(authIndex);

        // Вызываем метод authAsUser у самого теста (он унаследован от BaseUiTest)
        Object testInstance = extensionContext.getRequiredTestInstance();
        if (testInstance instanceof BaseUiTest) {
            ((BaseUiTest) testInstance).authAsUser(userToAuth);
        }
    }
}