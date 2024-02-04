package main.hr.production.model;
import java.math.BigDecimal;

public record CurrencyEntryRecord(BigDecimal euroRatio, String shortForCurrency, String description) {
    public String toInformativeString(){
        return shortForCurrency + " - " + description;
    }

}