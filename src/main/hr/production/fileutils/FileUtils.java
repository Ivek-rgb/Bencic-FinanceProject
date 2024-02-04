package main.hr.production.fileutils;

import main.hr.production.logback.SessionLogger;
import main.hr.production.model.IdentifiableObject;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class FileUtils {

    protected static <T extends IdentifiableObject> List<T> templateListReadCreator(String pathToFileStr, Function<BufferedReader, Optional<T>> optionalSupplier){
        List<T> listOfCategories = new ArrayList<>();
        try(BufferedReader lineReader = new BufferedReader(new FileReader(pathToFileStr))){
            Optional<T> tempCat;
            while ((tempCat = optionalSupplier.apply(lineReader)).isPresent()) {
                listOfCategories.add(tempCat.get());
            }
        }catch (IOException e){
            SessionLogger.sessionLogger.error(e.getMessage());
        }
        return listOfCategories;
    }

    protected static <T extends IdentifiableObject> List<T> templateReadObjFromFormattedFile(String pathToFileStr, Integer numberOfLinesPerObj, Function<List<String>, T> objectFactory){
        List<T> listOfObjects = new ArrayList<>();
        try(Stream<String> streamOfStrings = Files.lines(Path.of(pathToFileStr))){
            List<String> listOfStrings = streamOfStrings.toList();
            long numOfObjects = listOfStrings.size() / numberOfLinesPerObj;
            Stream.iterate(0, i -> i + 1).limit(numOfObjects).forEach(idx -> listOfObjects.add(objectFactory.apply(listOfStrings.subList(idx * numberOfLinesPerObj, (idx * numberOfLinesPerObj + numberOfLinesPerObj)))));
        } catch (IOException e) {
            SessionLogger.sessionLogger.error(e.getMessage());
        }
        return listOfObjects;
    }

    protected static <T extends IdentifiableObject> Long returnLatestIdentification(String pathToFileStr, Integer numberOfLinesPerObj){
        try(Stream<String> lines = Files.lines(Path.of(pathToFileStr))){
            List<String> listOfLines = lines.toList();
            return !listOfLines.isEmpty() ? Long.parseLong(listOfLines.get(listOfLines.size() - numberOfLinesPerObj)) : 0L;
        } catch (IOException e) {
            SessionLogger.sessionLogger.error("Fatal error while trying to read latest identification", e);
            throw new RuntimeException(e);
        }
    }

    protected static <T extends IdentifiableObject> void templateWriteObjToFormattedFile(String pathToFileStr, Collection<T>  objCollection, Function<T, String> writableBufferFactory, boolean append, Integer numberOfLinesPerObj){
            objCollection.forEach(object -> {
                try {
                    Long realId = returnLatestIdentification(pathToFileStr, numberOfLinesPerObj);
                    Files.writeString(Path.of(pathToFileStr), (realId + 1) + "\n" + writableBufferFactory.apply(object), append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE);
                } catch (IOException e) {
                    SessionLogger.sessionLogger.error("Error while writing to formatted file", e);
                }
            });
    }

    protected static <T extends IdentifiableObject> void templateWriteIdentifiedObjectsToFormattedFile(String pathFileStr, Collection<T> objCollection, Function<T, String> writableBufferFactory, boolean append){
        objCollection.forEach(object ->{
            try {
                Files.writeString(Path.of(pathFileStr), writableBufferFactory.apply(object), append ? StandardOpenOption.APPEND : StandardOpenOption.WRITE);
            } catch (IOException e) {
                SessionLogger.sessionLogger.error("Error while writing to formatted file", e);
            }
        });
    }

    protected static List<String> readAllLinesFromFile(Path path){
        try (Stream<String> linesRead = Files.lines(path)){
            return new ArrayList<>(linesRead.toList());
        } catch (IOException e) {
            SessionLogger.sessionLogger.error("Error while reading all lines formatted file", e);
            return new ArrayList<>();
        }
    }

}