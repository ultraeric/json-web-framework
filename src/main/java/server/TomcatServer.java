
//NOTE: To get LAN connection working, start a UDP Server in another thread with a DatagramSocket
//The client should send a broadcast message, and the server should return an affirmative response.
//The client extracts host information from this response, and connects to the specific URI
//resource that it wants.

//NOTE: Make sure that tomcat access is thread-safe.


package server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;

import com.jcabi.aspects.Async;

import client.Connection;
import client.JsonClient;
import example.ChatMessage;
import example.GetMessageEvent;
import example.SendMessageEvent;

public class TomcatServer extends Thread{
	private volatile Tomcat tomcat = new Tomcat();
	private volatile Properties serverProperties = new Properties();
	private volatile String serverPropertiesFileName = "server.properties";
	private volatile Context serverRootContext;
	private boolean initialized = false;
	private volatile int listeningServerPort = 0;

	public Tomcat getTomcat() {
		return tomcat;
	}
	public void setTomcat(Tomcat tomcat) {
		this.tomcat = tomcat;
	}
	public Properties getServerProperties() {
		return serverProperties;
	}
	public void setServerProperties(Properties serverProperties) {
		this.serverProperties = serverProperties;
	}
	public String getServerPropertiesFileName() {
		return serverPropertiesFileName;
	}
	public void setServerPropertiesFileName(String serverPropertiesFileName) {
		this.serverPropertiesFileName = serverPropertiesFileName;
	}
	
	/**
	 * Initialization of the tomcat's important variables, such as
	 * port, the root context, whether or not it is initialized, etc.
	 * 
	 * @return	The tomcat that was initialized.
	 */
	public TomcatServer initialize(){
		try{
			initializeProperties();
		}catch(Exception e){
			e.printStackTrace();
		}
		tomcat.setPort(Integer.parseInt(serverProperties.getProperty("server_port")));
		serverRootContext =
				tomcat.addContext(serverProperties.getProperty("server_uri_extension"),
						new File(System.getProperty(getProperty("server_root"))).getAbsolutePath());
		if(Boolean.parseBoolean(serverProperties.getProperty("lan_discovery_enabled"))){
			listeningServerPort = Integer.parseInt(serverProperties.getProperty("listener_server_port"));
		}
		initialized = true;
		return this;
	}
	
	@Override
	public void run(){
		if(initialized == false){this.initialize(); initialized = true;}

		try {
			tomcat.start();
		} catch (LifecycleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Tomcat Started");
		
		if(Boolean.parseBoolean(serverProperties.getProperty("lan_discovery_enabled"))){
			this.addServletContextListener(new StartupServletContextListener(listeningServerPort));
		}
		
		tomcat.getServer().await();
	}
	public Context getServerRootContext() {
		return serverRootContext;
	}
	public void setServerRootContext(Context serverRootContext) {
		this.serverRootContext = serverRootContext;
	}
	public void addServlet(Context c, String ext, String name, HttpServlet s){
		tomcat.addServlet(c, name, s);
		c.addServletMapping(ext, name);
	}
	private void addFilter(Context c, String name, Filter f, String... servletNames){
		FilterDef filterDef = new FilterDef();
		filterDef.setFilterName(name);
		filterDef.setFilterClass(f.getClass().getName());
		c.addFilterDef(filterDef);
		FilterMap filterMap = new FilterMap();
		filterMap.setFilterName(name);
		for(String s: servletNames){
			filterMap.addServletName(s);
		}
		c.addFilterMap(filterMap);
	}
	public void addFilter(String name, Filter f, String... servletNames){
		addFilter(serverRootContext, name, f, servletNames);
	}
	
	public TomcatServer addServlet(String extension, String name, HttpServlet s){
		addServlet(serverRootContext, extension, name, s);
		return this;
	}
	public TomcatServer addServletContextListener(ServletContextListener scl){
		serverRootContext.addApplicationListener(scl.getClass().getName());
		return this;
	}
	private void initializeProperties() throws IOException{
		InputStream propertiesInputStream = getClass().getClassLoader()
				.getResourceAsStream(serverPropertiesFileName);
		serverProperties.load(propertiesInputStream);
		propertiesInputStream.close();
	}
	private Properties createProperties() throws IOException{
		Properties p = new Properties();
		InputStream propertiesInputStream = getClass().getClassLoader()
				.getResourceAsStream(serverPropertiesFileName);
		p.load(propertiesInputStream);
		propertiesInputStream.close();
		return p;
	}
	
	private String getProperty(String s){
		return serverProperties.getProperty(s);
	}
	
}
