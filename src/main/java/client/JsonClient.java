package client;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;

import server.JsonListenerServlet;
import structures.ClientEvent;

public class JsonClient extends Thread{
/* This class represents an HTTP Client that holds multiple possible connections to different
 * hosts, each with possible URI extensions. This client is meant to be a handler for all
 * data transfers that can be represented through JSON representation. 
*/
	
	private volatile Map<String, Connection> connections = new HashMap<String, Connection>();
//	A hashmap of hosts and their names that can be accessed via this client.
	
	private volatile boolean running = false;
//	A boolean that determines whether or not the client is running. 
	
	private MinMaxPriorityQueue<ClientEvent> events = MinMaxPriorityQueue.<ClientEvent>create();
//	MMPQ of client events
		
	private volatile int eventLimit = 20;
//	Maximum number of events in MMPQ of events
	
	private volatile JsonListenerServlet servlet;
//	Servlet listening to incoming JSON requests
			
	public JsonClient(JsonListenerServlet jLS){
		servlet = jLS;
	}
	public JsonClient(){
	}
	public JsonClient(String connectionName, Connection connection){
		addConnection(connectionName, connection);
	}

	@Override
	public void run(){
		/* Starts up the client in an asynchronous fashion (behind the scenes multi-thread).
		 * Goes through a while loop executing client events, which are triggered when
		 * a connection is opened that provides a link between the asynchronous HTTP request
		 * and the action that the client takes once the response is captured.
		*/
		running = true;
		while(running == true){
			accessEvents("run", this);
		}
	}
	public synchronized Connection addConnection(String connectionName, Connection connection){
		connections.put(connectionName, connection);
		return connection;
	}
	public synchronized Connection removeConnection(String connectionName){
		return connections.remove(connectionName);
	}
	public synchronized Connection getConnection(String connectionName){
		return connections.get(connectionName);
	}
	public void shutDown(){
		running = false;
	}
	private synchronized void accessEvents(String method, Object... o){
		if(method.equals("execute")){
			events.add((ClientEvent) o[0]);
		}else if(method.equals("run")){
			if(events.size() > eventLimit)
				try {
					events.removeLast().abortExecute();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			/* Provides a way of removing client events once the queue is overflowed and
			 * runs the abort code for the event in a separate thread.
			*/
			Iterator<ClientEvent> eventsIterator = events.iterator();
			while(eventsIterator.hasNext()){
				ClientEvent e = eventsIterator.next();
				if(e.isTimedOut()){
					eventsIterator.remove();
					try {
						e.timeOutExcecute();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					/* This checks to see if an event has timed out of its specified timeout
					 * length. If so, it will execute some code.
					*/
					
				}else if(e.getInitiatingEvent().isDone()){
					try {
						eventsIterator.remove();
						e.execute();
						System.out.println("Event executed");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					/* Executes event's code if the HTTP request has completed
					 * and returned a JSON response.
					*/
				
				}
			}
		}
	}
	public synchronized void execute(ClientEvent e){
		accessEvents("execute", e);
	}
}