package dataaccess;


public class DataAccessExceptMod extends Exception{
    private final int statusCode;
    public DataAccessExceptMod(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode(){
        return this.statusCode;
    }
}






