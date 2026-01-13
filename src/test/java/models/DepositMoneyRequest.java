package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositMoneyRequest {
    @JsonProperty("id")
    private Long accountId;

    @JsonProperty("balance")
    private Double amount;
}