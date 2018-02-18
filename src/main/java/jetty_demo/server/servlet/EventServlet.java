package jetty_demo.server.servlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import jetty_demo.server.socket.EventSocket;

public class EventServlet extends WebSocketServlet
{

	private static final long serialVersionUID = -1503943420219706585L;

	@Override
    public void configure(WebSocketServletFactory factory)
    {
        factory.register(EventSocket.class);
    }
}