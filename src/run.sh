#!/bin/bash

clear

#add librarys

# Compile
javac SFTPServer.java
javac SFTPClient.java
javac ServerClient.java

#run server
java ServerClient

#run tests

#output to log file