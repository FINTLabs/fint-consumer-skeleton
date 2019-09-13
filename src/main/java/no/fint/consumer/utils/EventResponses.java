package no.fint.consumer.utils;

import no.fint.consumer.exceptions.EventResponseException;
import no.fint.event.model.Event;
import org.springframework.http.HttpStatus;

public class EventResponses {

    public static Event<?> handle(Event<?> event) {
        if (event == null || event.getResponseStatus() == null)
            throw new EventResponseException(HttpStatus.INTERNAL_SERVER_ERROR, null);
        switch (event.getResponseStatus()) {
            case ERROR:
                throw new EventResponseException(HttpStatus.INTERNAL_SERVER_ERROR, event.getResponse());
            case REJECTED:
                switch (event.getStatusCode()) {
                    case "GONE":
                        throw new EventResponseException(HttpStatus.GONE, event.getResponse());
                    case "NOT_FOUND":
                        throw new EventResponseException(HttpStatus.NOT_FOUND, event.getResponse());
                    default:
                        throw new EventResponseException(HttpStatus.BAD_REQUEST, event.getResponse());
                }
            case CONFLICT:
                throw new EventResponseException(HttpStatus.CONFLICT, event.getResponse());
        }
        return event;
    }
}
