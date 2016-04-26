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

package structures;

import java.util.concurrent.Future;

import com.jcabi.aspects.Async;

import client.Connection;

/**
 * An abstract class that ties together an asynchronous HTTP request that is open
 * and waiting for a response and the code that will be executed after that response
 * is received. Used for asynchronous processing of client requests to a remote
 * resource.
*/
public abstract class ClientEvent implements Comparable{

	
	private int timeOutMS = 20000;
	
	private long timeStartMS = System.currentTimeMillis();
	private int priority;
	private Future initiatingEvent;
	private Connection initiatingConnection;
	
	public Connection getInitiatingConnection(){
		return initiatingConnection;
	}
	public void setInitiatingConnection(Connection initiatingConnection) {
		this.initiatingConnection = initiatingConnection;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getTimeOutMS() {
		return timeOutMS;
	}
	public void setTimeOutMS(int timeOutMS) {
		this.timeOutMS = timeOutMS;
	}
	public long getTimeStartMS() {
		return timeStartMS;
	}
	public void setTimeStartMS(long timeStartMS) {
		this.timeStartMS = timeStartMS;
	}
	public boolean isTimedOut(){
		System.out.println(getTimeElapsed() - timeOutMS);
		return getTimeElapsed() > timeOutMS;
	}
	public long getTimeElapsed(){
		return (System.currentTimeMillis() - timeStartMS);
	}
	public Future getInitiatingEvent(){
		return initiatingEvent;
	}
	public void setInitiatingEvent(Future f){
		initiatingEvent = f;
	}
	public ClientEvent(Future ini){
		initiatingEvent = ini;
	}
	@Async
	public abstract void timeOutExcecute() throws Exception;
	@Async
	public abstract void execute() throws Exception;
	@Async
	public abstract void abortExecute() throws Exception;
	@Override
	public int compareTo(Object o){
		if(o instanceof ClientEvent){
			ClientEvent ce = (ClientEvent) o;
			if(ce.getPriority() < this.getPriority()) return -1;
			else if(ce.getPriority() > this.getPriority()) return 1;
			else{
				if(ce.getTimeElapsed() > this.getTimeElapsed()) return -1;
				else if(ce.getTimeElapsed() < this.getTimeElapsed()) return 1;
				else return 0;
			}
		}
		else return 0;
	}
}
