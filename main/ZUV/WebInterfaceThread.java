package ZUV;

import java.io.IOException;
import java.util.logging.LogManager;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;


public class WebInterfaceThread extends Thread {

	/***
	 * provides the websocket api and the http(s) admin GUI
	 * 
	 * @author jstaerk
	 *
	 */
		//private static Logger log = LogManager.getLogger(PersistUtil.class);
		private WebAppContext context;
		private Server server;

		

		public void run() {
/*
			try {
				// make jetty use our log4j2 by using Slf4jLog
				org.eclipse.jetty.util.log.Log
						.setLog(new org.eclipse.jetty.util.log.Slf4jLog());
			} catch (Exception e) {
				log.error(e);
			}
*/
			server = new Server();

			  // Since this example shows off SSL configuration, we need a keystore
	        // with the appropriate key. These lookup of jetty.home is purely a hack
	        // to get access to a keystore that we use in many unit tests and should
	        // probably be a direct path to your own keystore.
	      /*  File keystoreFile = new File(new FileInputStream(this.getClass().getResourceAsStream("my.conf")));
	        if (!keystoreFile.exists())
	        {
	            try {
					throw new FileNotFoundException(keystoreFile.getAbsolutePath());
				} catch (FileNotFoundException e) {
					log.error("Exception",e);
				
				}
	        }*/
	        
	        // HTTP Configuration
	        // HttpConfiguration is a collection of configuration information
	        // appropriate for http and https. The default scheme for http is
	        // <code>http</code> of course, as the default for secured http is
	        // <code>https</code> but we show setting the scheme to show it can be
	        // done. The port for secured communication is also set here.
	        HttpConfiguration http_config = new HttpConfiguration();

	        // HTTP connector
	        // The first server connector we create is the one for http, passing in
	        // the http configuration we configured above so it can get things like
	        // the output buffer size, etc. We also set the port (8080) and
	        // configure an idle timeout.
	        ServerConnector httpconnector = new ServerConnector(server,
	                new HttpConnectionFactory(http_config));
	        httpconnector.setPort(8080);
	        httpconnector.setIdleTimeout(30000);

	        server.setConnectors(new Connector[] { httpconnector });



			// Setup the basic application "context" for this application at "/"
			// This is also known as the handler tree (in jetty speak)
			// ServletContextHandler context = new
			// ServletContextHandler(ServletContextHandler.SESSIONS);

			String webDir = Main.class.getProtectionDomain().getCodeSource()
					.getLocation().toExternalForm(); // Results in
														// D:/Work/eclipseworkspace/testJettyResult/testJetty.jar

			context = new WebAppContext(webDir, "/");
			context.setWelcomeFiles(new String[] { "index.html" });

			context.setContextPath("/");
/*
			AdminIndex admin=new AdminIndex();
			context.addServlet(new ServletHolder(admin), admin.getPath()+"*");*/
			server.setHandler(context);

			try {
			
				server.start();
				server.dump(System.err);
			} catch (Throwable t) {
				System.err.println(t);

			}
		
	}
}
