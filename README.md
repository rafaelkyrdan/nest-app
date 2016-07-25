# nest-app
Demo project. Inspired by Nest - the connected home.

## What's it?
Example of server-to-server integration between Nest and WebSocketServer.
It is created in Scala. App listens on messages from Nest and sends it to all followers connected to WebSocketServer.
You can get more information by visiting the link [Overview Nest Developer Programm](https://developer.nest.com/)
and a few examples [a few examples](https://developer.nest.com/documentation/cloud/sample-code)

## What's inside?
1. Akka
2. Spray.io
3. Spray-WebSocket
4. Firebase

## How to run locally?

### Install

To get started you'll need a [Nest access token][nest-keys] and [Nest Home Simulator](https://chrome.google.com/webstore/detail/nest-home-simulator/jmcapoebgeaabepohkchkldlfhchkega).
Copy `src/main/resources/credentials-sample.txt` to `src/main/resources/credentials.txt`, fill in the placeholder values.

### Building

Building is done with [Sbt][sbt] .

``` sh
sbt compile
```

### Running

After you've compiled you can run the application by running the following:

``` sh
sbt run
```

Assuming your config is set up you should see messages logged to standard output, something like this `Bound to localhost/127.0.0.1:8080`.
Now open a link in the browser [http://localhost:8080/websocket.html](http://localhost:8080/websocket.html).
After handshaking between server and socket open your Home Simulator and cause some changes in the StructureUpdate or DeviceStateUpdate.
Enjoy.
