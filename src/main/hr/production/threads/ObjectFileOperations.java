package main.hr.production.threads;

import main.hr.production.fileutils.ObjectSerializationUtils;
import main.hr.production.model.Change;

import java.io.Serializable;
import java.util.List;

public class ObjectFileOperations {

    private static final Object MUTEX_OBJ = new Object();

    public <T extends Serializable> void writeObjToFile(List<T> objects){
        synchronized (MUTEX_OBJ){
            ObjectSerializationUtils.serializeObjects(objects);
        }
    }

    public <T extends Serializable> List<Change> readChangesFromFile(){
        synchronized (MUTEX_OBJ){
            return ObjectSerializationUtils.deserializeObjects(Change.class);
        }
    }

}

