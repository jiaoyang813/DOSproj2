package project2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

public class client {
	public static void main(String[] args) throws IOException
	{
	System.out.println("Client Started");
	int listenPort = 20000;//default client's listening port
	String serverName = null;// server to connect
	int portNumber = -1;// server's listening port
    boolean isClientRunning = true;
    String userInput = null; //init input
    String fromUser= ""; // communicate input
    String requestLast = ""; //store last fromUser
    String fromServer = null;
    BufferedReader stdIn =
            new BufferedReader(new InputStreamReader(System.in));
    String clientIp = Inet4Address.getLocalHost().getHostAddress();
    while(isClientRunning)
    {
    	if(portNumber < 0)
    	{
	    	System.out.println("Usage: [ServerName] [Port] to connect");
	    	System.out.println("Or Enter SHUTDOWN to Terminate:");
	    	System.out.print(">>");
		    userInput = stdIn.readLine();
		    String[] token = userInput.split(" ");
		    
		    if(token.length == 2)
		    {
		    	if(token[0] != null&& Integer.parseInt(token[1]) >0)
		    	{
			    	serverName = token[0];
			    	portNumber = Integer.parseInt(token[1]);
			    	//System.out.println("send request: "+request);
		    	}
		    }
		    else if(token.length == 1)
		    {
		    	if(token[0].equals("SHUTDOWN"))
		    	{
		    		System.out.println("CLIENT SHUTDOWN");
		    		stdIn.close();
		    		isClientRunning = false;
		    		System.exit(1);
		    	}
		    	else
		    	{	
		    		System.err.println("Something Weird");
		    		System.exit(0);
		    	}
		    		
		    }else
		    {
		    	System.err.println("Something Weird");
		    	System.exit(0);
		    }
    	}
	    //System.out.println("Enter Listenning Port: ");
	    //System.out.print(">>");
	    //listenPort =Integer.parseInt( stdIn.readLine());
	    
		try{ 
		Socket clientSocket = new Socket(serverName, portNumber);
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
		        new InputStreamReader(clientSocket.getInputStream()));
	    
	    fromServer = in.readLine();
	   // System.out.println(serverName+":"+portNumber+" "+fromServer);
	    out.println("REGISTER "+ clientIp+" "+listenPort);
        while ((fromServer = in.readLine()) != null) {
    	String request = requestLast.split(" ")[0];
        if(request.equals("READ")||request.equals("READLOCK")||
    			request.equals("WRITE") || request.equals("WRITELOCK")||
    			request.equals("RELEASELOCK")||request.equals("DELETE")||
    			request.equals("UPLOAD"))
        {
        	out.println(requestLast);
        	requestLast = "";
        }
        else{
	        if (fromUser.equals("BYE"))
	        {
	    		out.println(fromUser);
	    		//reset server port
	    		fromUser = "";
	    		portNumber = -1;
	    		serverName = null;
	        	break;
	        }

	        if(fromServer.equals("SERVER SHUTDOWN"))
	        {
	        	System.out.println(serverName+":"+portNumber + " "+fromServer);
	        	fromUser = "";
	        	portNumber = -1;
	        	serverName = null;
	        	break;
	        }
	        if(fromServer.equals("STARTUPLOAD"))
	        {
	        	String FileName = fromUser.split(" ")[1];
	        	try (BufferedReader inputFile = 
	    				new BufferedReader(new FileReader(System.getProperty("user.dir")+"/file/"+FileName)))// right path!!!!!!!!
	    		{
	    			String inputLine;
	    			while ((inputLine = inputFile.readLine()) != null) {
	    				out.println(inputLine);
	    			}
	    			out.println("ENDFILE");
	    			fromServer = in.readLine();
	    			inputFile.close();
	    		} catch (IOException ex) {
	    			  // report
	    			}
	        }
	        if(fromServer.equals("SENDDIR"))// send directory
	    	{
	        	String dir = "";
	    		while(!(dir = in.readLine()).equals("ENDDIR"))
	    		{
	    			System.out.println(dir);
	    		}
	    	}
	        
	        if(fromServer.equals("SENDFILE"))// send directory
	    	{
	        	String FileName = fromUser.split(" ")[1];
	        	String line = "";
	        	System.out.println("---Start of "+FileName);
	        	try {
	 			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
	 			          new FileOutputStream(System.getProperty("user.dir")+"/"+FileName), "utf-8"));
	 				//write it to disc 
	 			   while(!line.equals("ENDFILE")
		    				&&!line.equals("NOFILE"))
		    		{
	 				    line = in.readLine();
	 				    if(line.equals("ENDFILE")
			    				||line.equals("NOFILE"))
	 				    	break;
		    			System.out.println(line);
		    			writer.write(line);
		    			writer.newLine();	
		    		}
	 			System.out.println("---End of "+FileName);
	 			writer.close();
	 			} catch (IOException ex) {
	 			  // report
	 			}
	    		
	    	}
	        
	        
	        String[] serverinfo = fromServer.split(" ");
	    	if(serverinfo[0].equals("GOTO"))
	    	{
	    		out.println("BYE");//disconnect from current server
	    		//reset server and port
	    		serverName = serverinfo[1];
	    		portNumber = Integer.parseInt(serverinfo[2]);
	    		System.out.println("goto "+serverName+" "+portNumber);
	    		requestLast = fromUser;// store last user input
	    		break;
	    	}
	    	else
	    	{
	    	    System.out.println(serverName+":"+portNumber + " "+fromServer);
	        	System.out.print(">>");
	            fromUser = stdIn.readLine();
	            if (fromUser != null) {
	            	out.println(fromUser);
	                //System.out.println("Client: " + fromUser);
	            }else{
	            	System.out.println("NULL COMMAND");
	            	out.println("NULL CMD");
	            }
	        
	    	  }
	       }
        }
        clientSocket.close();
	   } catch (UnknownHostException e) {
		    System.err.println("Don't know about host " +serverName);
		    portNumber = -1;
		  //  System.exit(1);
	   } catch (IOException e) {
		    System.err.println("Couldn't get I/O for the connection to " +
		        serverName +":"+ portNumber);
		  portNumber = -1;
		}
    
	}
    

 }

}
