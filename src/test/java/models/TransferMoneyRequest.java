package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferMoneyRequest extends BaseModel {

    private Long fromAccountId;
    private Long toAccountId;
    private Double amount;
}