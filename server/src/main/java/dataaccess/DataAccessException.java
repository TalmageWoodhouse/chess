package dataaccess;


public class DataAccessException extends Exception{
  private final int statusCode;

  public DataAccessException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode(){
    return this.statusCode;
  }
}