# Jetty-Web-Server-SSL
A jetty web server with both http and https enabled by default.  Comes with websockets and basic servlets to help people get started.

## Web Server

The server portion of this is based off the Jetty examples.  Really basic server.  Contains a servlet that has a response of a simple webpage with the session.  There is also a small web app that displays hello on the screen.  

To start the server just run JettyWebServerDemoApp.java.  It is set to use port 8080 for http and 8443 for https.