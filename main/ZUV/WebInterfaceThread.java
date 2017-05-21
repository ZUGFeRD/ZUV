package ZUV;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;


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

			UploadServlet uploadServlet=new UploadServlet();
			context.addServlet(new ServletHolder(uploadServlet), "/upload/*");
			server.setHandler(context);

			try {
			
				server.start();
				server.dump(System.err);
			} catch (Throwable t) {
				System.err.println(t);

			}
		
	}
}
