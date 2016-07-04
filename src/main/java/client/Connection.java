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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.scheduling.annotation.AsyncResult;

import com.jcabi.aspects.Async;

import communications.BasicAuthentication;
import functions.JsonFunctions;
import structures.HostNotValidException;


/**
 * @author Yiqi (Eric) Hou
 *
 * <p>
 * A class summarizing the connection between this client and a host. Includes URI resources
 * accessible through extensions of the host's URI. Handles HTTP basic authentication.
 * 
 * <p>
 * <b>Planned:</b>
 * Salting + hashing of password for more secure HTTP basic authentication. This will be handled
 * internally between the Tomcat server and the client, so no need for the user of this API to
 * handle anything.
 */
public class Connection {
	private HttpHost connectionHost;
	private String uriSchemePrefix = "";
	private BasicAuthentication basicAuth;
	private Map<String, String> uriExtensions = new HashMap<String, String>();
	private boolean basicAuthNeeded = false;
	
	/**
	 * Constructor. Not meant for API use, please utilize constructor with Apache's HttpHost
	 * instead.
	 * 
	 * @param base			The base URI for the requested host. Do not include URI scheme.
	 * @param port			Port of the requested host.
	 * @param uriScheme		URI scheme to be used (HTTP or HTTPS).
	 * @param username		Username to access host if needed.
	 * @param password		Password to access host if needed.
	 * @param preemptive	Whether or not preemptive authentication sending is required
	 * 							NOT RECOMMENDED.
	 */	
	protected Connection(String base, int port, String uriScheme, 
						String username, String password, boolean preemptive){
		setHost(base, port, uriScheme);
		setBasicAuth(username, password, preemptive);
	}
	
	
	/**
	 * Constructor. Use of this is preferred. Sets authentication as preemptive.
	 * 
	 * @param host	HttpHost of target host.
	 * @param auth	Authentication to access host if needed.
	 */
	public Connection(HttpHost host, BasicAuthentication auth){
		setHost(host);
		setBasicAuth(auth);
	}
	
	/**
	 * Constructor. Use of this is not recommended.
	 * 
	 * @param host			HttpHost of target host.
	 * @param username		Username to access host if needed.
	 * @param password		Password to access host if needed.
	 * @param preemptive	Sets whether preemptive authentication sending is required.
	 * 							NOT RECOMMENDED.
	 */
	public Connection(HttpHost host, String username, String password,
						boolean preemptive){
		setHost(host);
		setBasicAuth(username, password, preemptive);
	}
	
	/**
	 * Constructor. Use of this is not recommended.
	 * 
	 * @param base			The base URI for the requested host. Do not include URI scheme.
	 * @param port			Port of the requested host.
	 * @param uriScheme		URI scheme to be used (HTTP or HTTPS).
	 * @param auth	Authentication to access host if needed.
	 */
	public Connection(String base, int port, String uriScheme, BasicAuthentication auth){
		setHost(base, port, uriScheme);
		setBasicAuth(auth);
	}
	
	
	/**
	 * Constructor. Use when authentication is not required. Not recommended.
	 * 
	 * @param base			The base URI for the requested host. Do not include URI scheme.
	 * @param port			Port of the requested host.
	 * @param uriScheme		URI scheme to be used (HTTP or HTTPS).
	 */
	public Connection(String base, int port, String uriScheme){
		setHost(base, port, uriScheme);
	}
	
	/**
	 * Constructor. Use when authentication is not required. Recommended.
	 * 
	 * @param host	The HttpHost for this connection.
	 */
	public Connection(HttpHost host){
		setHost(host);
	}
	
	
	/**
	 * Constructor. Use when the details of this connection are not known upon instantiation.
	 * If a host is still not set upon the calling of an HTTP method, throws a HostNotValidException.
	 */
	public Connection(){
		
	}
	
	
	/**
	 * Sets the HTTP basic authentication for the connection to this host. Not safe unless
	 * used in conjunction with SSL/HTTPS. If not using secure connection please use 
	 * the salted and hashed authentication instead.
	 * 
	 * @param username		Username to access host if needed.
	 * @param password		Password to access host if needed.
	 * @param preemptive	Sets whether preemptive authentication sending is required.
	 * 							NOT RECOMMENDED.
	 */
	public void setBasicAuth(String username, String password, boolean preemptive){
		basicAuth = new BasicAuthentication(username, password);
		if(preemptive) basicAuth.setPreemptive();
		basicAuthNeeded = true;
	}
	
	
	/**
	 * Sets the HTTP basic authentication for the connection to this host. Not safe unless
	 * used in conjunction with SSL/HTTPS. If not using secure connection please use 
	 * the salted and hashed authentication instead.
	 * 
	 * @param a BasicAuthentication to be used to access host if needed.
	 */
	public void setBasicAuth(BasicAuthentication a){this.basicAuth = a; basicAuthNeeded = true;}
	public void removeBasicAuth(){basicAuth = null; basicAuthNeeded = false;}
	
	
	/**
	 * Sets the host for this connection. Use this method with the empty constructor if the HttpHost
	 * is not known at time of instantiation. 
	 * 
	 * @param base			The base URI for the requested host. Do not include URI scheme.
	 * @param port			Port of the requested host.
	 * @param uriScheme		URI scheme to be used (HTTP or HTTPS).
	 * @return				Returns this instance of HttpHost.
	 */
	public HttpHost setHost(String base, int port, String uriScheme){
		setUriSchemePrefix(uriScheme);
		return connectionHost = new HttpHost(base, port, uriScheme);
	}
	
	
	/**
	 * Sets the host for this connection. Use this method with the empty constructor if the HttpHost
	 * is not known at time of instantiation. 
	 * 
	 * @param host	The HttpHost for this connection.
	 * @return		Returns this instance of HttpHost.
	 */
	public HttpHost setHost(HttpHost host){
		setUriSchemePrefix(host.getSchemeName());
		return connectionHost = host;
	}
	public HttpHost getHost(){return connectionHost;}
	
	
	/**
	 * Sets the prefix to be prepended to the URI when an HTTP request is initiated.
	 * 
	 * @param uriScheme	The string naming the URI scheme to be used by the connection to this host.
	 * 						SSL/HTTPS is recommended when using HTTP basic authentication.
	 */
	private void setUriSchemePrefix(String uriScheme){
		String u = uriScheme;
		switch(u){
			case "HTTP": uriSchemePrefix = "http://";
							break;
			case "HTTPS": uriSchemePrefix = "https://";	
							break;
		}
	}
	
	
	/**
	 * Adds a URI extension to this host that can thereafter be referred to by the key.
	 * 
	 * @param extensionName	Nickname of the extension
	 * @param extension		URI extension to be appended to the base URI when requesting from this
	 * 							URI.
	 */
	public void addUriExtension(String extensionName, String extension){
		if(extension.contains(connectionHost.getHostName())){
			throw new HostNotValidException();
		}else{
			uriExtensions.put(extensionName, extension);
		}
	}
	
	public void removeUriExtension(String extensionName){
		uriExtensions.remove(extensionName);
	}
	
//	public Future<JsonMapObject> httpGet(String extensionName) throws InterruptedException, ExecutionException{
//		return new AsyncResult<JsonMapObject>(new JsonMapObject(httpGetRaw(extensionName).get()));
//	}
	
	/**
	 * An asynchronous HTTP GET request that sends a request to the requested resource given
	 * by the URI extension's nickname. Starts a new thread, please pass this future object
	 * to a ClientEvent to handle the execution of any object handling after this object is
	 * returned.
	 * 
	 * @param extensionName	Nickname of the extension.
	 * @return				Returns a Future object promising a Map representation of the returned
	 * 							JSON.
	 */
	@Async
	public Future<Map<String, Object>> httpGetRaw(String extensionName){
		return httpGetRaw(extensionName, null);
	}
	@Async
	public Future<Map<String, Object>> httpGetRaw(String extensionName, HttpOptions httpOptions){
		if(connectionHost == null){
			throw new HostNotValidException();
		}
		try{
			HttpClient client = initializeHttpClient();
			//Creates  client context, which includes authentication.
			HttpClientContext context = HttpClientContext.create();
			if(basicAuthNeeded && basicAuth.isPreemptive()){
				configurePreemptiveAuth(connectionHost, context);
			}
			//Creates the get context, which describes the actual message to the server
			HttpGet httpGetContext = new HttpGet(uriSchemePrefix 
												+ connectionHost.getHostName()
												+ uriExtensions.get(extensionName));
			System.out.println(uriSchemePrefix 
												+ connectionHost.getHostName()
												+ uriExtensions.get(extensionName));
			httpGetContext.setHeader("Content-Type", "application/json");
			HttpEntity responseEntity = null;
			
			addHttpOptions(httpGetContext, httpOptions);
			
			
			if(basicAuthNeeded && basicAuth.isPreemptive()){
				try{
					HttpResponse response = client.execute(httpGetContext, context);
					responseEntity = response.getEntity();
				}catch(Exception e){
					System.out.println("Error in getting GET HttpResponse and its entity"
							+ " with preemptive authentication.");
				}
			}else{
				try{
					HttpResponse response = client.execute(httpGetContext);
					responseEntity = response.getEntity();
				}catch(Exception e){
					System.out.println("Error in getting GET HttpResponse and its entity"
							+ " without preemptive authentication.");
				}
			}
			Map<String, Object> returnMap = JsonFunctions.jsonToObject(consolidateResponse(responseEntity));
			return new AsyncResult<Map<String, Object>>(returnMap);
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("httpGetRaw Failed");
		}
		return null;
	}
//	public String httpPost(String extensionName, JsonMapObject message){
//		return httpPostRaw(extensionName, message.asJson());
//	}
//	public String httpPostRaw(String extensionName, Map<String, Object> message){
//		return httpPostRaw(extensionName, JsonFunctions.objectToJson(message));
//	}
	
	
	/**
	 * An asynchronous HTTP POST request that sends a request to the requested resource given
	 * by the URI extension's nickname. Included with the POST request is a String that
	 * reflects a JSON object to be posted. Starts a new thread, please pass this future object
	 * to a ClientEvent to handle the execution of any object handling after this object is
	 * returned.
	 * 
	 * @param extensionName	Nickname of the extension.
	 * @param message		The JSON to be sent as a String.
	 * @return				Returns a Future object promising a String representing the result of
	 * 							the POST request.
	 */
	@Async
	public Future<String> httpPostRaw(String extensionName, String message){
		return httpPostRaw(extensionName, message, null);
	}
	@Async
	public Future<String> httpPostRaw(String extensionName, String message, HttpOptions httpOptions){
		if(connectionHost == null){
			throw new HostNotValidException();
		}
		try{
			HttpClient client = initializeHttpClient();
			//Creates  client context, which includes authentication.
			HttpClientContext context = HttpClientContext.create();
			if(basicAuthNeeded && basicAuth.isPreemptive()){
				configurePreemptiveAuth(connectionHost, context);
			}
			HttpPost httpPostContext = new HttpPost(uriSchemePrefix 
					+ connectionHost.getHostName()
					+ uriExtensions.get(extensionName));
			StringEntity messageEntity = new StringEntity(message);
			messageEntity.setContentType("application/json");
			httpPostContext.setEntity(messageEntity);
			HttpEntity responseEntity = null;
			
			addHttpOptions(httpPostContext, httpOptions);
				
			if(basicAuthNeeded && basicAuth.isPreemptive()){
				try{
					HttpResponse response = client.execute(httpPostContext, context);
					responseEntity = response.getEntity();
				}catch(Exception e){
					System.out.println("Error in getting POST HttpResponse and its entity"
							+ " with preemptive authentication.");
				}
			}else{
				try{
					HttpResponse response = client.execute(httpPostContext);
					responseEntity = response.getEntity();
				}catch(Exception e){
					System.out.println("Error in getting POST HttpResponse and its entity"
							+ " without preemptive authentication.");
				}
			}
			return new AsyncResult<String>(consolidateResponse(responseEntity));
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("httpPostRaw Failed");
		}
		return null;
	}
//	public String httpPut(String extensionName, JsonMapObject message){
//		return httpPutRaw(extensionName, message.asJson());
//	}
//	public String httpPutRaw(String extensionName, Map<String, Object> message){
//		return httpPutRaw(extensionName, JsonFunctions.objectToJson(message));
//	}
	
	/**
	 * An asynchronous HTTP PUT request that sends a request to the requested resource given
	 * by the URI extension's nickname. Included with the PUT request is a String that
	 * reflects a JSON object to be posted. Starts a new thread, please pass this future object
	 * to a ClientEvent to handle the execution of any object handling after this object is
	 * returned.
	 * 
	 * @param extensionName	Nickname of the extension.
	 * @param message		The JSON to be sent as a String.
	 * @return				Returns a Future object promising a String representing the result of
	 * 							the PUT request.
	 */
	@Async
	public Future<String> httpPutRaw(String extensionName, String message){
		return httpPutRaw(extensionName, message, null);
	}
	@Async
	public Future<String> httpPutRaw(String extensionName, String message, HttpOptions httpOptions){
		if(connectionHost == null){
			throw new HostNotValidException();
		}
		try{
			HttpClient client = initializeHttpClient();
			//Creates  client context, which includes authentication.
			HttpClientContext context = HttpClientContext.create();
			if(basicAuthNeeded && basicAuth.isPreemptive()){
				configurePreemptiveAuth(connectionHost, context);
			}
			HttpPut httpPutContext = new HttpPut(uriSchemePrefix 
					+ connectionHost.getHostName()
					+ uriExtensions.get(extensionName));
			StringEntity messageEntity = new StringEntity(message);
			messageEntity.setContentType("application/json");
			httpPutContext.setEntity(messageEntity);
			HttpEntity responseEntity = null;
			
				addHttpOptions(httpPutContext, httpOptions);
			
			if(basicAuthNeeded && basicAuth.isPreemptive()){
				try{
					HttpResponse response = client.execute(httpPutContext, context);
					responseEntity = response.getEntity();
				}catch(Exception e){
					System.out.println("Error in getting PUT HttpResponse and its entity"
							+ " with preemptive authentication.");
				}
			}else{
				try{
					HttpResponse response = client.execute(httpPutContext);
					responseEntity = response.getEntity();
				}catch(Exception e){
					System.out.println("Error in getting PUT HttpResponse and its entity"
							+ " without preemptive authentication.");
				}
			}
			return new AsyncResult<String>(consolidateResponse(responseEntity));
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("httpPutRaw Failed");

		}
		return null;
	}
	
	/**
	 * An asynchronous HTTP DELETE request that sends a request to the requested resource given
	 * by the URI extension's nickname. Starts a new thread, please pass this future object
	 * to a ClientEvent to handle the execution of any object handling after this object is
	 * returned.
	 * 
	 * @param extensionName	Nickname of the extension.
	 * @return				Returns a Future object promising a String representing the result of
	 * 							the DELETE request.
	 */
	@Async
	public Future<String> httpDelete(String extensionName){
		return httpDelete(extensionName, null);
	}
	@Async
	public Future<String> httpDelete(String extensionName, HttpOptions httpOptions){
		if(connectionHost == null){
			throw new HostNotValidException();
		}
		try{
			HttpClient client = initializeHttpClient();
			//Creates  client context, which includes authentication.
			HttpClientContext context = HttpClientContext.create();
			if(basicAuthNeeded && basicAuth.isPreemptive()){
				configurePreemptiveAuth(connectionHost, context);
			}
			HttpDelete httpDeleteContext = new HttpDelete(uriSchemePrefix 
					+ connectionHost.getHostName()
					+ uriExtensions.get(extensionName));
			HttpEntity responseEntity = null;
			
			addHttpOptions(httpDeleteContext, httpOptions);
			
			if(basicAuthNeeded && basicAuth.isPreemptive()){
				try{
					HttpResponse response = client.execute(httpDeleteContext, context);
					responseEntity = response.getEntity();
				}catch(Exception e){
					System.out.println("Error in getting DELETE HttpResponse and its entity"
							+ " with preemptive authentication.");
				}
			}else{
				try{
					HttpResponse response = client.execute(httpDeleteContext);
					responseEntity = response.getEntity();
				}catch(Exception e){
					System.out.println("Error in getting DELETE HttpResponse and its entity"
							+ " without preemptive authentication.");
				}
			}
			return new AsyncResult<String>(consolidateResponse(responseEntity));
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("httpDeleteRaw Failed");
		}
		return new AsyncResult<String>("");
	}
	
	/**
	 * @param responseEntity The entity that holds all of the information that will be included in the
	 * server's response.
	 * @return 
	 */
	private String consolidateResponse(HttpEntity responseEntity){
		try{
			String lineHolder = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
			StringBuilder stringBuilder = new StringBuilder();
			while(br.ready() && !(lineHolder = br.readLine()).equals(null)){
		        stringBuilder.append(lineHolder);
		    }
			br.close();
			return stringBuilder.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	private HttpClient initializeHttpClient(){
		//Creates credentials provider if credentials are needed 
		//and the mini-client that will create the single-use connection
		//to an address
		BasicCredentialsProvider credentialsProvider = null;
		if(basicAuthNeeded){
			credentialsProvider = getBasicCredentialsProvider();
			return HttpClientBuilder.create()
					.setDefaultCredentialsProvider(credentialsProvider)
					.build();
		}else{
			return HttpClientBuilder.create().build();
		}
	}
	//Creates and adds authentication to the host and client if preemptive
	//authentication is required.
	private void configurePreemptiveAuth(HttpHost host, HttpClientContext context){
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuthScheme = new BasicScheme();
		authCache.put(connectionHost, basicAuthScheme);
		context.setAuthCache(authCache);
	}
	
	private BasicCredentialsProvider getBasicCredentialsProvider(){
		UsernamePasswordCredentials httpGetCreds = 
				new UsernamePasswordCredentials(basicAuth.getUser(), basicAuth.getPass());
		BasicCredentialsProvider basicCredsProvider = new BasicCredentialsProvider();
		basicCredsProvider.setCredentials(AuthScope.ANY, httpGetCreds);
		return basicCredsProvider;
	}
	
	private void addHttpOptions(HttpRequestBase httpRequest, HttpOptions httpOptions){
		if(httpOptions == null){
			return;
		}
		for(String k: httpOptions.getHeaders().keySet()){
			String v = httpOptions.getHeaders().get(k);
			httpRequest.addHeader(k, v);
		}
	}
}
