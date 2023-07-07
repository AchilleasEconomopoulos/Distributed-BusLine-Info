# Distributed-BusLine-Info
This is a distributed Java application emulating a bus tracking system, a project assigned as a prerequisite to pass the Distributed Systems course in the Informatics Department of the Athens University of Economics and Business.

![The Pub-Broker-Sub model](https://learn.microsoft.com/en-us/azure/architecture/patterns/_images/publish-subscribe.png)

<br></br>

## The idea


### Using the Publisher - Broker - Subscriber model
The bus tracking system would ideally consist of three main factors that create a network.

- Sensors that detect when certain buses pass from the location that they're placed in. (Publishers)
- Computers that are connected to said sensors and constantly collect and store their information in data structures. (Brokers)
- User terminals that connect to the above computers in order to obtain and visualize a stream of information about the desired bus line(s). (Subscribers)

All the above connections represent many-to-many relationships, meaning that:

- One broker can be connected to multiple publishers and vice versa.
- One subscriber can be connected to multiple brokers and vice versa.

<br></br>

### Implementing the model with the push / pull functions
- Push: the publisher feeds the connected brokers with information in the form of (Bus line, [Latitude, Longitude]) pairs that are subsequently stored in the brokers' storage.
- Pull: the broker *pulls* a pair from their own storage and sends it to the connected subscribers interested in that specific Bus line.

<br></br>

-----

<br></br>

## The implementation
In this implementation of the system, the sensors are replaced with computers that simply read text files with predefined bus line information the moment they are "powered up".

### IMPORTANT NOTE: **This implementation assumes that the brokers are always up and running so it is important that the Broker main classes are all run before the Publisher main classes in order for the application to function correctly.**

<br></br>

Having said that, the whole Distributed Application can be split into two parts:

- ### The Publisher and Broker applications: two simple Java applications.
	- #### The publisher:
 		- Reads txt files to determine which Bus Lines are assigned to it.
   		- Starts reading a txt file that simulates live bus position updates and simultaneously sends them out to the registered brokers (reads 1 update per second).
       
   - #### The broker:
     - Is responsible for a specific range of Bus Lines.
     - Uses a queue for each Bus Line assigned to it that stores the publisher updates.
     - Is implemented with the use of Thread programming so that multiple connections to both brokers and subscribers are supported by one broker at the same time.
     - Listens for connections for both Publishers and Subscribers.
     - Pushes (adds) the updates received from the publishers into the corresponding queues.
     - Pulls (removes) the updates from their corresponding queues and sends them to the subscribers interested.


- ### The Subscriber Android application using Google Maps:
	- The app UI consists of a simple menu and a Google Maps fragment (using the Google Maps API)
	- When new bus line information is received, it is visualized with a pin on the Maps fragment.
	- Tested On:
		- Android Studio Version: Flamengo 2022.2.1
		- Android Gradle Plugin Version: 8.0.2
		- Gradle Version: 8.0.2
		- Java Sdk: 1.8
		- Android Virtual Device: Pixel 3 API 31
 
<br></br>

The different applications communicate with the use of socket programming and exchange bus line information objects with the help of JSON libraries (turning objects to strings and vice versa).

<br></br>

------
#### A more detailed project description along with the full list of features required to be implemented by the students of the course it is given [here](Project-Distributed-2019.pdf) in Greek.
