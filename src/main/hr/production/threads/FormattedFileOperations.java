package main.hr.production.threads;

import main.hr.production.fileutils.AccountFileUtils;

import java.util.List;

public sealed interface FormattedFileOperations<T> permits AccountFileOperations, CurrencyFileOperations {

    void writeObjsIntoFile(T obj);

    List<T> readObjsFromFile();

}
