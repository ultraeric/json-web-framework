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
