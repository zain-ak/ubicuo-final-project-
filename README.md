# Ubicuo
![alt text](ubicuo/Android Application/app/src/main/res/drawable-xxxhdpi/ic_launcher_app_no_border_new.png "Ubicuo Logo")

## Introduction
Ubicuo is a fully fledged facial recognition attendance system, complete with the a database of users and the ability to manage those users. It also makes use of beacons for physical location verification (PLV). The core system was coded in C++ using the OpenCV library for face detection & face recognition. At the user end, an Android application was created so that users can view their attendance details. The database was created using MySQL and an effort was made to make it relational.

This project was our final year project for our computer science degree: basic testing was carried out among the group members, the context for it was classes at our university and thus the system was designed around that structure of classes. However, some effort was made to make the system generic enough that it could be used in multiple contexts.

## Core System
The core system consists of the face detection and face recognition algorithms, co-ordinating sessions (classes) and manipulating the database as users move past the cameras. The core is broken down as follows:

  1. Remote Server - Manages the overall sessions, it'll notify the local server (below) when a session is about to begin and sends it appropriate details about it. For the duration of a session, a forked copy of the remote server will be open to co-ordinate with its local server counterpart to manage the transfer of images from local to remote.
  
  The remote server receives images from the local server and reconstructs them on its end, and passes them onto the facial recognition algorithm. Local Binary Pattern Histograms were used in the project for **facial recognition**, primarily because of how easy it is to update files for a specific class in case any changes occur.
  
  The remote server is connected to the database and manipulates it as a user's attendance is verified by the face recognition algorithm. For the project, the connection to the database was local, and the MySQL C/C++ Connector library was used so that queries could be constructed and executed on the C++ program.
  
  2. Local Server - The local server receives a signal from the remote server along with some parameters. The local server then also forks into a new process while the original keeps listening. The forked process starts the **face detection** portion of the code and records for a set amount of time. During the project, this time was kept to a minimum but in a real-world implementation it would run for 30 minutes each during entry and exit.
  
  The local server is also set up for fault tolerance in case the remote server crashes. It does this by saving any detected faces into a folder on the machine that can later on be sent to the remote server when it's running again. The backup folder is created especially for the session to remove any confusion about which files belong to which session. The files themselves are named according to the session and their iteration number.
  
  *The local server and remote server are connected through basic C/C++ socket programming.
  
## Android Application
The Android application was a great learning experience in this project. It allowed the learning of front-end design following Google's Material Design philosophy, and backend programming to connect the application to a remote database to fetch relevant data. Discussed below will be the different views of the application and how they were implemented.

The app differentiates between teacher and student through the login credentials entered, and based on this different scripts are called on the backend. The homepage of the application is similar for both teachers & students, displaying what subjects they are teaching or enrolled in, respectively.


  
