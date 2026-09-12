package iteration1.api;

import common.annotations.FraudCheckMock;
import iteration1.api.FraudCheckWireMockExtension;
import generators.TestDataGenerator;
import models.TransferResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static constants.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({FraudCheckWireMockExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransferWithFraudCheckTest extends BaseTest {

    private TestDataGenerator.TestUserData userData1;
    private TestDataGenerator.TestUserData userData2;
    private TransferResponse transferResponse;

    @Override
    @BeforeEach
    public void setupTest() {
        super.setupTest();
        userData1 = TestDataGenerator.createTestUserAndAccount();
        userData2 = TestDataGenerator.createTestUserAndAccount();
    }

    // APPROVED
    @Test
    @Order(1)
    @FraudCheckMock(status = FRAUD_STATUS_SUCCESS, decision = FRAUD_DECISION_APPROVED,
            riskScore = FRAUD_RISK_LOW, reason = FRAUD_REASON_LOW_RISK,
            requiresManualReview = false, additionalVerificationRequired = false)
    public void testTransferApproved_ShouldProcessImmediately() {
        double amount = generateTransferAmount(userData1.getDepositAmount());
        transferResponse = userData1.getAccountSteps().transferWithFraudCheck(
                String.valueOf(userData1.getAccount().getId()),
                String.valueOf(userData2.getAccount().getId()), amount);

        ModelAssertions.assertThatModels(TransferResponse.builder()
                .status(FRAUD_DECISION_APPROVED).message(MESSAGE_APPROVED_AND_PROCESSED)
                .amount(amount)
                .senderAccountId(String.valueOf(userData1.getAccount().getId()))
                .receiverAccountId(String.valueOf(userData2.getAccount().getId()))
                .fraudRiskScore(FRAUD_RISK_LOW).fraudReason(FRAUD_REASON_LOW_RISK)
                .requiresManualReview(false).requiresVerification(false).build(), transferResponse).match();
        assertTransferCompleted(amount);
    }

    // перевод заблокирован
    @Test
    @Order(2)
    @FraudCheckMock(status = FRAUD_STATUS_SUCCESS, decision = FRAUD_DECISION_BLOCKED,
            riskScore = FRAUD_RISK_CRITICAL, reason = FRAUD_REASON_HIGH_RISK,
            requiresManualReview = false, additionalVerificationRequired = false)
    public void testTransferBlocked_ShouldBlockTransaction() {
        double amount = generateTransferAmount(userData1.getDepositAmount());
        transferResponse = userData1.getAccountSteps().transferWithFraudCheck(
                String.valueOf(userData1.getAccount().getId()),
                String.valueOf(userData2.getAccount().getId()), amount);

        ModelAssertions.assertThatModels(TransferResponse.builder()
                .status(FRAUD_DECISION_BLOCKED).message(MESSAGE_BLOCKED_BY_FRAUD)
                .amount(amount)
                .senderAccountId(String.valueOf(userData1.getAccount().getId()))
                .receiverAccountId(String.valueOf(userData2.getAccount().getId()))
                .fraudRiskScore(FRAUD_RISK_CRITICAL).fraudReason(FRAUD_REASON_HIGH_RISK)
                .requiresManualReview(false).requiresVerification(false).build(), transferResponse).match();
        assertTransferNotCompleted();
    }

    // ручная проверка
    @Test
    @Order(3)
    @FraudCheckMock(status = FRAUD_STATUS_SUCCESS, decision = FRAUD_DECISION_REVIEW_REQUIRED,
            riskScore = FRAUD_RISK_HIGH, reason = FRAUD_REASON_MANUAL_REVIEW,
            requiresManualReview = true, additionalVerificationRequired = false)
    public void testTransferReviewRequired_ShouldGoToManualReview() {
        double amount = generateTransferAmount(userData1.getDepositAmount());
        transferResponse = userData1.getAccountSteps().transferWithFraudCheck(
                String.valueOf(userData1.getAccount().getId()),
                String.valueOf(userData2.getAccount().getId()), amount);

        ModelAssertions.assertThatModels(TransferResponse.builder()
                .status(FRAUD_STATUS_PENDING_REVIEW).message(MESSAGE_PENDING_MANUAL_REVIEW)
                .amount(amount)
                .senderAccountId(String.valueOf(userData1.getAccount().getId()))
                .receiverAccountId(String.valueOf(userData2.getAccount().getId()))
                .fraudRiskScore(FRAUD_RISK_HIGH).fraudReason(FRAUD_REASON_MANUAL_REVIEW)
                .requiresManualReview(true).requiresVerification(false).build(), transferResponse).match();
        assertTransferNotCompleted();
    }

    //  нужна верификация
    @Test
    @Order(4)
    @FraudCheckMock(status = FRAUD_STATUS_SUCCESS, decision = FRAUD_DECISION_VERIFICATION_REQUIRED,
            riskScore = FRAUD_RISK_MEDIUM, reason = FRAUD_REASON_VERIFICATION,
            requiresManualReview = false, additionalVerificationRequired = true)
    public void testTransferVerificationRequired_ShouldRequireAdditionalVerification() {
        double amount = generateTransferAmount(userData1.getDepositAmount());
        transferResponse = userData1.getAccountSteps().transferWithFraudCheck(
                String.valueOf(userData1.getAccount().getId()),
                String.valueOf(userData2.getAccount().getId()), amount);

        ModelAssertions.assertThatModels(TransferResponse.builder()
                .status(FRAUD_STATUS_PENDING_VERIFICATION).message(MESSAGE_VERIFICATION_REQUIRED)
                .amount(amount)
                .senderAccountId(String.valueOf(userData1.getAccount().getId()))
                .receiverAccountId(String.valueOf(userData2.getAccount().getId()))
                .fraudRiskScore(FRAUD_RISK_MEDIUM).fraudReason(FRAUD_REASON_VERIFICATION)
                .requiresManualReview(false).requiresVerification(true).build(), transferResponse).match();
        assertTransferNotCompleted();
    }

    //  таймаут
    @Test
    @Order(5)
    @FraudCheckMock(status = FRAUD_STATUS_ERROR, decision = FRAUD_DECISION_ERROR,
            riskScore = FRAUD_RISK_NONE, reason = FRAUD_REASON_TIMEOUT,
            requiresManualReview = false, additionalVerificationRequired = false,
            responseDelay = FRAUD_RESPONSE_DELAY, httpStatus = STATUS_INTERNAL_ERROR)
    public void testTransferServiceTimeout_ShouldFallbackToReview() {
        double amount = generateTransferAmount(userData1.getDepositAmount());
        transferResponse = userData1.getAccountSteps().transferWithFraudCheck(
                String.valueOf(userData1.getAccount().getId()),
                String.valueOf(userData2.getAccount().getId()), amount);

        ModelAssertions.assertThatModels(TransferResponse.builder()
                .status(FRAUD_STATUS_PENDING_REVIEW).message(MESSAGE_SERVICE_TEMPORARILY_UNAVAILABLE)
                .amount(amount)
                .senderAccountId(String.valueOf(userData1.getAccount().getId()))
                .receiverAccountId(String.valueOf(userData2.getAccount().getId()))
                .fraudRiskScore(FRAUD_RISK_NONE).fraudReason(FRAUD_REASON_TIMEOUT)
                .requiresManualReview(true).requiresVerification(false).build(), transferResponse).match();
        assertTransferNotCompleted();
    }

    // ошибка подключения
    @Test
    @Order(6)
    @FraudCheckMock(status = FRAUD_STATUS_ERROR, decision = FRAUD_DECISION_ERROR,
            riskScore = FRAUD_RISK_NONE, reason = FRAUD_REASON_CONNECTION_REFUSED,
            requiresManualReview = false, additionalVerificationRequired = false,
            httpStatus = STATUS_INTERNAL_ERROR)
    public void testTransferConnectionError_ShouldFallbackToReview() {
        double amount = generateTransferAmount(userData1.getDepositAmount());
        transferResponse = userData1.getAccountSteps().transferWithFraudCheck(
                String.valueOf(userData1.getAccount().getId()),
                String.valueOf(userData2.getAccount().getId()), amount);

        ModelAssertions.assertThatModels(TransferResponse.builder()
                .status(FRAUD_STATUS_PENDING_REVIEW).message(MESSAGE_SERVICE_UNAVAILABLE)
                .amount(amount)
                .senderAccountId(String.valueOf(userData1.getAccount().getId()))
                .receiverAccountId(String.valueOf(userData2.getAccount().getId()))
                .fraudRiskScore(FRAUD_RISK_NONE).fraudReason(FRAUD_REASON_CONNECTION_REFUSED)
                .requiresManualReview(true).requiresVerification(false).build(), transferResponse).match();
        assertTransferNotCompleted();
    }

    //  ошибка 500
    @Test
    @Order(7)
    @FraudCheckMock(status = FRAUD_STATUS_ERROR, decision = FRAUD_DECISION_ERROR,
            riskScore = FRAUD_RISK_NONE, reason = FRAUD_REASON_INTERNAL_ERROR,
            requiresManualReview = false, additionalVerificationRequired = false,
            httpStatus = STATUS_INTERNAL_ERROR)
    public void testTransferNon200Status_ShouldFallbackToReview() {
        double amount = generateTransferAmount(userData1.getDepositAmount());
        transferResponse = userData1.getAccountSteps().transferWithFraudCheck(
                String.valueOf(userData1.getAccount().getId()),
                String.valueOf(userData2.getAccount().getId()), amount);

        ModelAssertions.assertThatModels(TransferResponse.builder()
                .status(FRAUD_STATUS_PENDING_REVIEW).message(MESSAGE_SERVICE_ERROR)
                .amount(amount)
                .senderAccountId(String.valueOf(userData1.getAccount().getId()))
                .receiverAccountId(String.valueOf(userData2.getAccount().getId()))
                .fraudRiskScore(FRAUD_RISK_NONE).fraudReason(FRAUD_REASON_INTERNAL_ERROR)
                .requiresManualReview(true).requiresVerification(false).build(), transferResponse).match();
        assertTransferNotCompleted();
    }

    // ========== HELPERS ==========
    private double generateTransferAmount(double balance) {
        return MIN_TRANSFER + Math.random() * (balance - MIN_TRANSFER);
    }

    private void assertTransferCompleted(double amount) {
        var a1 = userData1.getAccountSteps().getAccount(String.valueOf(userData1.getAccount().getId()));
        var a2 = userData2.getAccountSteps().getAccount(String.valueOf(userData2.getAccount().getId()));
        assertThat(a1.getBalance()).isEqualTo(userData1.getDepositAmount() - amount);
        assertThat(a2.getBalance()).isEqualTo(amount);
    }

    private void assertTransferNotCompleted() {
        var a1 = userData1.getAccountSteps().getAccount(String.valueOf(userData1.getAccount().getId()));
        var a2 = userData2.getAccountSteps().getAccount(String.valueOf(userData2.getAccount().getId()));
        assertThat(a1.getBalance()).isEqualTo(userData1.getDepositAmount());
        assertThat(a2.getBalance()).isEqualTo(ZERO_AMOUNT);
    }
}