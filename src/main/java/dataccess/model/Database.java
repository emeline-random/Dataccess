package dataccess.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class Database {

    private ArrayList<Table> tables;
    private String name;

    public Database(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
