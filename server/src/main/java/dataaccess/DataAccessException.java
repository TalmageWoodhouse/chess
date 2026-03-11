package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception {
    private final int statusCode;
    public DataAccessException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public DataAccessException(String message, Exception ex) {
        super(message, ex);
        statusCode = 500;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
