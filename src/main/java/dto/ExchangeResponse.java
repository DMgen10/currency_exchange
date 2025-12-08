package dto;

import java.math.BigDecimal;

public class ExchangeResponse {

    private String from;
    private String to;
    private BigDecimal amount;
    private BigDecimal convertAmount;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getConvertAmount() {
        return convertAmount;
    }

    public void setConvertAmount(BigDecimal convertAmount) {
        this.convertAmount = convertAmount;
    }
}
