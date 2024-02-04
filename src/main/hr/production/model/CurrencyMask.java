package main.hr.production.model;

import main.hr.production.exceptions.checked.CurrencyNotFoundException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class CurrencyMask implements Serializable, Comparable<CurrencyMask> {

//    @Serial
//    private static final long serialVersionUID = 6529685098267757690L;

    public static String defaultConversionValue = "EUR";

    private static Map<String, CurrencyEntryRecord> conversionMap = new HashMap<>();

    private BigDecimal amount = BigDecimal.ZERO;

    public CurrencyMask(BigDecimal amount) {
        this.amount = amount;
    }

    public CurrencyMask(BigDecimal amount, String converterValue){
        this.amount = amount.multiply(conversionMap.get(converterValue).euroRatio(), MathContext.DECIMAL64);
    }

    public BigDecimal getChangedAmount() {
        return amount.divide(conversionMap.get(defaultConversionValue).euroRatio(), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount(){
        return amount;
    }

    public String getTextualAmount(){
        return getChangedAmount() + " " + defaultConversionValue;
    }

    public BigDecimal getCurrencyRatioToEuro(String chosenCurrency) throws CurrencyNotFoundException {
        if(!conversionMap.containsKey(chosenCurrency))
            throw new CurrencyNotFoundException(chosenCurrency);
        return conversionMap.get(chosenCurrency).euroRatio();
    }

    public void addOntoAmount(BigDecimal amount){
        this.amount = this.amount.add(amount);
    }
    public void addOntoAmount(Double amount){
        addOntoAmount(BigDecimal.valueOf(amount));
    }

    public void subtractFromAmount(CurrencyMask currencyMask){
        this.amount = this.amount.subtract(currencyMask.getAmount());
    }

    public void addOntoAmount(CurrencyMask currencyMask){
        this.amount = this.amount.add(currencyMask.getAmount());
    }

    public static CurrencyMask addTwoCurrencyMasks(CurrencyMask currencyMask1, CurrencyMask currencyMask2){
        currencyMask1.addOntoAmount(currencyMask1);
        return currencyMask1;
    }

    public static CurrencyMask subtractTwoCurrencyMasks(CurrencyMask currencyMask1, CurrencyMask currencyMask2){
        currencyMask1.subtractFromAmount(currencyMask2);
        return currencyMask2;
    }

    public void subtractFromAmount(BigDecimal amount){
        this.amount = this.amount.subtract(amount);
    }

    public void subtractFromAmount(Double amount){
        subtractFromAmount(BigDecimal.valueOf(amount));
    }

    public static void setDefaultConversionValue(String defaultConversionValue) {
        CurrencyMask.defaultConversionValue = defaultConversionValue;
    }

    public static Map<String, CurrencyEntryRecord> getConversionMap() {
        return conversionMap;
    }

    public static void setConversionMap(Map<String, CurrencyEntryRecord> conversionMap) {
        CurrencyMask.conversionMap = conversionMap;
    }

    @Override
    public String toString() {
        return "AMOUNT: " + amount + " default in EUR";
    }

    @Override
    public int compareTo(CurrencyMask o) {
        return this.getAmount().compareTo(o.getAmount());
    }
}
