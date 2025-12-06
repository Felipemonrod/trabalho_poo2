package exceptions;

public class DashboardDataException extends RuntimeException {
    public DashboardDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
