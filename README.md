# 725_assignment_1
This project has been tested on linux

Written by Dion Balmforth


**To run this project:**
- unzip the given folder
- You should now see the Three Java classes, two files and one "Shell" folder (inside the src folder)
- open terminal inside the src folder
- run ./run.sh to build and start the client and server
  + once "Setting up Client..." can be seen connection has been made
- enter the tests you wish to run in the terminal you called ./run.sh from
- the client should connect immediately with the server and a + response should be given back to the client

**Program Flow:**

User enters their information, USER must be first. This is the information for the 
only user currently stored in the userinfo.txt file
- User enters "USER user"
- User enters "PASS pass"
- User enters "ACCT acc"

After this the user then receives the ! response from the server, indicating that
they are now logged in. From here they can use any of the following functions

- TYPE { A | B | C }
- LIST { F | V }
- CDIR new-directory
- KILL file-spec
- NAME old-file-spec
- TOBE new-file-spec
- RETR file-spec
- SEND 
- DONE 

IMPORTANT NOTE:
- SEND does not sent the file correctly, therefore will fail tests.
- DONE shuts down the servers, however both threads that the servers run on will continue, 
please use ^C to terminate this threads.
- The commands given are case sensitive, please use uppercase when needed

Aside from these points all commands should work as expected, returning information 
to the client after each command and giving helpful responses.

\+  implies success.
\-  implies error.
\!  implies logged in.

**Expected tests:**
- not logging in and attempting to use other commands
- entering user, then changing order PASS and ACCT are called
- The dip.txt folder to test renaming, deleting etc.
- entering incorrect information for all commands
- not entering enough information for all commands
