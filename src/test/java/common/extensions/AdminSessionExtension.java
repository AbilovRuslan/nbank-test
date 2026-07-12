package common.extensions;

import common.annotations.AdminSession;
import iteration1.ui.BaseUiTest;
import models.CreateUserRequest;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import requests.steps.AdminSteps;

public class AdminSessionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        AdminSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(AdminSession.class);
        if (annotation == null) {
            return;
        }

        // Создаём админа (если нужно) или используем готового
        CreateUserRequest admin = AdminSteps.createUser(); // или используем admin/admin

        Object testInstance = extensionContext.getRequiredTestInstance();
        if (testInstance instanceof BaseUiTest) {
            ((BaseUiTest) testInstance).authAsUser(admin);
        }
    }
}