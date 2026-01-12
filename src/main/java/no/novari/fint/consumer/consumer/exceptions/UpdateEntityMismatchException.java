package no.novari.fint.consumer.consumer.exceptions;

@SuppressWarnings("ALL")
public class UpdateEntityMismatchException extends RuntimeException {
    public UpdateEntityMismatchException(String message) {
        super(message);
    }
}
