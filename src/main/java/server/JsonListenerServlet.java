package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.Servlet;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.JsonClient;
import structures.ServerEvent;

/**
 * @author Yiqi (Eric) Hou
 * 
 */
public class JsonListenerServlet extends HttpServlet implements Servlet{
	private JsonClient jsonClient;
	private ServerEvent postEvent;
	private ServerEvent getEvent;
	private ServerEvent deleteEvent;
	private ServerEvent putEvent;
	public ServerEvent getPostEvent() {
		return postEvent;
	}
	public JsonListenerServlet setPostEvent(ServerEvent postEvent) {
		this.postEvent = postEvent;
		return this;
	}
	public ServerEvent getGetEvent() {
		return getEvent;
	}
	public JsonListenerServlet setGetEvent(ServerEvent getEvent) {
		this.getEvent = getEvent;
		return this;
	}
	public ServerEvent getDeleteEvent() {
		return deleteEvent;
	}
	public JsonListenerServlet setDeleteEvent(ServerEvent deleteEvent) {
		this.deleteEvent = deleteEvent;
		return this;
	}
	public ServerEvent getPutEvent() {
		return putEvent;
	}
	public JsonListenerServlet setPutEvent(ServerEvent putEvent) {
		this.putEvent = putEvent;
		return this;
	}
	public JsonListenerServlet(){
		super();
		jsonClient = new JsonClient(this);
	}
	public JsonListenerServlet(JsonClient c){
		super();
		jsonClient = c;
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String body = extractBody(request);
		String returnBodyJson = getEvent.execute(body);
		formatResponse(response, returnBodyJson);
	}
	@Override 
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String body = extractBody(request);
		String returnBodyJson = putEvent.execute(body);
		formatResponse(response, returnBodyJson);
	}
	@Override 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String body = extractBody(request);
		String returnBodyJson = postEvent.execute(body);
		formatResponse(response, returnBodyJson);
	}
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String body = extractBody(request);
		String returnBodyJson = deleteEvent.execute(body);
		formatResponse(response, returnBodyJson);
	}
	private String extractBody(HttpServletRequest request) throws IOException{
		String requestBody = "";
		if(request.getContentType().equals("application/json")){
			StringWriter writer = new StringWriter();
			ServletInputStream in = request.getInputStream();
			while(in.available() > 0){
				writer.write(in.read());
			}
			requestBody = writer.toString();
			writer.close();
			return requestBody;
		}
		else return "";
	}
	private void formatResponse(HttpServletResponse response, String body) throws IOException{
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(body);
	}
}
