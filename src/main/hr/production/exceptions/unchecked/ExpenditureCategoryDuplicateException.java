package main.hr.production.exceptions.unchecked;

public class ExpenditureCategoryDuplicateException extends RuntimeException{
    public ExpenditureCategoryDuplicateException() {
    }

    public ExpenditureCategoryDuplicateException(String message) {
        super(message);
    }

    public ExpenditureCategoryDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExpenditureCategoryDuplicateException(Throwable cause) {
        super(cause);
    }

    public ExpenditureCategoryDuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
