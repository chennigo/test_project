package com.systelm.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.LongSupplier;

@Component
public class DocNoGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    public String next(String prefix, LongSupplier existingCount) {
        String datePart = LocalDate.now().format(DATE_FORMAT);
        String docPrefix = prefix + datePart;
        long count = existingCount.getAsLong();
        return docPrefix + String.format("%03d", count + 1);
    }
}
