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
import java.util.Map;

public class HttpOptions {
	private Map<String, String> header = new HashMap<String, String>();
	
	public HttpOptions(){
		
	}
	public HttpOptions(String headerIdentifier, String value){
		header.put(headerIdentifier, value);
	}
	public HttpOptions addHeader(String headerIdentifier, String value){
		header.put(headerIdentifier, value);
		return this;
	}
	public Map<String, String> getHeaders(){
		return header;
	}
	
}
