/*   
  	This file is part of JSON Conversion and JSON Web Framework.

    JSON Conversion and JSON Web Framework is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JSON Conversion and JSON Web Framework is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with JSON Conversion and JSON Web Framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package functions;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Primitives;

/**
 * @author Yiqi (Eric) Hou
 *
 * <p>
 * A builder/factory class that reads instances of objects and builds JSON representations of them.
 * Made for dynamic creation of JSONs from objects without creation of a specific method to 
 * build the JSON.
 */
public class JsonBuilder {
	private Object obj;
	
	/**
	 * Constructor.
	 * 
	 * @param o	Object to be turned into JSON.
	 */
	public JsonBuilder(Object o){
		obj = o;
	}
	
	
	/**
	 * Builds the JSON from the object specified in the constructor.
	 * 
	 * @return	A Map representation of the object.
	 */
	public Map<String, Object> build(){
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		for(Field f: obj.getClass().getDeclaredFields()){
			Class<?> T = f.getType();
			f.setAccessible(true);
			try{
				if(T.isPrimitive()){
					switch(T.getName()){
					case "int": jsonMap.put(f.getName(), f.getInt(obj));
								break;
					case "double": jsonMap.put(f.getName(), f.getDouble(obj));
								break;
					case "boolean": jsonMap.put(f.getName(), f.getBoolean(obj));
								break;
					case "byte": jsonMap.put(f.getName(), f.getByte(obj));
								break;
					case "short": jsonMap.put(f.getName(), f.getShort(obj));
								break;
					case "long": jsonMap.put(f.getName(), f.getLong(obj));
								break;
					case "float": jsonMap.put(f.getName(), f.getFloat(obj));
								break;
					case "char": jsonMap.put(f.getName(), f.getChar(obj));
								break;
					}
					System.out.println("Primitive added");
				}else{
					if(T.equals(String.class)){
						jsonMap.put(f.getName(), f.get(obj));
						System.out.println("String added");
					}else if(List.class.isAssignableFrom(T)){
						ParameterizedType listPType = (ParameterizedType) f.getGenericType();
				        Class<?> listType = (Class<?>) listPType
				        							.getActualTypeArguments()[0];
				        if(Primitives.unwrap(listType).isPrimitive()){
				        	jsonMap.put(f.getName(), (ArrayList<Object>) f.get(obj));
				        }else{
				        	if(listType.isAssignableFrom(String.class)){
				        		jsonMap.put(f.getName(), (ArrayList<Object>) f.get(obj));
				        	}else{
								List<Object> subList = (ArrayList<Object>) f.get(obj);
								List<Map<String, Object>> subListJsonMap =
										new ArrayList<Map<String, Object>>();
								for(Object o: subList){
									subListJsonMap.add(new JsonBuilder(o).build());
								}
								jsonMap.put(f.getName(), subListJsonMap);
				        	}
				        }
				        System.out.println("List added");
					}else{
						jsonMap.put(f.getName(), new JsonBuilder(f.get(obj)).build());
						System.out.println("Other added");
					}
				}
			}catch(Exception e){
				System.out.println("Error: Object does not exist or other issue");
				continue;
			}
			f.setAccessible(false);
		}
		return jsonMap;
	}
}
