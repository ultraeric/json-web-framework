/*   
  	This file is part of JSON Web Framework.

    JSON Web Framework is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JSON Web Framework is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with JSON Web Framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package client;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;

import server.JsonListenerServlet;
import structures.ClientEvent;

/**
 * @author Yiqi (Eric) Hou
 * 
 * <p>
 * This class represents an HTTP Client that holds multiple possible connections to different
 * hosts, each with possible URI extensions. This client is meant to be a handler for all
 * data transfers that can be represented through JSON representation. 
 *
 */
public class JsonClient extends Thread{
	
	public static final int ADD_EVENT = 1;
	public static final int EXECUTE_EVENTS = 2;
	
	private volatile Map<String, Connection> connections = new HashMap<String, Connection>();
	
	private volatile boolean running = false;
	
	private MinMaxPriorityQueue<ClientEvent> events = MinMaxPriorityQueue.<ClientEvent>create();
		
	private int eventLimit = 20;
	
	private JsonListenerServlet servlet;
	
	private Connection lanConnection;
			
	/**
	 * Constructor linking this JsonClient to a servlet when applicable.
	 * 
	 * @param jLS	The JsonListenerServlet linked to this client.
	 */
	public JsonClient(JsonListenerServlet jLS){
		servlet = jLS;
	}
	
	public JsonClient(){
	}
	
	
	public JsonClient(String connectionName, Connection connection){
		addConnection(connectionName, connection);
	}

	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run(){
		/* Starts up the client in an asynchronous fashion (behind the scenes multi-thread).
		 * Goes through a while loop executing client events, which are triggered when
		 * a connection is opened that provides a link between the asynchronous HTTP request
		 * and the action that the client takes once the response is captured.
		*/
		running = true;
		while(running == true){
			accessEvents(EXECUTE_EVENTS, this);
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
	private synchronized void accessEvents(int methodID, Object... o){
		if(methodID == ADD_EVENT){
			events.add((ClientEvent) o[0]);
		}else if(methodID == EXECUTE_EVENTS){
			executeEvents();
		}
	}
	private void executeEvents(){
		if(events.size() > eventLimit)
			try {
				events.removeLast().abortedExecute();
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
					e.timedOutExcecute();
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
					e.start();
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
	public synchronized void addEvent(ClientEvent e){
		accessEvents(ADD_EVENT, e);
	}
	//Broadcast to broadcast channel + port; once a response is received, add to the client the 
//	LAN server's connection information.
	public void discoverLANServer(){
		
	}
}