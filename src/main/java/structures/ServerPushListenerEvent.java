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

import client.Connection;
import client.JsonClient;

/**
 * @author Yiqi (Eric) Hou
 *
 * <p>
 * An abstract class that ties together an asynchronous HTTP request that is open
 * and waiting for a response and the code that will be executed after that response
 * is received. Note that the format of the response is not known and must be checked
 * within the method @internalExecute.
 * 
 * Attempts to keep a constant connection to the server, listening to pushes coming from the
 * server resource that this is connected to. Please include a header description "x_method: push"
 */
public abstract class ServerPushListenerEvent extends ClientEvent{
	private JsonClient c;
	private Connection conn;
	private String extName;
	
	public ServerPushListenerEvent(JsonClient c, Connection conn, String extName) {
		super(conn.httpGetRaw(extName));
		this.c = c;
	}
	
	/**
	 * Method that creates the next listener in the server push listener chain. As the push
	 * listener from the server is merely a self-replicating event, this dictates how to
	 * replicate the event.
	 * 
	 * @param conn		The Connection to listen for pushes from
	 * @param extName	The extension of the Connection to listen for pushes from
	 * @return			The next event in the self-replicating event chain.
	 */
	protected abstract ServerPushListenerEvent createNextPushListener(Connection conn, String extName);
	
	/**
	 * Whatever is to be done internally by the response that is returned to this ClientEvent.
	 * Get the response using this.getInitiatingEvent().get();. 
	 * 
	 */
	protected abstract void internalExecute();
	@Override
	public final void run(){
		c.addEvent(createNextPushListener(conn, extName));
		internalExecute();
	};
}
