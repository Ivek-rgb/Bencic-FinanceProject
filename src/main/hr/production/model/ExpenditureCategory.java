package main.hr.production.model;

import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ExpenditureCategory extends NamedEntity implements Serializable {
    protected transient Color appendedColorForReference;

    public static final Map<String, String> MAPPED_FIELDS = new TreeMap<>(Map.of("name", "Name"));

    public static final ExpenditureCategory noValueCategory = new ExpenditureCategory(-1L, "no category", Color.valueOf("#ffffff"));

    public static final ExpenditureCategory incomeCategory = new ExpenditureCategory(-2L, "Income", Color.GREEN);

    public ExpenditureCategory(Long objectID, String name) {
        super(objectID, name);
    }

    public static class ExpenditureCategoryBuilder extends NamedEntity{

        private Color representativeColor;
        public ExpenditureCategoryBuilder(String name) {
            super(-1L, name);
        }

        public ExpenditureCategoryBuilder withID(Long id){
            this.setObjectID(id);
            return this;
        }

        public ExpenditureCategoryBuilder withRepresentativeColor(Color color){
            this.representativeColor = color;
            return this;
        }

        public ExpenditureCategory finishBuilding(){
            return new ExpenditureCategory(getObjectID(), name, representativeColor);
        }

    }

    public ExpenditureCategory(Long objectID, String name, Color appendedColorForReference) {
        super(objectID, name);
        this.appendedColorForReference = appendedColorForReference;
    }

    public String getAppendedColorInHexString(){
        return String.format("#%02X%02X%02X", (int) (appendedColorForReference.getRed() * 255), (int) (appendedColorForReference.getGreen() * 255), (int) (appendedColorForReference.getBlue() * 255));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExpenditureCategory expenditureCategory){
            return this.getName().equals(expenditureCategory.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Color getAppendedColorForReference() {
        return appendedColorForReference;
    }

    public void setAppendedColorForReference(Color appendedColorForReference) {
        this.appendedColorForReference = appendedColorForReference;
    }

    public void setAppendedColorForReference(String appendedColorForReference){
        this.appendedColorForReference = Color.valueOf(appendedColorForReference);
    }

}
