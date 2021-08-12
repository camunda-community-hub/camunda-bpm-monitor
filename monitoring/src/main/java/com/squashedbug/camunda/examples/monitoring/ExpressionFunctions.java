package com.squashedbug.camunda.examples.monitoring;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class ExpressionFunctions {

    public String getRandomDateWithinSeconds(int seconds) {
        ZonedDateTime d = ZonedDateTime.now().plusSeconds(new Random().nextInt(seconds) + 1);
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(d);
    }

    public void throwException() throws Exception {
        throw new Exception("an exception");
    }
}