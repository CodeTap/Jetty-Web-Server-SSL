package jetty_demo;

import jetty_demo.server.JettyServer;

public class JettyWebServerDemoApp 
{
    public static void main( String[] args )
    {
        JettyServer webserver = new JettyServer(8080,8443);
        webserver.startServer();
    }	
}
