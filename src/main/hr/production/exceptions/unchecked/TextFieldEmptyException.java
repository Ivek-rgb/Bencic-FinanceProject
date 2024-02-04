package main.hr.production.exceptions.unchecked;

public class TextFieldEmptyException extends RuntimeException{

    public TextFieldEmptyException() {
    }

    public TextFieldEmptyException(String message) {
        super(message);
    }

    public TextFieldEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public TextFieldEmptyException(Throwable cause) {
        super(cause);
    }

    public TextFieldEmptyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
