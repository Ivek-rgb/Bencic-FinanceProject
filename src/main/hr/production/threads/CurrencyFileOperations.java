package main.hr.production.threads;

import main.hr.production.fileutils.CurrencyFileUtils;
import main.hr.production.logback.SessionLogger;
import main.hr.production.model.CurrencyMask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public non-sealed class CurrencyFileOperations extends CurrencyFileUtils implements FormattedFileOperations<String>{

    protected final static Object MUTEX_OBJ = new Object();

    @Override
    public void writeObjsIntoFile(String obj) {
        synchronized (MUTEX_OBJ){
            try {
                Files.writeString(Path.of(CurrencyFileUtils.CURRENCY_INFO_PATH), obj);
            } catch (IOException e) {
                SessionLogger.sessionLogger.error(e.getMessage());
            }
        }
    }

    public void archiveCurrencyExchanges(){
        writeObjsIntoFile(CurrencyMask.getConversionMap().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("\n")));
    }

    public void updateCurrencyMap(Stream<String> updatedTokenizedValues){
        CurrencyFileUtils.updateCurrencyConversionMap(updatedTokenizedValues);
    }

    public void updateCurrencyMap(){
        synchronized (MUTEX_OBJ){
            CurrencyFileUtils.updateCurrencyConversionMap(readObjsFromFile().stream());
        }
    }

    @Override
    public List<String> readObjsFromFile() {
        synchronized (MUTEX_OBJ){
            try(Stream<String> streamOfLines = Files.lines(Path.of(CurrencyFileUtils.CURRENCY_INFO_PATH))){
                return streamOfLines.toList();
            } catch (IOException e) {
                SessionLogger.sessionLogger.error(e.getMessage());
                return new ArrayList<>();
            }
        }
    }

}
