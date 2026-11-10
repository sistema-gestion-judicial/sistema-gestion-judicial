package mx.edu.utez.judicial.audiencias.api.DAO;

public class DataAccesException extends RuntimeException {
    public DataAccesException(String message,Throwable cause){
        super(message,cause);
    }
}
