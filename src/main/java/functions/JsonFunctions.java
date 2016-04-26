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

package functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 * @author Yiqi (Eric) Hou
 *
 * <p>
 * A collection of JSON-based functions interpreted using Google's GSON API.
 *
 */
public class JsonFunctions {
	private static Map<String, Object> jsonObject = new HashMap<String, Object>();
	private static Gson jsonWorker = new Gson();
	private static JsonParser jsonParser = new JsonParser();
	
	/**
	 * Takes a JSON in String format and turns it into the format of Map<String, Object>. Used
	 * in intermediate conversions during HTTP requests/responses and as a utility method.
	 * 
	 * @param jsonString	The JSON as a String.
	 * @return				The JSON as a Map.
	 */
	public static Map<String, Object> jsonToObject(String jsonString){
		return jsonWorker.fromJson(jsonParser.parse(jsonString), jsonObject.getClass());
	}
	
	/**
	 * Takes a JSON in String format and turns it into the format of List<Map<String, Object>>.
	 * Refrain from using this.
	 * 
	 * @param jsonString	JSON as a String.
	 * @return				Returns the list.
	 */
	public static List<Map<String, Object>> jsonToList(String jsonString){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JsonArray jsonArray = jsonParser.parse(jsonString).getAsJsonArray();
		for(int i = 0; i < jsonArray.size(); i++){
			list.add((Map<String, Object>) 
					jsonWorker.fromJson(jsonString, jsonObject.getClass()));
		}
		return list;
	}
	
	/**
	 * Turns a Map representation of a JSON into a String representation of it.
	 * 
	 * @param o	The JSON as a Map.
	 * @return	The JSON as a String.
	 */
	public static String objectToJson(Map<String, Object> o){
		return jsonWorker.toJson(o);
	}
}
