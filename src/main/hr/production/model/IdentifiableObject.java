package main.hr.production.model;

import java.io.Serializable;

public abstract class IdentifiableObject implements Serializable {

    private Long objectID;

    public IdentifiableObject(Long objectID) {
        this.objectID = objectID;
    }

    public Long getObjectID() {
        return objectID;
    }

    public void setObjectID(Long objectID) {
        this.objectID = objectID;
    }

    public boolean equalsID(Long id){
        return this.objectID.equals(id);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IdentifiableObject identifiableObject){
            return this.objectID.equals(identifiableObject.objectID);
        }
        return false;
    }

}
