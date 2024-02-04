package main.hr.production.model;

public abstract class NamedEntity extends IdentifiableObject{

    protected String name;

    public NamedEntity(Long objectID, String name) {
        super(objectID);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
