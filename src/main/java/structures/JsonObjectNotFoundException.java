package structures;

public class JsonObjectNotFoundException extends RuntimeException{
	public JsonObjectNotFoundException(){
		super();
		System.out.println("JsonObjectNotFoundException: Please check JSON structure");
	}
}
