package com.mustafakahraman.popularmovies1.data;

import android.arch.persistence.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {

    @TypeConverter
    public static Date toDate(String strDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = format.parse(strDate);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }

        return date;
        //return strDate == null ? null : stringToDate(strDate);
    }

    @TypeConverter
    public static String toStringDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate;
        try {
            strDate = dateFormat.format(date);
            System.out.println("Current Date Time : " + strDate);
        } catch (Exception e) {
            e.printStackTrace();
            strDate = null;
        }

        return strDate;
        //return date == null ? null : dateToString(date);
    }

    @TypeConverter
    public static int boolToInteger(boolean bool) {
        return bool ? 1 : 0;
    }

}
