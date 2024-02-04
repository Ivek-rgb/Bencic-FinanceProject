package main.hr.production.logback;

import main.hr.production.app.AccountControllers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionLogger {
    public static final Logger sessionLogger = LoggerFactory.getLogger("Session logger");

    public static void logExceptionOnCurrentAccount(Throwable throwable){
        sessionLogger.error("EXCEPTION THROWN ON" + AccountControllers.getCurrentLoginAccount(), throwable);
    }


}
