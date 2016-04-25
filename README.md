**MicroDG -  Process-Controller-Service **

Author: *David Brady*
Date:   *25/04/2016*

This is a Microservice which acts as a ESB, tying multiple micro service outputs together

To run this service locally:
* From within the BETA-Process-Controller-Service Directory
'''
sbt clean compile
sbt test
sbt run
'''
* This will compile the service, test it and run it. 
* Then open a browser, navigate to localhost:8083 proceeded by the desired endpoint
* A list of endpoints can be found at: localhost:8083/hello

Alternatively, you can open this source code in the Typesafe Activator UI:
* From within the BETA-Process-Controller-Service Directory
'''
activator ui
'''

**PLEASE NOTE**
This project contains work that does not belong to me:
* src/main/scala/support/CORSSupport

Title: joseraya/CorsSupport.scala
Author: Jose Raya
Date: 19/04/2016
Code version: 1.0 
Type: Source Code
Availability: https://gist.github.com/joseraya/176821d856b43b1cfe19






