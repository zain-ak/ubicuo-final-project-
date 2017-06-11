/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Session.h
 * Author: Zain
 *
 * Created on April 25, 2017, 11:25 PM
 */

#ifndef SESSION_H
#define SESSION_H

#include <iostream>
#include <ctime>

class Session {
    private:
        std::string subjectID, serverID, locationID, sTime, day;
        int classID;
        struct tm *time;
        int week;
        
    public:
        Session(int cID, std::string sID, std::string day, std::string time) {
            this->classID = cID;
            this->subjectID = sID;
            sTime = time;
            this->day = day;
            
            time_t rawtime;
            std::time(&rawtime);
            this->time = std::localtime(&rawtime);
            ::strptime(time.c_str(), "%H:%M:%S", this->time);
            
        } //for now, just get the class & subject IDs.
        
        friend std::ostream& operator<< (std::ostream &out, const Session &x) {
            out << x.classID << "\t" << x.subjectID << "\t\t" << x.day<< "\t\t" << x.sTime;
            return out;
        }
        
        int getClassID () { return this->classID; }
        std::string getSubjectID () { return this->subjectID; }
        std::string getDay () { return this->day; }
        struct tm *getTime() { return this->time; }
    
};

#endif /* SESSION_H */

