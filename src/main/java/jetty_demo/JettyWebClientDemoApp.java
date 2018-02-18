package jetty_demo;

import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import jetty_demo.server.socket.EventSocket;

public class JettyWebClientDemoApp 
{

	public static void main(String[] args) 
	{
		URI uri = URI.create("ws://localhost:8080/events/");
		Scanner scanner = null;
        WebSocketClient client = new WebSocketClient();
        try
        {
            try
            {
            	
                client.start();
                // The socket that receives events
                EventSocket socket = new EventSocket();
                // Attempt Connect
                Future<Session> fut = client.connect(socket,uri);
                // Wait for Connect
                Session session = fut.get();
                scanner = new Scanner( System.in );
                System.out.println("Enter a message: q to quit.");
                
                while(true)
                {
                	String input = scanner.nextLine();
                	if (input.contentEquals("q"))
                		break;
                	else
                		session.getRemote().sendString(input);
                }
                
                session.close();
            }
            finally
            {
                client.stop();
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
	}

}
