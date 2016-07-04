package structures;

public class HostNotValidException extends RuntimeException{
	public HostNotValidException(){
		super();
		System.out.println("Host not valid. Please check subHost and base host names.");
	}
}
