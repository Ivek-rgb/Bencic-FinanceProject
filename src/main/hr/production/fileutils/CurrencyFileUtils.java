package main.hr.production.fileutils;

import main.hr.production.model.CurrencyEntryRecord;
import main.hr.production.model.CurrencyMask;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CurrencyFileUtils extends FileUtils {

    public static final String CURRENCY_INFO_PATH = "dat/currencyvals.txt";

    public static void updateCurrencyConversionMap(Stream<String> readLines){

        Map<String, CurrencyEntryRecord> updatedConversionMap = readLines.map(line -> line.split("="))
                .collect(Collectors.toMap(arr -> arr[0].split("-")[0], arr -> {
                    String[] values = arr[0].split("-");
                    return new CurrencyEntryRecord(new BigDecimal(arr[1]), values[0], values[1]);
                }));

        CurrencyMask.setConversionMap(updatedConversionMap);
    }

}