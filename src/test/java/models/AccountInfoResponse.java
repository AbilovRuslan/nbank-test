
package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountInfoResponse extends BaseModel {
    private Long id;
    private String accountNumber;
    private Double balance;
    private Long userId;
    private List<Transaction> transactions;

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public Double getBalance() { return balance; }
    public Long getUserId() { return userId; }
    public List<Transaction> getTransactions() { return transactions; }

    public void setId(Long id) { this.id = id; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setBalance(Double balance) { this.balance = balance; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaction {
        private Long id;
        private Double amount;
        private String type;
        private String timestamp;
        private Long relatedAccountId;

        public Long getId() { return id; }
        public Double getAmount() { return amount; }
        public String getType() { return type; }
        public String getTimestamp() { return timestamp; }
        public Long getRelatedAccountId() { return relatedAccountId; }

        public void setId(Long id) { this.id = id; }
        public void setAmount(Double amount) { this.amount = amount; }
        public void setType(String type) { this.type = type; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public void setRelatedAccountId(Long relatedAccountId) { this.relatedAccountId = relatedAccountId; }
    }
}
