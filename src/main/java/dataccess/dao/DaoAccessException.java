package dataccess.dao;

public class DaoAccessException extends Exception {

    public DaoAccessException(String message) {
        super(message);
    }

    public DaoAccessException(Exception e) {
        super(e);
    }

}
