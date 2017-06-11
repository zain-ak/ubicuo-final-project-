package com.example.airport;

/**
 * Created by Zain on 4/23/17.
 */

public class Attendance {

    int entry, exit, beacon, overall;
    String description, date;

    Attendance() {}

    Attendance(int entry, int exit, int beacon, int overall, String description, String date) {
        this.entry = entry;
        this.exit = exit;
        this.beacon = beacon;
        this.overall = overall;
        this.description = description;
        this.date = date;
    }

    public int isEntry() {
        return entry;
    }

    public int isExit() {
        return exit;
    }

    public int isBeacon() {
        return beacon;
    }

    public int isOverall() {
        return overall;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }
}
