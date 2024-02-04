package main.hr.production.exceptions.checked;

public class CurrencyNotFoundException extends Exception{

    public CurrencyNotFoundException(String inputCurrency) {
        super(inputCurrency + " not found in currency archive");
    }

    public CurrencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrencyNotFoundException(Throwable cause) {
        super(cause);
    }

    public CurrencyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
