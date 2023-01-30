package com.lingh.other;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class Foo {
    public static void printDateTime(ZonedDateTime date) {
        System.out.println("printDateTime: " + date + " @ " + date.toEpochSecond());
    }

    public static void printDate(LocalDate date) {
        System.out.println("printDate: " + date);
    }
}