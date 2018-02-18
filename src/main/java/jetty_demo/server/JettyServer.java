package jetty_demo.server;

import java.security.KeyStore;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import jetty_demo.server.servlet.EventServlet;
import jetty_demo.server.servlet.HelloServlet;


public class JettyServer 
{
	/** The port. */
	private int port;
	
	/** The ssl port. */
	private int ssl_port;
	
	/** The keygen. */
	private JettyServerSecurity security;
	
	/**
	 * Instantiates a new web server.  Also sets the required fields of port for http and port for https.
	 *
	 * @param port the port
	 * @param ssl_port the ssl port
	 */
	public JettyServer(int port, int ssl_port)
	{
		this.port = port;
		this.ssl_port = ssl_port;
		this.security = new JettyServerSecurity();
	}
	
	/**
	 * Starts the Jetty Webserver.  First calls the web security class generate a security key and a security
	 * certificate.  This allows the web server to run HTTPS via TLS 1.2.  
	 */
	public void startServer()
	{
	
		System.out.println("Web Server security starting");
		KeyStore keystore = security.generateCertAndServerKeyStore();
		System.out.println("Web Server security finished");
		
		// Create a basic jetty server object without declaring the port. Since
	    // we are configuring connectors directly we'll be setting ports on
	    // those connectors.
	    Server server = new Server();
	    
		//Creating the web application context
	    System.out.println("Loading Web App");
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		webapp.setResourceBase("src/main/webapp");
		
		 
		 // HTTP Configuration
	    // HttpConfiguration is a collection of configuration information
	    // appropriate for http and https. The default scheme for http is
	    // <code>http</code> of course, as the default for secured http is
	    // <code>https</code> but we show setting the scheme to show it can be
	    // done. The port for secured communication is also set here.
	    HttpConfiguration http_config = new HttpConfiguration();
	    http_config.setSecureScheme("https");
	    http_config.setSecurePort(ssl_port);
	    http_config.setOutputBufferSize(32768);
	
	    // HTTP connector
	    // The first server connector we create is the one for http, passing in
	    // the http configuration we configured above so it can get things like
	    // the output buffer size, etc. We also set the port (8080) and
	    // configure an idle timeout.
	    ServerConnector http = new ServerConnector(server,
	            new HttpConnectionFactory(http_config));
	    http.setPort(port);
	    http.setIdleTimeout(30000);
	
	    // SSL Context Factory for HTTPS
	    // SSL requires a certificate so we configure a factory for ssl contents
	    // with information pointing to what keystore the ssl connection needs
	    // to know about. Much more configuration is available the ssl context,
	    // including things like choosing the particular certificate out of a
	    // keystore to be used.
	
	    SslContextFactory sslContextFactory = new SslContextFactory();
	    sslContextFactory.setKeyStore(keystore);
		sslContextFactory.setKeyStorePassword("654321");

	
	    // OPTIONAL: Un-comment the following to use Conscrypt for SSL instead of
	    // the native JSSE implementation.
	
	    //Security.addProvider(new OpenSSLProvider());
	    //sslContextFactory.setProvider("Conscrypt");
	
	    // HTTPS Configuration
	    // A new HttpConfiguration object is needed for the next connector and
	    // you can pass the old one as an argument to effectively clone the
	    // contents. On this HttpConfiguration object we add a
	    // SecureRequestCustomizer which is how a new connector is able to
	    // resolve the https connection before handing control over to the Jetty
	    // Server.
	    HttpConfiguration https_config = new HttpConfiguration(http_config);
	    SecureRequestCustomizer src = new SecureRequestCustomizer();
	    src.setStsMaxAge(2000);
	    src.setStsIncludeSubDomains(true);
	    https_config.addCustomizer(src);
	
	    // HTTPS connector
	    // We create a second ServerConnector, passing in the http configuration
	    // we just made along with the previously created ssl context factory.
	    // Next we set the port and a longer idle timeout.
	    ServerConnector https = new ServerConnector(server,
	        new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
	            new HttpConnectionFactory(https_config));
	    https.setPort(8443);
	    https.setIdleTimeout(500000);
	
	    // Here you see the server having multiple connectors registered with
	    // it, now requests can flow into the server from both http and https
	    // urls to their respective ports and be processed accordingly by jetty.
	    // A simple handler is also registered with the server so the example
	    // has something to pass requests off to.
	
	    // Set the connectors
	    System.out.println("Setting http and https connectors");
	    server.setConnectors(new Connector[] { http, https });
	
        // The ServletHandler is a dead simple way to create a context handler
        // that is backed by an instance of a Servlet.
        // This handler then needs to be registered with the Server object.
	    ServletHandler hanlder = webapp.getServletHandler();    
	    
	    loadServlets(hanlder);
	    server.setHandler(webapp);

	    //ServletHolder holderEvents = new ServletHolder("ws-events", EventServlet.class);
	    webapp.addServlet(EventServlet.class, "/events/*");
	    // Start the server
	    try 
	    {
			server.start();
			server.join();
			System.out.println("Web Server started");
		} 
	    catch (Exception ex) 
	    {
	    	System.out.println("Error starting webserver.");
	    	System.out.println(ex);
		}
    }

	private void loadServlets(ServletHandler handler)
	{
		handler.addServletWithMapping(HelloServlet.class, "/hello");
	}
}
