/*
 * The primary remote server for the FRAS project, primary communication takes place on Port 20011,
 * Ports reserved for session use are 20020 - 20100
 * A duplicate of this project called secondary remote server acts as a backup in case this project fails to run, communication takes place on Port 20012
*/

/*system("./secondaryremoteserver"); */

#include "Session.h"

#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 
#include <thread>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>
#include <iomanip>
#include <random>
#include <vector>
#include <algorithm>
#include <chrono>

/*
Include directly the different
headers from cppconn/ and mysql_driver.h + mysql_util.h
(and mysql_connection.h). This will reduce your build time!
*/
#include "mysql_connection.h"
#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/resultset.h>
#include <cppconn/statement.h>
#include <cppconn/prepared_statement.h>

/*
OpenCV includes
*/
#include "opencv2/core.hpp"
#include "opencv2/face.hpp"
#include "opencv2/highgui.hpp"

using namespace cv;
using namespace cv::face;
using namespace std;

/*General Program Parameters*/
vector<Session> sessionList;
vector<int> usedPorts;
time_t rawtime;
struct tm * currentDay;
const string DAYS[5] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};
string today;

class Timer {
public:
    Timer() : beg_(clock_::now()) {}
    void reset() { beg_ = clock_::now(); }
    double elapsed() const {
        return std::chrono::duration_cast<second_>
        (clock_::now() - beg_).count(); }

private:
    typedef std::chrono::high_resolution_clock clock_;
    typedef std::chrono::duration<double, std::ratio<1> > second_;
    std::chrono::time_point<clock_> beg_;
}; //Class used for measuring time

/*SQL Connection Parameters*/
sql::Driver *driver;
sql::Connection *con;
sql::ResultSet *res;
sql::PreparedStatement *pstmt;

int portNoGen();
bool connectToDB();
void getClassList();
void startNewSession(int, Session);
bool signalLocal(int, string);
void rcvImage(int, string); /* function prototype */
void facerecognition(Mat, string);
void updateAttendanceEntry(int, string);
void updateAttendanceExit (int, string);
string insertSessionDB(string, int);
bool insertAttendanceDB(int, string);

void error(const char *msg)
{
    perror(msg);
    //exit(1);
}

int main(int argc, char *argv[])
{
    int tempSession = 0;
    int pid;
    if ( connectToDB() )
        cout << "Successfully connected to Database\n";
    else {
        cerr <<"ERROR connecting to database\n";
        exit(1);
    }
    
    getClassList();
    Timer tmr;
    int timeElapsed = 0;
    
    bool temp = true;
    tempSession = portNoGen();
    
    //if (signalLocal(tempSession)) {
    while (true) {
        pid = fork();
        while (true) {
            if (pid < 0)
                cerr << "ERROR while forking\n";
            if (pid == 0 && temp == true) {
                startNewSession(tempSession, sessionList.at(6));
                //exit(0);//start a new session with 
            }
            if (pid > 0)
                temp = false;
        }
    }
    //}
    
    /*Go through sessionList, whenever it's time for a class, generate a random port
     *number for the new session
    */
    
    //if (/*Time for new class*/) {
//    while (true) {
//        timeElapsed = tmr.elapsed();
//        if (timeElapsed >= 108000) {
//            if (!con->isValid()) {
//                if (connectToDB() ) {
//                    cout << "Successful reconnection to DB\n";
//                }
//                else {
//                    cerr << "Database is down, ending program\n";
//                    exit(1);
//                }
//            }
//            time(&rawtime);
//            currentDay = localtime(&rawtime);
//            for (int i = 0; sessionList.size(); i++) {
//                if ( difftime(mktime(currentDay), mktime(sessionList.at(i).getTime())) <= 180 ) {
//                    tempSession = portNoGen();
//                    if (signalLocal(tempSession)) {
//                        pid = fork();
//                        if (pid < 0)
//                            cerr << "ERROR while forking\n";
//                        if (pid == 0 && temp == true) {
//                            startNewSession(tempSession, sessionList.at(i));
//                        }
////                        if (pid > 0)
////                            temp = false;
//                    }
//                }
//            }
//        }
//    }
   
    
    
     return 0; /* we never get here */
}

/*Generate a unique port number for a new session in the range 20020 - 20100*/
int portNoGen() {
    random_device rd; //seed generator
    mt19937_64 generator{rd()}; //generator initialized with seed from rd
    uniform_int_distribution<> dist{20020, 20100};
    
    int x = dist(generator);
    bool valid = false;
    while (!valid) {
        if(find(usedPorts.begin(), usedPorts.end(), x) != usedPorts.end())
            x = dist(generator);
        else
            valid = true;
    }
    
    usedPorts.push_back(x);
    return x;
}

bool connectToDB() {
/*Open a connection to the database Final Project*/
    driver = get_driver_instance();
    try {
        con = driver->connect("tcp://127.0.0.1", "root", "root");
    }
    catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl << endl;
        cout << "Attempting to connect to backup database\n";
        try {
            con = driver->connect("localhost", "root", "root");
        }
        catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl << endl;
        cout << "Connection to backup database failed, exiting program\n";
        exit(1);
        }
        con->setSchema("final_project_backup");
        return con->isValid();
    }
    /* Connect to the MySQL test database */
    con->setSchema("final_project");
    
    return con->isValid();
}

void startNewSession(int portNo, Session subject) {
        
    cout << "Beginning new session on port " << portNo << endl;
    //connectToDB();
    string sessionID = insertSessionDB(subject.getSubjectID(), subject.getClassID()); //thread
    cout << sessionID << endl;
    insertAttendanceDB(subject.getClassID(), sessionID);
    cout << "Database updated\n";
    if (signalLocal(portNo, sessionID)) {
        int sockfd, newsockfd, pid;
        socklen_t clilen;
        struct sockaddr_in serv_addr, cli_addr;

        sockfd = socket(AF_INET, SOCK_STREAM, 0);
        if (sockfd < 0) 
           error("ERROR opening socket");
        bzero((char *) &serv_addr, sizeof(serv_addr));
        serv_addr.sin_family = AF_INET;
        serv_addr.sin_addr.s_addr = INADDR_ANY;
        serv_addr.sin_port = htons(portNo);
        int bind  = ::bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
        listen(sockfd,5);
        clilen = sizeof(cli_addr);
        while (1) 
        {
            cout << "Listening\n";
            newsockfd = accept(sockfd, 
                  (struct sockaddr *) &cli_addr, &clilen);
            if (newsockfd < 0) 
                error("ERROR on accept");

            //thread tempThread(dostuff, newsockfd);
    //         tempThread.join();
    //         tempThread.detach();
            pid = fork();
            if (pid < 0)
                error("ERROR on fork");
            if (pid == 0)  
            {
                close(sockfd);
                rcvImage(newsockfd, sessionID);
                exit(0);
            }
            else close(newsockfd);
        } /* end of while */
        close(sockfd);
    }
    
     
}

bool signalLocal(int portNo, string sessionID) {
    int sockfd, portno, n;
    struct sockaddr_in serv_addr;
    struct hostent *server;
    portno = 20011;
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        error("ERROR opening socket");
        return false;
    }
    //server = gethostbyname("172.28.31.255");
    server = gethostbyname("127.0.0.1");
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        return false;
    }
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr,
          (char *)&serv_addr.sin_addr.s_addr,
          server->h_length);
    serv_addr.sin_port = htons(portno);
    if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) {
        error("ERROR connecting, trying backup port 20012");
        portno = 20012;
        serv_addr.sin_port = htons(portno);
        if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) {
            error("ERROR connecting to backup port, program exiting");
            exit(1);
        }
        return false;
    }
    
    portNo = htonl(portNo);
    
    // Write the number to the opened socket
    send(sockfd, &portNo, sizeof(portNo), 0);
    
    //Write the sessionID
    send(sockfd, sessionID.c_str(), sessionID.length(), 0);
    
    close(sockfd);
    cout << "Local server signaled\n";
    return true;
}

void rcvImage (int sock, string sessionID)
{
    cout << "Receiving image dimensions\n";
    Size2i imgDim = Size2i(0,0);
    int cols, rows = -1;
    
    if (recv(sock, (char*)&cols, sizeof(imgDim.height), 0) == -1) 
        cerr << "Error receiving dimensions\n";
    if (recv(sock, (char*)&rows, sizeof(imgDim.height), 0) == -1)
        cerr << "Error receiving dimensions\n";
    
    imgDim = Size2i(cols, rows);
    cout << "Image Dimensions: " << cols << " x " << rows << endl;
    
    cout << "Receiving image\n";
    Mat  img = Mat::zeros(imgDim, CV_8UC1);
    int  imgSize = img.total() * img.elemSize();
    uchar buffer[imgSize];
    int n = 0;
    
   //bzero(buffer, imgSize);
   
   for (int i = 0; i < imgSize; i += n) 
   {
       n = recv(sock, buffer + i, imgSize - i, 0);
       if (n == -1)
           cerr << "Error receiving image\n";
       cout << "Bytes Received: " << n << endl;
   }
   
   //n = recv(sock, buffer, imgSize, 0);
   //cout << "Bytes read: " << n << endl;
   
   int ptr = 0;
   for (int i = 0;  i < imgDim.height; i++) 
   {
        for (int j = 0; j < imgDim.width; j++) 
        {                                     
             img.at<uchar>(i,j) = (int) uchar(buffer[ptr]);//cv::Vec3b(buffer[ptr+ 0],buffer[ptr+1],buffer[ptr+2]);
             ptr++;
        }
    } 
 //Mat img(Size(HEIGHT, WIDTH), CV_8UC3, buffer);  
 //Mat image(690,690,CV_8UC3,*buffer);
    if(img.empty())
       cout << "Theres nothing insde me";
   else
       cout << "SOMEINTH" << endl;
        
   imshow( "Server", img );  
   //imwrite("/Users/Zain/Desktop/pop.jpg",img);
    namedWindow( "Server", CV_WINDOW_AUTOSIZE );// Create a window for display.
    facerecognition(img, sessionID);
    waitKey(0);
}

void facerecognition(Mat face, string sessionID)
{
  Ptr<LBPHFaceRecognizer> model = createLBPHFaceRecognizer();
  model->setThreshold(80.00);
  model->load("/Users/Zain/Desktop/csci319_lab1.xml");
  int predictlabel = model-> predict(face);
  cout << "The predicted label is " << predictlabel << endl;
  
  if (predictlabel != -1)
  {
    cout << "The face is in the database." << endl << "Updating the database" << endl;
    updateAttendanceEntry(predictlabel, sessionID);
  }
  else
    cout << "The face is not in the database." << endl;
}

void getClassList() 
{
    cout << "Creating session list\n";
    Session *temp;
    string subjectID, classID;
    

    time(&rawtime);
    currentDay = localtime(&rawtime);
    today = DAYS[currentDay->tm_wday];
    char buffer[100];
    strftime (buffer,100,"Today: %A, %d %B %G", currentDay);
    
    cout << endl << buffer << endl;
    cout << "Today's Class List:\n";
    cout << setw(1) << "Class ID" << setw(15)  << "Subject ID" << setw(10) << "Day" << setw(12) << "Time" << endl;
    
    try {
        sql::PreparedStatement *prepStmt;
        sql::ResultSet *result;

        prepStmt = con->prepareStatement("SELECT id, subject_id, day_of_week, time FROM CLASS");
        result = prepStmt->executeQuery();
        while (result->next()) {
            temp = new Session(result->getInt("id"), result->getString("subject_id"), result->getString("day_of_week"), result->getString("time"));
            //if (temp->getDay() == today)
                sessionList.push_back(*temp);
        } 
            //subjectID = res->getString("id");
        delete result;
        delete prepStmt;
    }
    catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
    }
    
    for(int i = 0; i < sessionList.size(); i++) {
        cout << sessionList.at(i) << endl;
    }
    
    delete temp;
}

string insertSessionDB(string subjectID, int classID) {
    string weekNo = "1";
    string sessionID;
    
    cout << "Creating new session on Database\n";
    /*Get some params from the database to create a session ID
      Knowing which subject to get and class to get has to be somehow automated*/
    try {
        sql::PreparedStatement *prepStmt;
        sql::Statement *stmt = con->createStatement();
        sessionID = subjectID + "-" + to_string(classID) + "-" + weekNo;
        
        stmt->execute("SET autocommit=0");
        stmt->execute("LOCK TABLE Session WRITE");
        prepStmt = con->prepareStatement("INSERT INTO Session VALUES (?,?, now(), ?)");
        prepStmt->setString(1, sessionID);
        prepStmt->setInt(2, classID);
        prepStmt->setString(3, weekNo);
        prepStmt->execute();
        stmt->execute("COMMIT");
        stmt->execute("UNLOCK TABLES");
        
        
        delete prepStmt;
        delete stmt;
    }
    catch (sql::SQLException &e)
    {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
    }
    
    cout << sessionID << " added successfully" << endl;
    return sessionID;
    
}

bool insertAttendanceDB(int classID, string sessionID) {
    
    vector<int> studentList;
    cout << "Creating attendance list for " << sessionID << endl;
    
    try {
        sql::PreparedStatement *prepStmt;
        sql::Statement *stmt = con->createStatement();
        sql::ResultSet *result;
        
        stmt->execute("SET autocommit=0");
        stmt->execute("LOCK TABLES Enrolled READ, Attendance WRITE");
        //cout << "Locked\n";
        
        prepStmt = con->prepareStatement("SELECT student_id FROM Enrolled WHERE class_id = ?");
        prepStmt->setInt(1, classID);
        result = prepStmt->executeQuery();
        //cout << "Students gotten\n";
        
        while(result->next())
            studentList.push_back(result->getInt("student_id"));
        
        cout << "Size of student list: " << studentList.size() << endl;
        
        for (int i = 0; i < studentList.size(); i++) {
            prepStmt->clearParameters();
            prepStmt = con->prepareStatement("INSERT INTO Attendance VALUES (?, ?, ?, ?, ?, ?)");
            prepStmt->setInt(1, studentList.at(i));
            prepStmt->setString(2, sessionID);
            prepStmt->setInt(3, 0);
            prepStmt->setInt(4, 0);
            prepStmt->setInt(5, 0);
            prepStmt->setInt(6, 0);
            prepStmt->execute();
        }
        //cout << "Students added\n";
        
        stmt->execute("COMMIT");
        stmt->execute("UNLOCK TABLES");
        //cout << "Tables unlocked\n";
        
        
        delete prepStmt;
        delete stmt;
    }
    catch (sql::SQLException &e)
    {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return false;
    }
    return true;
}

void updateAttendanceEntry (int id, string sessionID) {
    
    cout << "Updating attendance entry for " << id << " for session " << sessionID << endl;
    try {
        sql::PreparedStatement *prepStmt;
        sql::Statement *stmt = con->createStatement();
        stmt->execute("SET autocommit=0");
        stmt->execute("LOCK TABLE Attendance WRITE");
        prepStmt = con->prepareStatement("UPDATE Attendance SET camera_entry = 1 WHERE student_id = ? AND session_id = ?");
        prepStmt->setInt(1, id);
        prepStmt->setString(2, sessionID);
        prepStmt->executeUpdate();
        stmt->execute("COMMIT");
        stmt->execute("UNLOCK TABLES");
        
        delete prepStmt;
        delete stmt;
    }
     catch (sql::SQLException &e)
    {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        cout << "Error updating entry for  " << id << "\n\n";
    }
    cout << "Entry for  " << id << " updated successfully!\n\n";
}

void updateAttendanceExit (int id, string sessionID) {
    
    cout << "Updating attendance exit for " << id << " for session " << sessionID << endl;
    try {
        sql::PreparedStatement *prepStmt;
        sql::Statement *stmt = con->createStatement();
        stmt->execute("SET autocommit=0");
        stmt->execute("LOCK TABLE Attendance WRITE");
        prepStmt = con->prepareStatement("UPDATE Attendance SET camera_exit = 1 WHERE student_id = ? AND session_id = ?");
        prepStmt->setInt(1, id);
        prepStmt->setString(2, sessionID);
        prepStmt->executeUpdate();
        stmt->execute("COMMIT");
        stmt->execute("UNLOCK TABLES");
        
        delete prepStmt;
        delete stmt;
    }
     catch (sql::SQLException &e)
    {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        cout << "Error updating exit for  " << id << "\n\n";
    }
    cout << "Exit for  " << id << " updated successfully!\n\n";
}