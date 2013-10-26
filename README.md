NewsLetter-Emailer
==================

A distributed MultiThreaded java Application that Sends SMTP Emails read from a database.


Working:
The Number Emails handled by a single machine Depends on the number of Active Machines.
Each Machine Distributes its set of emails between its threads.

Every time a new machine enters, redistribution of work load takes place.
After every machine has finished working, master machine resets the database.
The user is supposed to enter the desired number of threads as it is dependent on the working machine and requirements.
Login Details are to be entered in the config.properties file inside the Email folder.
