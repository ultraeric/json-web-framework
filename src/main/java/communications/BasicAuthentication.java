package communications;

public class BasicAuthentication {
	private String user = "";
	private String pw = "";
	private boolean preemptive = false;
	public BasicAuthentication(String u, String p){
		user = u;
		pw = p;
	}
	public BasicAuthentication setPreemptive(){preemptive = true; return this;}
	public BasicAuthentication setNotPreemptive(){preemptive = false; return this;}
	public boolean isPreemptive(){return preemptive;}
	public String getUser(){return user;}
	public String getPass(){return pw;}
}
