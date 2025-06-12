package com.mayorman.employees.utils;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Date;
public class TimeUtil  {
    public static Timestamp now(){
        Date date = new Date();
        return new Timestamp(date.getTime());
    }

    public static String getIsoTime(Timestamp timestamp){
        if (timestamp == null){
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return timestamp.toLocalDateTime().format(formatter);
    }
}
