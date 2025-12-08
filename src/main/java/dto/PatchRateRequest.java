package dto;

import java.math.BigDecimal;

public class PatchRateRequest {

    private BigDecimal rate;

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
