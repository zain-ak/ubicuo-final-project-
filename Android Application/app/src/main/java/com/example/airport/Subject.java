package com.example.airport;

/**
 * Created by Zain on 4/22/17.
 */

public class Subject {
    private String title;
    private String code;
    private String attendanceRate;
    private String class_type;
    private String class_no;
    private String class_id;
    private int color = -1;

    Subject() {}

    Subject (String title, String code, String attendanceRate, String class_type, String class_no, String class_id) {
        this.title = title;
        this.code = code;
        this.attendanceRate = attendanceRate;
        this.class_type = class_type;
        this.class_no = class_no;
        this.class_id = class_id;
    }

    void setColor(int color) {this.color = color;}
    int getColor() {return this.color;}
    String getTitle () {return this.title;}
    String getCode () {return this.code;}
    String getAttendanceRate () {return this.attendanceRate;}

    public String getClass_no() {
        return class_no;
    }

    public void setClass_no(String class_no) {
        this.class_no = class_no;
    }

    public String getClass_type() {
        return class_type;
    }

    public void setClass_type(String class_type) {
        this.class_type = class_type;
    }

    public String getClassID () {return this.class_id;}
}
