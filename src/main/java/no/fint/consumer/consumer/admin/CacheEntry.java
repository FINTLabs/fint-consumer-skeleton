package no.fint.consumer.consumer.admin;

import lombok.Data;

import java.util.Date;

@Data
public class CacheEntry {
    private final Date lastUpdated;
    private final Integer size;
}
