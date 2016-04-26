
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
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Primitives;

import structures.JsonMapObject;


/**
 * @author Yiqi (Eric) Hou
 * 
 * <p>
 * A builder/factory class that reads JSON representations of an object and returns
 * an Object representation of the object that can be used by Java. Made for dynamic
 * interpretation and building of objects that are in JSON formation, typically for
 * web based applications. 
 * 
 * <p>
 * User must ensure to check the type of JSON and the type it is being interpreted as.
 * This interpreter assumes that the JSON can be represented as an object of the specified
 * type and will try to populate the fields of the object as best as it can.
 * 
 * <p> 
 * Before using, a JSON representation of the object must be passed in String or Map format. 
 * A Class object must also be passed in, representing the type of the JSON to be interpreted
 * and built.
 */
public class JsonInterpreter{

	private Class<?> typeClass;

	private Map<String, Object> jsonMap;

	/**
	 * Constructor. 
	 * 
	 * @param T		A Class object specifying the type that the JSON should be cast to.
	 * @param json	The String representation of the JSON to be built.
	 */
	public JsonInterpreter(Class<?> T, String json){
		this(T, JsonFunctions.jsonToObject(json));
	}
	
	/**
	 * Constructor from an object already in Map format.
	 * 
	 * @param T		A Class object specifying the type that the JSON should be cast to.
	 * @param map	The Map representation of the JSON to be built.
	 */
	public JsonInterpreter(Class<?> T, Map<String, Object> map){
		this.typeClass = T;
		this.jsonMap = map;
	}
	
	/**
	 * @param json	The JSON as a String.
	 * @return		Returns current instance of interpreter.
	 */
	public JsonInterpreter setJson(String json){
		return setJson(JsonFunctions.jsonToObject(json));
	}
	
	/**
	 * @param map	The JSON as a Map.
	 * @return		Returns current instance of interpreter.
	 */
	public JsonInterpreter setJson(Map<String, Object> map){
		jsonMap = map;
		return this;
	}
	
	/**
	 * Builds the object from a JSON input specified by the Class parameter. This method assumes
	 * the Object returned is being assigned to a variable of the specified type,
	 * and also assumes that the JSON can be cast to the specified type. External checking of
	 * JSON adherence to the type is recommended.
	 * 
	 * @param T		Class that the JSON is representing.
	 * @param json	The JSON as a String.
	 * @return		The Object represented by the JSON.
	 */
	public <T> T build(Class<?> T, String json){
		return (T) build(T, JsonFunctions.jsonToObject(json));
	}
	
	/**
	 * Builds the object from a JSON input specified by the Class parameter. This method assumes
	 * the Object returned is being assigned to a variable of the specified type,
	 * and also assumes that the JSON can be cast to the specified type. External checking of
	 * JSON adherence to the type is recommended.
	 * 
	 * @param T		Class that the JSON is representing.
	 * @param map	The JSON as a Map.
	 * @return		The Object represented by the JSON.
	 */
	public <T> T build(Class<?> T, Map<String, Object> map){
		this.typeClass = T;
		this.jsonMap = map;
		return (T) build();
	}
	
	
	/**
	 * Builds the object given the JSON and Class type already assigned. This method assumes
	 * the Object returned is being assigned to a variable of the specified type,
	 * and also assumes that the JSON can be cast to the specified type. External checking of
	 * JSON adherence to the type is recommended.
	 * 
	 * @return	The Object represented by the JSON.
	 */
	public <T> T build(){
		T returnObject;
		try{
			returnObject = (T) typeClass.getConstructors()[0].newInstance();
			for(Field f: typeClass.getDeclaredFields()){
				Class<?> subType = f.getType();
				String name = f.getName();
				f.setAccessible(true);
				try{
					if(subType.isPrimitive()){
						switch(subType.getName()){
						case "int": f.set(returnObject, (int) Math.round((double) jsonMap.get(name)));
									break;
						case "double": f.set(returnObject, (double) jsonMap.get(name));
									break;
						case "boolean": f.set(returnObject, (boolean) jsonMap.get(name));
									break;
						case "byte": f.set(returnObject, (byte) Math.round((double) jsonMap.get(name)));
									break;
						case "short": f.set(returnObject, (short) Math.round((double) jsonMap.get(name)));
									break;
						case "long": f.set(returnObject, (long)(double) jsonMap.get(name));
									break;
						case "float": f.set(returnObject, (float)(double) jsonMap.get(name));
									break;
						case "char": f.set(returnObject, (char) jsonMap.get(name));
									break;
						}
						System.out.println("Primitive Set");
					}else{
						if(subType.equals(String.class)){
							f.set(returnObject, getString(name));
							System.out.println("String Set");
						}else if(List.class.isAssignableFrom(subType)){
							
//							Following 3 lines extract the type that the List field holds.
							ParameterizedType listPType = (ParameterizedType) f.getGenericType();
					        Class<?> listType = (Class<?>) listPType
					        							.getActualTypeArguments()[0];
					        if(Primitives.unwrap(listType).isPrimitive()){
					        	f.set(returnObject, getPrimitiveList(name));
					        }else{
					        	if(listType.isAssignableFrom(String.class)){
					        		f.set(returnObject, getPrimitiveList(name));
					        	}else{
					        		f.set(returnObject, getNonPrimitiveList(listType, name));
					        	}
					        }
					        System.out.println("List set");
					        continue;
						}
						f.set(returnObject, getNonPrimitive(subType, name));
						System.out.println("Nonprimitive Set");
					}
				}catch(Exception e){
					continue;
				}
				f.setAccessible(false);
			}
			return returnObject;
		}catch(Exception e){
			System.out.println("Failed Build");
		}
		return null;
	}
	protected <T> ArrayList<T> getPrimitiveList(String key){
		return (ArrayList<T>) jsonMap.get(key);
	}
	/**
	 * Gets from the JSON a list of non-primitives. For each non-primitive, interprets the
	 * sub-JSON for that list extracted from the current JSON and casts it to the specified
	 * non-primitive type.
	 * 
	 * @param T		Non-primitive type that the list holds
	 * @param key	Name of the list
	 * @return		Returns an ArrayList of the name <b>key</b> containing objects of type <b>T</b>.
	 */
	protected <T> ArrayList<T> getNonPrimitiveList(Class<?> T, String key){
		List<Map<String, Object>> subJsonList = 
				(ArrayList<Map<String, Object>>) jsonMap.get(key);
		ArrayList<T> returnList = new ArrayList<T>();
		for(Map<String, Object> subJsonMap: subJsonList){
			returnList.add((T) new JsonInterpreter(T, subJsonMap).build());
		}
		return returnList;
	}
	
	protected String getString(String key){
		return (String) jsonMap.get(key);
	}
	protected <T> T getNonPrimitive(Class<?> T, String key){
		return (T) new JsonInterpreter(T, getSubJsonMap(key)).build();
	}
	protected Map<String, Object> getSubJsonMap(String key){
		return (Map<String, Object>) jsonMap.get(key);
	}
}
