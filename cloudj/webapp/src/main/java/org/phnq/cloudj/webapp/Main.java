package org.phnq.cloudj.webapp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author pgostovic
 */
public class Main {
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setDescriptor("target/webapp/WEB-INF/web.xml");
        webapp.setResourceBase("target/webapp");
        webapp.setParentLoaderPriority(true);
        
        
        server.setHandler(webapp);

        server.start();
        server.join();
    }
}
