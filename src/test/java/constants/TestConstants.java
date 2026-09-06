package constants;

public class TestConstants {
    // ================= БИЗНЕС-ЛИМИТЫ =================
    public static final double MAX_DEPOSIT_LIMIT = 5000.0;
    public static final double MIN_VALID_DEPOSIT = 0.01;
    public static final double MIN_TRANSFER = 0.01;
    public static final double MIN_BALANCE = 0.01;

    // ================= СООБЩЕНИЯ ОБ ОШИБКАХ =================
    public static final String ERROR_MIN_DEPOSIT = "Deposit amount must be at least 0.01";
    public static final String ERROR_MAX_DEPOSIT = "Deposit amount cannot exceed 5000";
    public static final String ERROR_INVALID_AMOUNT = "Transfer amount must be at least 0.01";
    public static final String ERROR_INSUFFICIENT_FUNDS = "Invalid transfer: insufficient funds or invalid accounts";
    public static final String ERROR_ACCOUNT_NOT_FOUND = "Invalid transfer: insufficient funds or invalid accounts";
    public static final String ERROR_UNAUTHORIZED_ACCOUNT = "Unauthorized access to account";
    public static final String ERROR_NAME_REQUIRED = "Name must contain";
    public static final String ERROR_NAME_FORMAT = "must contain two words";

    // ================= ТЕСТОВЫЕ ЗНАЧЕНИЯ ДЛЯ DEPOSIT =================
    public static final long NON_EXISTENT_ACCOUNT_ID = 999999L;
    public static final double EXCEED_LIMIT_AMOUNT = 0.02;
    public static final double PRECISION_TEST_AMOUNT = 1234.56;
    public static final double ZERO_AMOUNT = 0.0;
    public static final double SMALL_NEGATIVE_AMOUNT = -0.01;
    public static final double MEDIUM_NEGATIVE_AMOUNT = -100.0;
    public static final double LARGE_NEGATIVE_AMOUNT = -999.99;
    public static final double SLIGHTLY_ABOVE_LIMIT = 5000.01;
    public static final double MODERATELY_ABOVE_LIMIT = 5000.1;
    public static final double FAR_ABOVE_LIMIT = 6000.0;
    public static final double EXTREME_ABOVE_LIMIT = 10000.0;

    // ================= ТЕСТОВЫЕ ЗНАЧЕНИЯ ДЛЯ TRANSFER =================
    public static final double TRANSFER_AMOUNT_SMALL = 10.0;
    public static final double TRANSFER_AMOUNT_MEDIUM = 100.50;
    public static final double TRANSFER_AMOUNT_LARGE = 1000.0;
    public static final double TRANSFER_AMOUNT_MAX = 2500.75;

    // ================= ДОПОЛНИТЕЛЬНЫЕ ЗНАЧЕНИЯ ДЛЯ ДЕПОЗИТОВ =================
    public static final double DEPOSIT_200 = 200.0;
    public static final double DEPOSIT_300 = 300.0;
    public static final double DEPOSIT_400 = 400.0;
    public static final double LIMIT_ROUNDING_ARTIFACT = 0.009;

    // ================= ПРОЦЕНТЫ ДЛЯ РАСЧЕТОВ =================
    public static final double TRANSFER_PERCENT_30 = 0.3;
    public static final double TRANSFER_PERCENT_70 = 0.7;
    public static final double TRANSFER_PERCENT_50 = 0.5;
    public static final double EXCEED_BALANCE_AMOUNT = 100.0;

    // ================= БАЛАНСЫ =================
    public static final double MIN_INITIAL_BALANCE = 500.0;
    public static final double MAX_INITIAL_BALANCE = 5000.0;

    // ================= ТЕСТОВЫЕ ИМЕНА (из UsernameUpdate) =================
    public static final String VALID_NAME_TWO_WORDS = "Ivan Ivanov";
    public static final String VALID_NAME_WITH_MIDDLE = "Anna Maria";
    public static final String VALID_NAME_ENGLISH = "John Doe";
    public static final String VALID_NAME_RUSSIAN = "Петр Петров";
    public static final String VALID_NAME_WITH_HYPHEN = "Anna-Maria Smith";

    public static final String INVALID_NAME_ONE_WORD = "Ivan";
    public static final String INVALID_NAME_THREE_WORDS = "Ivan Ivanov Petrovich";
    public static final String INVALID_NAME_SPACES = "   ";
    public static final String INVALID_NAME_EMPTY = "";
    public static final String INVALID_NAME_SPECIAL_CHARS = "Ivan@ Ivanov";
    public static final String INVALID_NAME_NUMBERS = "Ivan 123";
    public static final String INVALID_NAME_SYMBOLS = "Ivan!#$% Ivanov";
    public static final String INVALID_USERNAME_SHORT = "a";

    // ================= ЗНАЧЕНИЯ ДЛЯ АУТЕНТИФИКАЦИИ =================
    public static final long DEFAULT_USER_ID = 0L;
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String WELCOME_MESSAGE = "Welcome, noname!";

    // ================= HTTP СТАТУСЫ =================
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_CONFLICT = 409;
    public static final int STATUS_INTERNAL_ERROR = 500;

    // ================= ТЕХНИЧЕСКИЕ КОНСТАНТЫ =================
    public static final double DELTA = 0.001;
    public static final int MAX_DECIMAL_PLACES = 2;
    public static final int RANDOM_TEST_REPETITIONS = 5;
    public static final int PRECISION_MULTIPLIER = 100;

    // ================= МАССИВЫ ДЛЯ ПАРАМЕТРИЗАЦИИ DEPOSIT =================
    public static final Double[] VALID_DEPOSIT_AMOUNTS = {
            MIN_VALID_DEPOSIT,
            TRANSFER_AMOUNT_MEDIUM,
            TRANSFER_AMOUNT_MAX,
            MAX_DEPOSIT_LIMIT - DELTA,
            MAX_DEPOSIT_LIMIT
    };
    public static final double[] INVALID_DEPOSIT_AMOUNTS = {
            ZERO_AMOUNT,
            SMALL_NEGATIVE_AMOUNT,
            MEDIUM_NEGATIVE_AMOUNT,
            LARGE_NEGATIVE_AMOUNT,
            SLIGHTLY_ABOVE_LIMIT,
            MODERATELY_ABOVE_LIMIT,
            FAR_ABOVE_LIMIT,
            EXTREME_ABOVE_LIMIT
    };

    // ================= МАССИВЫ ДЛЯ ПАРАМЕТРИЗАЦИИ TRANSFER =================
    public static final double[] VALID_TRANSFER_AMOUNTS = {
            MIN_TRANSFER,
            TRANSFER_AMOUNT_MEDIUM,
            TRANSFER_AMOUNT_LARGE,
            TRANSFER_AMOUNT_MAX
    };

    public static final double[] INVALID_TRANSFER_AMOUNTS = {
            ZERO_AMOUNT,
            SMALL_NEGATIVE_AMOUNT,
            MEDIUM_NEGATIVE_AMOUNT,
            LARGE_NEGATIVE_AMOUNT
    };

    // ================= МАССИВЫ ДЛЯ ПАРАМЕТРИЗАЦИИ USERNAME =================
    public static final String[] VALID_USERNAMES = {
            VALID_NAME_TWO_WORDS,
            VALID_NAME_WITH_MIDDLE,
            VALID_NAME_ENGLISH,
            VALID_NAME_RUSSIAN,
            VALID_NAME_WITH_HYPHEN
    };

    public static final String[] INVALID_USERNAMES = {
            INVALID_NAME_ONE_WORD,
            INVALID_NAME_THREE_WORDS,
            INVALID_NAME_SPACES,
            INVALID_NAME_EMPTY,
            INVALID_NAME_SPECIAL_CHARS,
            INVALID_NAME_NUMBERS,
            INVALID_NAME_SYMBOLS
    };

    // ================= СЦЕНАРИИ МНОЖЕСТВЕННЫХ ДЕПОЗИТОВ =================
    public static final Object[][] MULTIPLE_DEPOSITS_SCENARIOS = {
            {"Three deposits", new Double[]{TRANSFER_AMOUNT_LARGE, TRANSFER_AMOUNT_MEDIUM, TRANSFER_AMOUNT_SMALL}, TRANSFER_AMOUNT_LARGE + TRANSFER_AMOUNT_MEDIUM + TRANSFER_AMOUNT_SMALL},
            {"Accumulated balance above deposit limit", new Double[]{MIN_VALID_DEPOSIT, MAX_DEPOSIT_LIMIT - DELTA}, MAX_DEPOSIT_LIMIT + LIMIT_ROUNDING_ARTIFACT},
            {"Four deposits", new Double[]{DEPOSIT_200, DEPOSIT_300, DEPOSIT_400, TRANSFER_AMOUNT_SMALL}, DEPOSIT_200 + DEPOSIT_300 + DEPOSIT_400 + TRANSFER_AMOUNT_SMALL}
    };

    // Приватный конструктор чтобы нельзя было создать экземпляр
    private TestConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }

    // ================= FRAUD DETECTION КОНСТАНТЫ =================

    // Статусы Fraud Service
    public static final String FRAUD_STATUS_SUCCESS = "SUCCESS";
    public static final String FRAUD_STATUS_ERROR = "ERROR";
    public static final String FRAUD_STATUS_PENDING_REVIEW = "PENDING_REVIEW";
    public static final String FRAUD_STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";

    // Решения Fraud Service
    public static final String FRAUD_DECISION_APPROVED = "APPROVED";
    public static final String FRAUD_DECISION_BLOCKED = "BLOCKED";
    public static final String FRAUD_DECISION_REVIEW_REQUIRED = "REVIEW_REQUIRED";
    public static final String FRAUD_DECISION_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    public static final String FRAUD_DECISION_ERROR = "ERROR";

    // Риск-скоры
    public static final double FRAUD_RISK_NONE = 0.0;
    public static final double FRAUD_RISK_LOW = 0.2;
    public static final double FRAUD_RISK_MEDIUM = 0.6;
    public static final double FRAUD_RISK_HIGH = 0.7;
    public static final double FRAUD_RISK_CRITICAL = 0.95;

    // Причины
    public static final String FRAUD_REASON_LOW_RISK = "Low risk transaction";
    public static final String FRAUD_REASON_HIGH_RISK = "High risk transaction detected";
    public static final String FRAUD_REASON_MANUAL_REVIEW = "Suspicious transaction requires manual review";
    public static final String FRAUD_REASON_VERIFICATION = "Additional verification required";
    public static final String FRAUD_REASON_TIMEOUT = "Service timeout";
    public static final String FRAUD_REASON_CONNECTION_REFUSED = "Connection refused";
    public static final String FRAUD_REASON_INTERNAL_ERROR = "Internal server error";

    // Сообщения в ответах
    public static final String MESSAGE_APPROVED_AND_PROCESSED = "approved and processed immediately";
    public static final String MESSAGE_BLOCKED_BY_FRAUD = "Blocked by fraud detection";
    public static final String MESSAGE_PENDING_MANUAL_REVIEW = "Pending manual review";
    public static final String MESSAGE_VERIFICATION_REQUIRED = "Additional verification required";
    public static final String MESSAGE_SERVICE_TEMPORARILY_UNAVAILABLE = "Service temporarily unavailable";
    public static final String MESSAGE_SERVICE_UNAVAILABLE = "Service unavailable";
    public static final String MESSAGE_SERVICE_ERROR = "Service error";

    // Настройки мока
    public static final int FRAUD_RESPONSE_DELAY = 5000;
}