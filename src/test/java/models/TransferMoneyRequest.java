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
public class TransferMoneyRequest {
    @JsonProperty("fromAccountId")
    private Long fromAccountId;

    @JsonProperty("toAccountId")
    private Long toAccountId;

    @JsonProperty("amount")
    private Double amount;
}