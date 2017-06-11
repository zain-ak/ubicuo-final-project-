package com.example.airport;

/**
 * Created by Zain on 5/15/17.
 */

public class Student {

    private String first_name;
    private String last_name;
    private String id;

    Student(String fn, String ln, String id) {
        first_name = fn;
        last_name = ln;
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getId() {
        return id;
    }
}
