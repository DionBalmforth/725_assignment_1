#!/bin/bash

clear

# Compile
javac SFTPServer.java
javac SFTPClient.java
javac ServerClient.java

#run server
java ServerClient
