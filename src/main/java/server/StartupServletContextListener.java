package server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * @author Yiqi (Eric) Hou
 * 
 * This class starts up the UDPServer on Tomcat server startup.
 *
 */
public class StartupServletContextListener implements ServletContextListener{
	
	int udpServerPort;
	
	public StartupServletContextListener(int udpServerPort){
		this.udpServerPort = udpServerPort;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		UDPServer udpServer = new UDPServer(udpServerPort);
		udpServer.start();
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}
	
}
