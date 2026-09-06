package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferResponse {
    private String status;
    private String message;
    private double amount;
    private String senderAccountId;
    private String receiverAccountId;
    private double fraudRiskScore;
    private String fraudReason;
    private boolean requiresManualReview;
    private boolean requiresVerification;
}