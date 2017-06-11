# Ubicuo

## Introduction
Ubicuo is a fully fledged facial recognition attendance system, complete with the a database of users and the ability to manage those users. It also makes use of beacons for physical location verification (PLV). The core system was coded in C++ using the OpenCV library for face detection & face recognition. At the user end, an Android application was created so that users can view their attendance details. The database was created using MySQL and an effort was made to make it relational.

This project was our final year project for our computer science degree: basic testing was carried out among the group members, the context for it was classes at our university and thus the system was designed around that structure of classes. However, some effort was made to make the system generic enough that it could be used in multiple contexts.

## Core System
The core system consists of the face detection and face recognition algorithms, co-ordinating sessions (classes) and manipulating the database as users move past the cameras. The core is broken down as follows:

  1. Remote Server - Mana
  
