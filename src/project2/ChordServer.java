package project2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import Chord.*;

public class ChordServer {
    public static Chord chord= new Chord();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader stdIn =
	            new BufferedReader(new InputStreamReader(System.in));
		int portNumber = -1;
		String serverIP ="localhost";
		System.out.println("This Is A Chord Server!");
		System.out.println("Usage: Designate a port number the server will listen on");
		System.out.println("Please Enter Port Number(1025~65535):");
		System.out.print(">>");				
		try {
			portNumber = Integer.parseInt(stdIn.readLine());
			serverIP = InetAddress.getLocalHost().getHostAddress();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean running = true;
		System.out.println("ChordServer is listenning on port "+ portNumber);
		String from;
		while(running){
			try (ServerSocket serverSocket = new ServerSocket(portNumber);	
			     Socket clientSocket = serverSocket.accept();
		         PrintWriter out =
		                new PrintWriter(clientSocket.getOutputStream(), true);
		         BufferedReader in = new BufferedReader(
		                new InputStreamReader(clientSocket.getInputStream()));
			 ) { 
	            out.println("Welcome to Chord Server "+serverIP+":"+portNumber); 
			    from = in.readLine();//get ip and port
			    if(from!=null &&from.split(" ")[0].equals("JOIN"))
			    { //JOIN IP + port
			    //create chord ring
				    String ip = from.split(" ")[1];
				    int port = Integer.parseInt( from.split(" ")[2]);
				    URL url = new URL("http", ip, port, "");
				    chord.joinChord(url.toString());
				    //notify all node!
				    chord.notifyAllPeers();
				    System.out.println("Add Node "+url);
			    }
			    
			    if(from!=null&&from.split(" ")[0].equals("LEAVE"))
			    { 
				    String ip = from.split(" ")[1];
				    int port = Integer.parseInt(from.split(" ")[2]);
				    URL url = new URL("http", ip, port, "");
				    chord.leaveChord(url.toString()); 
				    //notify all node!
				    chord.notifyAllPeers();
				    System.out.println("Removed Node "+url);
			    }
			    //chord.save();
	        } catch (IOException e) {
	            System.err.println("Could not listen on port " + portNumber);
	            System.exit(-1);
	        }
		}	
	}

}
