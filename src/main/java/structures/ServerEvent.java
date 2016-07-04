package structures;

import java.util.Map;

import functions.JsonBuilder;
import functions.JsonFunctions;
import functions.JsonInterpreter;

/**
 * @author Yiqi (Eric) Hou
 * 
 * <p>
 * An event that the JsonListenerServlet will run upon the receiving of an HTTP request.
 * Allows API users to designate their own functionality to handle HTTP requests. Note that
 * the input and outputs must be classes that can be represented through JSON. 
 *
 * @param <T1> Type of input
 * @param <T2> Type of output
 */
public abstract class ServerEvent<T1, T2> {
	private Class<T1> type1;
	
	/**
	 * Constructor. 
	 * 
	 * @param t1 The Class object representing the input type.
	 */
	public ServerEvent(Class<T1> t1){
		setInputType(t1);
	}
	public void setInputType(Class<T1> t1){
		type1 = t1;
	}
	
	/**
	 * The code to execute when an object of type T1 is received through a JSON in an HTTP request.
	 * Allows customization of functionality for the API user to designate how the return object
	 * is generated and how the input object is handled.
	 * 
	 * @param requestObject The object sent through JSON in an HTTP request.
	 * @return The object to return in the HTTP response.
	 */
	public abstract T2 internalExecute(T1 requestObject);
	
	
	public String execute(String s){
		return JsonFunctions.objectToJson(
				new JsonBuilder(
					internalExecute(
						(T1) new JsonInterpreter(type1, s)
					.build()))
				.build());
	}
}
