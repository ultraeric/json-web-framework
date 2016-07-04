package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer extends Thread{
	int serverPort = 0;
	public UDPServer(int port){
		this.serverPort = port;
	}
	
	@Override
	public void run(){
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] receiveData = new byte[1024];
	    byte[] sendData = new byte[1024];
	    while(true)
        {
           DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
           try {
        	   serverSocket.receive(receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           String sentence = new String(receivePacket.getData());
           System.out.println("RECEIVED: " + sentence);
           InetAddress IPAddress = receivePacket.getAddress();
           int port = receivePacket.getPort();
           String capitalizedSentence = sentence.toUpperCase();
           sendData = capitalizedSentence.getBytes();
           DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
           try {
				serverSocket.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           receiveData =  new byte[1024];
           sendData = new byte[1024];
        }
	}
}
