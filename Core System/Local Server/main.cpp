#include <iostream>
#include <chrono>
#include <ctype.h>

#include <thread>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#include "opencv2/video/tracking.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/videoio.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/objdetect.hpp"

using namespace cv;
using namespace std;

Point2f point;
bool addRemotePt = false;
String face_cascade_name = "/Users/Zain/Desktop/KLTtest/KLTtest/face.xml";
CascadeClassifier face_cascade;
TermCriteria termcrit(TermCriteria::COUNT | TermCriteria::EPS, 20, 0.03);
Size subPixWinSize(10, 10), winSize(31, 31);
const int MAX_COUNT = 500;
bool needtoInit = true;
string sessionID = "CSCIXXX-X-XX";
long int frameCount, timeElapsed, name, sessionPortNo = 0;

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

void calcFrameRate() {
    VideoCapture cap;
    Mat frame;
    long int frameCount;
    cap.open(0);
    if (!cap.isOpened())
    {
        cout << "Error loading camera";
        return;
    }
    
    cout << "Calculating frame rate, wait 10 seconds\n";
    Timer tmr;
    while ((int)tmr.elapsed() != 10) {
        cap >> frame;
        frameCount++;
    }
    
    cout << frameCount << endl;
    cout << "FPS: " << frameCount/10 << endl;
} //A helper function to calculate the frame rate (sec) whatever input device is being used to get frames

Mat equalizeFace (Mat face) {
    int w = face.cols;
    int h = face.rows;
    Mat wholeFace;
    equalizeHist(face, wholeFace);
    int midX = w/2;
    
    Mat leftSide = face(Rect(0,0, midX,h));
    Mat rightSide = face(Rect(midX,0, w-midX,h));
    equalizeHist(leftSide, leftSide);
    equalizeHist(rightSide, rightSide);
    
    for (int y=0; y<h; y++) {
        for (int x=0; x<w; x++) {
            int v;
            if (x < w/4) {
                // Left 25%: just use the left face.
                v = leftSide.at<uchar>(y,x);
            }
            else if (x < w*2/4) {
                // Mid-left 25%: blend the left face & whole face.
                int lv = leftSide.at<uchar>(y,x);
                int wv = wholeFace.at<uchar>(y,x);
                // Blend more of the whole face as it moves
                // further right along the face.
                float f = (x - w*1/4) / (float)(w/4);
                v = cvRound((1.0f - f) * lv + (f) * wv);
            }
            else if (x < w*3/4) {
                // Mid-right 25%: blend right face & whole face.
                int rv = rightSide.at<uchar>(y,x-midX);
                int wv = wholeFace.at<uchar>(y,x);
                // Blend more of the right-side face as it moves
                // further right along the face.
                float f = (x - w*2/4) / (float)(w/4);
                v = cvRound((1.0f - f) * wv + (f) * rv);
            }
            else {
                // Right 25%: just use the right face.
                v = rightSide.at<uchar>(y,x-midX);
            }
            face.at<uchar>(y,x) = v;
        }// end x loop
    }//end y loop
    
    Mat filtered = Mat(face.size(), CV_8U);
    bilateralFilter(face, filtered, 0, 20.0, 2.0); //Filtering

    return face;
}

void error(const char *msg)
{
    perror(msg);
    //exit(1);
}

void sendToRemote(Mat frame) {
    
    Size2i frameDim = Size2i(frame.cols, frame.rows);
    Mat temp = frame(Rect(Point(0,0), frameDim)).clone().reshape(0,1);
    
    cout << "sendToRemote() port:" << sessionPortNo << endl;
    
    cout << "ROWS: " << frame.rows << endl;
    cout << "COLS: " << frame.cols << endl;
    unsigned long int imgSize = frame.total() * frame.elemSize();
    cout << "Size of image: " << imgSize << " bytes" << endl;
    int sockfd, n;
    struct sockaddr_in serv_addr;
    struct hostent *server;
    
    uchar *buffer = new uchar[imgSize];
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0)
        error("ERROR opening socket");
    server = gethostbyname("127.0.0.1");
    //server = gethostbyname("172.28.16.104");
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr,
          (char *)&serv_addr.sin_addr.s_addr,
          server->h_length);
    serv_addr.sin_port = htons(sessionPortNo);
    if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) {
        error("ERROR connecting, saving image locally");
        string filename = "/Users/Zain/Desktop/KLTNetbeans/BackedUpImages/" + sessionID + "-" + ::to_string(name) + ".jpeg"; //sessionID+imageIteration;
        cout << filename << endl;
        imwrite(filename, frame);
    }
    else {
        bzero(buffer,imgSize);

        /*Send dimensions of the frame first*/
        if (send(sockfd, (char*)&frame.cols, sizeof(frame.cols), 0) == -1)
            cerr << "# of columns not sent successfully\n";
        if (send(sockfd, (char*)&frame.rows, sizeof(frame.rows), 0) == -1)
            cerr << "# of rows not sent successfully\n";

        /*Send the image*/
        buffer = temp.data;
        cout << "Number of bytes written: " << send(sockfd, buffer, imgSize, 0) << endl << endl;
    }

    close(sockfd);
}

vector<Rect> faceDetection(Mat grayFrame, vector<Rect> &faces) {
    
    Mat *temp = new Mat();
    equalizeHist(grayFrame, *temp);
    Timer tmr;
    face_cascade.detectMultiScale(*temp, faces, 1.03, 3, 0 | CASCADE_SCALE_IMAGE, Size(190,190), Size(210,210)); //detecting faces w/ min size of 100x100
    //cout << "Face Detection took " << tmr.elapsed() << " seconds\n";
    delete temp;
    
    return faces;
    
} //Face Detection using Viola Jones

Mat* drawRectangles(Mat &originalFrame, Mat grayFrame, vector<Rect> &faces) {
    
    Point *center = new Point[faces.size()];
    Rect tempFace;
    Mat *mask = new Mat();
    *mask = cv::Mat::zeros(grayFrame.size(), grayFrame.type());
    
    Mat *faceROI = new Mat(); //test variable
    
    if (faces.size() == 0)
        return NULL;
    
    for (size_t i = 0; i < faces.size(); i++)
    {
        tempFace = faces[i];
        tempFace.width *= 0.8;
        tempFace.x += (0.05*tempFace.x);
        center[i] = Point( tempFace.x + tempFace.width*0.4, tempFace.y + tempFace.height*0.4 );
        //cout << center[i] << endl;
        Mat roi(*mask, tempFace);
        roi = Scalar(255,255,255); //White box over each face in mask window
        *faceROI = Mat(grayFrame, tempFace);
        
        //rectangle(*mask, tempFace, Scalar(255,0,0), 1, 8, 0);
        rectangle(originalFrame, tempFace, Scalar(0, 255, 0), 1);
        //ellipse( originalFrame, center[i], Size( tempFace.width*0.4, tempFace.height*0.4), 0, 0, 360, Scalar( 255, 120, 128 ), 4, 8, 0 );
        
        //imshow("Mask", *mask);
        //
        imshow("Face ROI", *faceROI);
        *faceROI = equalizeFace(*faceROI);
        imshow("Equalized Face ROI", *faceROI);
        
      
        thread tempThread(sendToRemote, *faceROI);
        tempThread.detach();
        name++;

    }
    
    delete faceROI;
    return mask;
    
} //draw rectangles around detected faces & gets masks for each face which will then be used by KLT

void runKLT(Mat &originalFrame, Mat grayFrame, Mat &prevGray, Mat masks[], size_t numFaces, vector< vector<Point2f> > &pointsFirst, vector< vector<Point2f> > &pointsSecond) {
    
    for (size_t i = 0; i < numFaces; i++) {
        
        if (needtoInit)
        {
            goodFeaturesToTrack(grayFrame, pointsFirst[i], MAX_COUNT, 0.001, 5, masks[i], 3, 0, 0.04);
            cornerSubPix(grayFrame, pointsFirst[i], subPixWinSize, Size(-1, -1), termcrit);
            for (size_t j = 0; j < pointsFirst[i].size(); j++)
                circle(originalFrame, pointsFirst[i][j], 2, Scalar(255, 255, 0), 1, 8);
            addRemotePt = false;
        }
        
        if (!pointsSecond[i].empty()) //means the swap has happened
        {
            vector<uchar> status; //error checking var
            vector<float> err; //error checking var
            
            if (prevGray.empty())
                grayFrame.copyTo(prevGray);
            
            calcOpticalFlowPyrLK(prevGray, grayFrame, pointsSecond[i], pointsFirst[i], status, err, winSize, 6, termcrit, 0, 0.001);
            size_t j, k;
            
            for (j = k = 0; j < pointsFirst[i].size(); j++)
            {
                if (addRemotePt)
                {
                    if (norm(point - pointsFirst[i][j]) <= 5)
                    {
                        addRemotePt = false;
                        continue;
                    }
                }
                
                if (!status[j])
                    continue;
                
                pointsFirst[i][k++] = pointsFirst[i][j];
                circle(originalFrame, pointsFirst[i][j], 2, Scalar(0, 255, 255), 1, 8);
            }
            
            pointsFirst[i].resize(k);
        }
        
        if (addRemotePt && pointsFirst[i].size() < (size_t)MAX_COUNT)
        {
            vector<Point2f> tmp;
            tmp.push_back(point);
            cornerSubPix(grayFrame, tmp, winSize, Size(-1, -1), termcrit);
            pointsFirst[i].push_back(tmp[0]);
            addRemotePt = false;
        }
        
        needtoInit = false;
        std::swap(pointsFirst[i], pointsSecond[i]);
    }
    grayFrame.copyTo(prevGray);
}

int executeFaceDetection() {
    if (!face_cascade.load(face_cascade_name))
    {
        cout << "Error loading face cascade";
        return -1;
    }
    
    VideoCapture cap;
    cap.open(1);
    if (!cap.isOpened())
    {
        cout << "Error loading camera";
        return -1;
    }
    
    Mat gray, prevGray, image;
    vector<Rect> faces;
    Mat *masks;
    vector< vector<Point2f> > pointsFirst, pointsSecond;
    
    Timer tmr;
    while (timeElapsed < 10000) //Run for 30 minutes brah
    {
        cap >> image;
        
        frameCount++;
        
        if (image.empty())
        {
            cerr << "Error -1 - No captured frame\n";
            break;
        }
        
        timeElapsed = tmr.elapsed();
        cvtColor(image, gray, COLOR_BGR2GRAY);
        
        faceDetection(gray, faces);
        masks = drawRectangles(image, gray, faces);
        
        pointsFirst.resize(faces.size());
        pointsSecond.resize(faces.size());
        
        //runKLT(image, gray, prevGray, masks, faces.size(), pointsFirst, pointsSecond);
        
        char c = (char)waitKey(10);
        if (c == 27) //ESC key to loop out...
            break;
        
        if (c == 'r') {
            needtoInit = true;
            pointsFirst.clear();
            pointsSecond.clear();
        }
        
        if (c == 'c') {
            pointsFirst.clear();
            pointsSecond.clear();
        }
        
        if (faces.size() == 0)
            needtoInit = true;
        
        
        imshow("Original Feed", image);
        
    }
    cout << "Program ran for " << tmr.elapsed() << " seconds\n" << frameCount << " frames processed\nFPS: " << frameCount/tmr.elapsed() << endl << endl;
    return 0;

}


int main() {
    int sockfd, newsockfd, portno, pid;
    socklen_t clilen;
    struct sockaddr_in serv_addr, cli_addr;
    
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0)
        error("ERROR opening socket");
    bzero((char *) &serv_addr, sizeof(serv_addr));
    portno = 20011;
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);
    int bind  = ::bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
    if (bind == -1) {
        cerr << "ERROR binding to port 20011, trying on backup (20012)\n";
        portno = 20012;
        serv_addr.sin_port = htons(portno);
        bind = ::bind(sockfd, (struct sockaddr *) & serv_addr, sizeof(serv_addr));
    }
    
    if (bind == -1) {
        cerr << "ERROR binding to backup port 20012, exiting program\n";
        exit(1);
    }
    
    cout << "Local Server Listening on port " << portno << endl;
    listen(sockfd,5);
    clilen = sizeof(cli_addr);
    while (1)
    {
        newsockfd = accept(sockfd,
                           (struct sockaddr *) &cli_addr, &clilen);
        
        int portNo = 0;
        vector<char> buffer(sessionID.length());
        
        if (recv(newsockfd, &portNo, sizeof(portNo), 0) == -1)
            cerr << "Error receiving session port number\n";
        
        if(recv(newsockfd, buffer.data(), buffer.size(), 0) == -1)
            cerr << "Error receiving session ID\n";
        
        sessionID.clear();
        sessionID.append(buffer.cbegin(), buffer.cend());
        sessionPortNo = ntohl(portNo);
        
        //if (sessionPortNo > 0)
            cout << "Session " << sessionID << " starting on Port " << sessionPortNo << endl;
        if (newsockfd < 0)
            error("ERROR on accept");
        
        pid = fork();
        if (pid < 0)
            error("ERROR on fork");
        if (pid == 0)
        {
            close(sockfd);
            cout << "About to execute fork()\n";
            executeFaceDetection();
            exit(0);
        }
        else close(newsockfd);
    } /* end of while */
    close(sockfd);
    

    
 
    
    //executeFaceDetection();
    
    
       //cv::VideoCapture cap;
//    cv::VideoCapture capTwo;
//    //cv::VideoCapture capThree;
//        //cap.open(1);
//        capTwo.open(2);
//        //capThree.open(2);
//        if (!capTwo.isOpened())
//        {
//            std::cout << "Error loading camera";
//            return -1;
//        }
//    
//    cv::Mat frameOne, frameTwo, frameThree;
//    while(true) {
//        //cap >> frameOne;
//        capTwo >> frameTwo;
//        //capThree >> frameThree;
//        
//        //imshow("Feed", frameOne);
//        imshow("FeedTwo", frameTwo);
//        //imshow("FeedThree", frameThree);
//        
//        char c = (char) cv::waitKey(10);
//                if (c == 27) //ESC key to loop out...
//                    break;
//    }
    
    return 0;

}
