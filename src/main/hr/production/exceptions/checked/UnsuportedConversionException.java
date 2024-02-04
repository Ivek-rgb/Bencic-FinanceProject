package main.hr.production.exceptions.checked;

public class UnsuportedConversionException extends Exception{

    public UnsuportedConversionException() {
    }

    public UnsuportedConversionException(String message) {
        super(message);
    }

    public UnsuportedConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsuportedConversionException(Throwable cause) {
        super(cause);
    }

    public UnsuportedConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
