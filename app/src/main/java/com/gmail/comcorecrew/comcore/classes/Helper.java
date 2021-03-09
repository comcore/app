package com.gmail.comcorecrew.comcore.classes;

import com.gmail.comcorecrew.comcore.enums.Gender;

/*
 * Class that contains helper functions.
 */
public class Helper {

    private static final int[] daysInMonth = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public static final int maxData = 0x001E8483; //4MB + 6 Bytes of chars
    public static final int maxNameLen = 32;
    public static final int maxBioLen = 128;
    public static final int profileLen = 166; //Bio len + Name len + Id + other info

    //Stores a month, day, and year (up to 32767) in the first 3 bytes of an int.
    public static int toDateFormat(int month, int day, int year) {
        //Robust date validity checker in an if statement.
        if ((month < 1) || (month > 12) || (day < 1) || (day > daysInMonth[month - 1]) || (year < 0)
                || (year > 32767) || ((month == 2) && (day == 29) && ((year % 4) != 0))) {
            throw new IllegalArgumentException();
        }
        int format = month;
        format = (format << 5) | day;
        return (format << 15) | year;
    }

    public static int toBioFormat(Gender gender, int month, int day, int year) {
        int format = toDateFormat(month, day, year);
        switch (gender) {
            case MALE: {
                return (1 << 24) | format;
            }
            case FEMALE: {
                return  (2 << 24) | format;
            }
            case NONBINARY: {
                return (3 << 24) | format;
            }
            default: {
                return format;
            }
        }
    }

    //Returns month from format.
    public static int monthFromFormat(int format) {
        return format >> 20;
    }

    //Returns day from format.
    public static int dayFromFormat(int format) {
        return (format >> 15) & 0b11111;
    }

    //Returns year from format.
    public static int yearFromFormat(int format) {
        return format & 0b111111111111111;
    }
}
