package dataccess.utils;

import org.springframework.stereotype.Controller;

import javax.faces.convert.Converter;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.IntegerConverter;

@Controller
public class Type {

    public enum Types {
        NUMBER,
        STRING,
        DATE
    }

    public static Converter getConverter(Types type){
        switch (type) {
            case DATE:
                return new DateTimeConverter();
            case NUMBER:
                return new IntegerConverter();
            case STRING:
                return null;
        }
        return null;
    }

    public static boolean isString(Types type) {
        switch (type) {
            case STRING:
            case DATE:
                return true;
            case NUMBER:
                return false;
        }
        return true;
    }

    public static Types getType(Object o) {
        if (o instanceof String) return Types.STRING;
        else if (o instanceof Integer || o instanceof Double || o instanceof Float) return Types.NUMBER;
        else return Types.DATE;
    }
}
