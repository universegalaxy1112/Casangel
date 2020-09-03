package com.itikarus.miska.library.utils;

import android.util.Log;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String FMT_FULL = "yyyy-MM-dd HH:mm:ss";
    public static String FMT_FULL_FILENAME_FORMAT = "yyyyMMddHHmmss";
    public static String FMT_STANDARD = "MMM d, HH:mm";
    public static String FMT_TIME = "HH:mm";
    public static String FMT_DATE = "dd/MM/yyyy";
    public static String FMT_REVTIME = "MM.dd.yyyy";
    public static String FMT_MONTH_YEAR = "MM-yyyy";
    public static String FMT_YEAR_MONTH_DAY = "yyyy-MM-dd";
    public static String FMT_HOUR_MINUTE_aa = "HH:mm aa";
    public static String FMT_FOR_ERROR_DETAIL1 = "yy.MM.dd HH.mm";
    public static String FMT_FOR_ERROR_DETAIL2 = "aa";

    public static String convertFormat(String nowFmt, String newFmt, String datetime) {
        SimpleDateFormat srcDf = new SimpleDateFormat(nowFmt);
        try {
            Date date = srcDf.parse(datetime);
            SimpleDateFormat destDf = new SimpleDateFormat(newFmt);
            String mydate = destDf.format(date);
//            mydate = mydate.replace("p.m.", "pm");
//            mydate = mydate.replace("a.m.", "am");
            return mydate;
        } catch (ParseException e) {
            e.printStackTrace();
            return datetime;
        }
    }

    public static String getDateTimeString(String FMT) {
        SimpleDateFormat sdf = new SimpleDateFormat(FMT);
        return sdf.format(new Date());
    }

    public static String getDateTimeName() {
        SimpleDateFormat sdf = new SimpleDateFormat(FMT_STANDARD);
        return sdf.format(new Date());
    }

    public static String getDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(FMT_FULL);
        return sdf.format(new Date());
    }

    public static String getCurrentTimeStampForFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat(FMT_FULL_FILENAME_FORMAT);
        return sdf.format(new Date());
    }

    public static String getDateString(String df) {
        SimpleDateFormat sdf = new SimpleDateFormat(df);
        return sdf.format(new Date());
    }

    public static String getDateSpanishString(Date date, String df) {
        Locale spanishLocale = new Locale("es", "ES");
        SimpleDateFormat sdf = new SimpleDateFormat(df, spanishLocale);
        return sdf.format(date);
    }

    public static String getTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat(FMT_TIME);
        return sdf.format(new Date());
    }

    public static String currentTimeMillis() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static Calendar stringToCalendar(String str) {
        return stringToCalendar(str, FMT_MONTH_YEAR);
    }

    public static String calendarToString(Calendar calendar, String strFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
        String str = sdf.format(calendar.getTime());
        return str;
    }

    public static Calendar stringToCalendar(String str, String strFormt) {
        Calendar cal = Calendar.getInstance();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(strFormt);
            cal.setTime(sdf.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    public static long getDifferentTime(long pastTime) {
        long currentTime = System.currentTimeMillis();
        return currentTime - pastTime;
    }

    public static String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }

    public static float milisecToPastTimeSec(long current, long standard) {
        return milisecToSec(current - standard);
    }

    public static float milisecToSec(long milisec) {
        return (float) milisec / (1000f);
    }

    //*********** Checks if the Date is not Passed ********//

    public static boolean checkIsDatePassed(String givenDate) {

        boolean isPassed = false;

        Date dateGiven, dateSystem;

        Calendar calender = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = dateFormat.format(calender.getTime());

        try {
            dateSystem = dateFormat.parse(currentDate);
            dateGiven = dateFormat.parse(givenDate.replaceAll("[a-zA-Z]", " "));

            if (dateSystem.getTime() >= dateGiven.getTime()) {
                isPassed = true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("exception", "ParseException= " + e);
        }


        return isPassed;
    }
}
