package main.hr.production.fileutils;

import main.hr.production.logback.SessionLogger;

import java.io.*;
import java.util.*;

public class ObjectSerializationUtils {

    private static final String PATH_TO_CHANGE_SERIALIZATION_FILE = "dat/changes.dat";

    public static <T extends Serializable> void serializeObjects(Collection<T> collectionOfObjects){
        try(ObjectOutputStream objectWriter = new ObjectOutputStream(new FileOutputStream(PATH_TO_CHANGE_SERIALIZATION_FILE))){
            collectionOfObjects.forEach(object -> {
                try {
                    objectWriter.writeObject(object);
                } catch (IOException e) {
                    SessionLogger.sessionLogger.error("Failed to completely serialize objects", e);
                }
            });
        } catch (IOException e) {
            SessionLogger.sessionLogger.error("Failure to open file for serialization", e);
        }
    }

    public static <T extends Serializable> List<T> deserializeObjects(Class<T> clazz){
        try(ObjectInputStream objectReader = new ObjectInputStream(new FileInputStream(PATH_TO_CHANGE_SERIALIZATION_FILE))){
            List<T> listToBeFilled = new ArrayList<>();
            try{
                while(true) listToBeFilled.add((T) objectReader.readObject());
            }catch (EOFException e){
                return listToBeFilled;
            }
        } catch (IOException e) {
            if(!(e instanceof EOFException))
                SessionLogger.sessionLogger.error("Failure to open file for deserialization", e);
            return new ArrayList<>();
        } catch (ClassNotFoundException e) {
            SessionLogger.sessionLogger.error(e.getMessage());
            return new ArrayList<>();
        }
    }

}