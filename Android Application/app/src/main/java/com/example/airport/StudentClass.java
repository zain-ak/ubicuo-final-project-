package com.example.airport;

/**
 * Created by Zain on 5/19/17.
 */

public class StudentClass {

    private String classID, subjectCode, classType, dow, time, locationID;
    int hr, min;

    StudentClass(String cID, String subjectCode, String classType, String dow, String time, String lID) {
        classID = cID;
        this.subjectCode = subjectCode;
        this.classType = classType;
        this.dow = dow;
        this.time = time;
        locationID = lID;

        String split[] = time.split(":");
        this.hr = Integer.parseInt(split[0]);
        this.min = Integer.parseInt(split[1]);

    }

    String getDOW() { return dow; }
    String getSubjectCode() { return this.subjectCode; }
    String getClassType() { return this.classType; }
    int getHR() { return hr; }
    int getMin() { return min; }
    String getClassID() { return classID; }
    String getLocationID() { return locationID; }

}
