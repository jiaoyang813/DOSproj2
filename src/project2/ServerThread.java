package project2;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import Chord.*;

public class ServerThread extends Thread {
	public ChordNode chordnode;
	public ChordKey key;
	public int listenPort;
	public String serverName;
	public String serviceType;
	//client being servicing
	public String client;
	public int clientPort;
	private Socket socket = null;
	public String IncomingServer;
    public ServerThread(Socket socket,String servername, int listenningPort) 
    		               throws MalformedURLException, UnknownHostException {
        super(socket.getLocalAddress().getHostName()+":"+listenningPort);
        listenPort = listenningPort;
        serverName = servername;
        this.socket = socket;
        URL url = new URL("http", InetAddress.getByName(serverName).getHostAddress(), 
        		              listenningPort, "");
	    chordnode = new ChordNode(url.toString());
	    //System.out.println(chordnode);
    }
    
    public void run() {
    try (
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
    ) {
        String inputLine, outputLine;
        ProcessRequest clientservice = new ProcessRequest();
        clientservice.db.setPath(System.getProperty("user.dir")+
	          "/data/"+serverName+listenPort);
        clientservice.setOutStream(out);
        clientservice.setInStream(in);
        int port = socket.getPort();
        //read chord.txt is necessary,because one server only have one chord.txt shared by
        //all the thread, if fire up a thread, it should read the chord.txt as a global variable
        boolean chordExist = new File(System.getProperty("user.dir")+
	          "/data/"+serverName+listenPort+"/chord.txt").isFile();
        if(chordExist)
        {
            try (ObjectInputStream inputFile = 
    				new ObjectInputStream(new FileInputStream(System.getProperty("user.dir")+
	        		          "/data/"+serverName+listenPort+"/chord.txt"));)// right path!!!!!!!!
    		{
    			chordnode.setSuccessor((ChordNode)inputFile.readObject());
    			chordnode.setPredecessor((ChordNode)inputFile.readObject());
    			chordnode.setFingerTable((FingerTable)inputFile.readObject());
    		} catch (IOException ex) {
    			  // report
    		}
        }
        
        //start conversation here
        out.println("Welcome");
        inputLine = in.readLine(); //register ip
        if(inputLine.equals("FILESFROMSERVER"))
        {
        	String fromPred ="";
        	String rcvfilename = null;
        	String rcvlockstatus;
        	String rcvlockowner;
        	while((fromPred = in.readLine())!=null)
        	{
        	    if(fromPred.equals("ENDTRANSFER"))
        	    	break;
        	    if(fromPred.split(" ")[0].equals("FILE"))
        	    {
        	    	
        	    	rcvfilename = fromPred.split(" ")[1];
        	    	rcvlockstatus = fromPred.split(" ")[2];
        	    	
        	    	if(rcvlockstatus.equals("ReadLock"))
        	    	{
        	    		rcvlockowner = in.readLine();
        	    		String line = "";
        	    		try {
        	 			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        	 			          new FileOutputStream(System.getProperty("user.dir")+
		 	    				          "/data/"+serverName+listenPort+"/"+rcvfilename), "utf-8"));
        	 				
        	 			   while( !line.equals("ENDFILE"))
             	    		{
           	 				 line = in.readLine();
           	 				if(line.equals("ENDFILE"))
           	 					break;
              	 				 writer.write(line);
           	 				 //System.out.println(line);
       	 			    	 writer.newLine();	
       	 			    	
             	    		}
           	 			    writer.flush();
           	 			    writer.close();
        	 			} catch (IOException ex) {
        	 			  // report
        	 			}
        	    		MetaData curState = new MetaData(rcvlockstatus);
        	    		clientservice.db.updateMetaFile(rcvfilename, curState);
        	    	    clientservice.db.updateReadLock(rcvfilename, rcvlockowner);
        	    		
        	    	}else if(rcvlockstatus.equals("WriteLock"))
        	    	{
        	    		rcvlockowner = in.readLine();
        	    		String line = "";
        	    		try {
        	 			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        	 			          new FileOutputStream(System.getProperty("user.dir")+
		 	    				          "/data/"+serverName+listenPort+"/"+rcvfilename), "utf-8"));
        	 				
        	 			   while( !line.equals("ENDFILE"))
             	    		{
           	 				 line = in.readLine();
           	 				if(line.equals("ENDFILE"))
           	 					break;
              	 				 writer.write(line);
           	 				 //System.out.println(line);
       	 			    	 writer.newLine();	
       	 			    	
             	    		}
           	 			    writer.flush();
           	 			    writer.close();
        	 			} catch (IOException ex) {
        	 			  // report
        	 			}
        	    		MetaData curState = new MetaData(rcvlockstatus);
        	    		clientservice.db.updateMetaFile(rcvfilename, curState);
        	    	    clientservice.db.updateWriteLock(rcvfilename, rcvlockowner);
        	    		
        	    	}else//no lock on file
        	    	{
        	    		System.out.println("Recv: "+ rcvfilename);
        	    		String line = "";
        	    		try {
        	 			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        	 			          new FileOutputStream(System.getProperty("user.dir")+
		 	    				          "/data/"+serverName+listenPort+"/"+rcvfilename), "utf-8"));	
        	 		
        	 			   while( !line.equals("ENDFILE"))
          	    		{
        	 				 line = in.readLine();
        	 				if(line.equals("ENDFILE"))
        	 					break;
           	 				 writer.write(line);
        	 				 //System.out.println(line);
    	 			    	 writer.newLine();	
    	 			    	
          	    		}
        	 			    writer.flush();
        	 			    writer.close();
        	 			} catch (IOException ex) {
        	 			  // report
        	 			}
        	    		MetaData curState = new MetaData(rcvlockstatus);
        	    		clientservice.db.updateMetaFile(rcvfilename, curState);
        	    	}
        	    	
        	    }
        	}
        	System.out.println("FILEs FROM PREDECESSOR");
        	//socket.close();//job done	
        }
        else if(inputLine.equals("ChordUpdate"))
        {	
        	ChordNode succ = null;
        	ChordNode pred = null;
        	Object obj = null;
        	FingerTable ftable = null;
        	try(ObjectInputStream ooi = 
        			new ObjectInputStream(socket.getInputStream());)
        	{
	        	obj = ooi.readObject();// read succ
	        	succ = (ChordNode) obj;
	        	obj = ooi.readObject();//read pred
	        	pred = (ChordNode) obj;
	        	obj = ooi.readObject(); //read ftable
	        	ftable = (FingerTable) obj;
	        	chordnode.setSuccessor(succ);
				chordnode.setPredecessor(pred);
				chordnode.setFingerTable(ftable);
				//chordnode.printFingerTable(System.out);
        	}catch(IOException ex) {
        		//ex.printStackTrace();
  		    }

        	File dir = new File(System.getProperty("user.dir")+
   		          "/data/"+serverName+listenPort);
        	if(!dir.isDirectory())
        		dir.mkdirs();
        	//write chord.txt
        	try (ObjectOutputStream writer = new ObjectOutputStream(
			          new FileOutputStream(System.getProperty("user.dir")+
	        		          "/data/"+serverName+listenPort+"/chord.txt")); )
	        {
			    writer.writeObject(succ);
			    writer.writeObject(pred);
			    writer.writeObject(ftable);
			} catch (IOException ex) {
			  // report
			}
        	
        	//load newest chordnode information.
    		try (ObjectInputStream inputFile = 
			new ObjectInputStream(new FileInputStream(System.getProperty("user.dir")+
    		          "/data/"+serverName+listenPort+"/chord.txt"));)// right path!!!!!!!!
    		{
    			chordnode.setSuccessor((ChordNode)inputFile.readObject());
    			chordnode.setPredecessor((ChordNode)inputFile.readObject());
    			chordnode.setFingerTable((FingerTable)inputFile.readObject());
    		} catch (IOException ex) {
    			  // report
    		}
        	//System.out.println("Update from ChordServer!");
        	socket.close();//job done
        }
        else{
	        outputLine = clientservice.processInputline(inputLine.split(" "));
	        out.println(outputLine +" "+ port); // welcome client ip port
	        System.out.println(clientservice.client +":"+port+" "+inputLine);
	        //while loop communication
	        boolean running = true;
	        while (running) {
	        inputLine = in.readLine();
	    	String type = inputLine.split(" ")[0];
	    	if(type.equals("READ")||type.equals("READLOCK")||
	    			type.equals("WRITE") || type.equals("WRITELOCK")||
	    			type.equals("RELEASELOCK")||type.equals("DELETE")||
	    			type.equals("UPLOAD"))
	    		serviceType = "FILELOOKUP";
	    	else 
	    		serviceType = "LOCALSERVICE";
	    	
	        switch(serviceType)
	        {
	        	case "FILELOOKUP":
	        		//make file key
	        		//System.out.println("File Lookup Operation");
	        		//System.out.println(inputLine);
	        		String file = inputLine.split(" ")[1];
	        		ChordKey fileKey = new ChordKey(file);
	        		FingerTable ftable = chordnode.getFingerTable();
	        		Finger f1,f2;
	        		ChordNode targetnode = null;
	        		//local lookup
	        		//System.out.println(chordnode);
	        		ChordKey predkey = null;
	        		if(chordnode.getPredecessor() == null)
	        		    predkey = chordnode.getNodeKey();
	        		else 
	        			predkey = chordnode.getPredecessor().getNodeKey();
                    //check if file belongs to this node
	        		//if so, do a local lookup operation
	        		if(fileKey.isBetween(predkey,chordnode.getNodeKey())
	        				|| predkey.compareTo(chordnode.getNodeKey()) == 0)
	        		{
	        			// deal with upload later
	        			//System.out.println("Local LookUp");
	        			outputLine = clientservice.processInputline(inputLine.split(" "));
	        			System.out.println(clientservice.client +":"+port+" "+inputLine);
	            		out.println(outputLine);
	        		}
	        		else
	        		{
	        			//file is not on local, go to other node for help
	            		//first check ftable to find node responsible for this filekey
	        			//System.out.println("Remote LookUp");
	            		for(int i = 0; i < Hash.KEY_LENGTH ; i++)
	            		{
	            			f1 = ftable.getFinger(i);
	            			if( i == 31 )
	            				f2 = ftable.getFinger(0);
	            			else
	            				f2 = ftable.getFinger(i+1);
	            			if(fileKey.isBetween(f1.getStart(),f2.getStart()))
	            			{
	            				targetnode = f1.getNode();			
	            				break;
	            			}    		
	            		}
	            	System.out.println("target node:"+targetnode);
            		//find or not , ask client go to the node for help   
        			String id = targetnode.getNodeId().substring(7,targetnode.getNodeId().length());
        			String nodeIP = id.split(":")[0];
        			int nodePort = Integer.parseInt(id.split(":")[1]);
        			//goto ip port
        			out.println("GOTO " + nodeIP+" "+nodePort );
        			running = false;
        			//socket.close();//job done!	
        			break;
	        		}	
	        	break;
	        	case "LOCALSERVICE":
	        	{
	        		//System.out.println("Local Operation");
	        		if (inputLine.equals("BYE"))
	                {
	                	System.out.println("");
	                	out.println(this.getName() +":BYE");
	                	running = false;
	                	System.out.println("Server is listenning on port "+listenPort);	
	                    //break;
	                } 
	        		
	        		if(inputLine.equals("SHUTDOWN SERVER"))
	        		{
	        			outputLine = clientservice.processInputline(inputLine.split(" "));
	        			System.out.println(clientservice.client +":"+port+" "+inputLine);
	        			//out.println(outputLine);	
	        			//transfer file to its successor
		        		ChordNode successor;
		        		
		        		if(chordnode.getSuccessor() != null && chordnode.getPredecessor() != null
		        				&&!chordnode.getSuccessor().equals(chordnode))
		        		{
		        			successor = chordnode.getSuccessor();
		        			String id = successor.getNodeId().substring(7,successor.getNodeId().length());
		        			String succIP = id.split(":")[0];
		        			int succPort = Integer.parseInt(id.split(":")[1]);
		        			System.out.println("Succ "+succIP + succPort);
		        			out.println("GOTO " + succIP+" "+succPort );	
		        			try(Socket clientSocket = new Socket(succIP, succPort);
		        					PrintWriter sout = new PrintWriter(clientSocket.getOutputStream(), true);
		        					BufferedReader sin = new BufferedReader(
		        						        new InputStreamReader(clientSocket.getInputStream()));)
		        			{
	        					//transfer file to my successor
	        					sin.readLine();//welcome
	        					//notify it is an inter-server file transfer
	        					sout.println("FILESFROMSERVER"); 
	        					//retrieve files and locks from db
	        					HashMap<String, MetaData> files = 
	        							clientservice.db.metafile;
	        					HashMap<String, ArrayList<String>> rlock =
	        							clientservice.db.rmap;
	        					HashMap<String,String> wlock =
	        							clientservice.db.wmap; 
	        					Iterator<Entry<String, MetaData>> it = files.entrySet().iterator();
	        					while(it.hasNext())
	        				    {
	        				    	 Entry<String, MetaData> thisEntry = (Entry<String, MetaData>) it.next();
	        				    	 MetaData record = (MetaData)thisEntry.getValue();
	        				    	 String fname = thisEntry.getKey();
	        				    	 String lockstatus = record.lock;
	        				    	 String lockowner = "";
	        				    	 if(lockstatus.equals("ReadLock"))
        				    		 {
        				    			 ArrayList<String> owners = rlock.get(fname);
        				    			 for(String owner : owners)
        				    			 {
        				    				 lockowner  = lockowner + " "+owner;
        				    			 }
        				    			 
        				    		 }else if(lockstatus.equals("WriteLock"))
        				    		 {
        				    			 lockowner = wlock.get(fname);
        				    		 }
        				    		 
	        				    	 //if the file are not system info files, transfer it to successor
	        				    	 if(!fname.equals("ReadLock.txt")&&!fname.equals("WriteLock.txt")&&
	        				    			!fname.equals("chord.txt")&&!fname.equals("metafile.txt")&&
	        				    			!fname.equals(".DS_Store"))
	        				    	 {
	        				    		 System.out.println("SEND FILE "+fname);
	        				    		 if(lockstatus.equals("ReadLock"))
	        				    		 {
	        				    			 sout.println("FILE "+fname + " ReadLock ");
	        				    			 sout.println(lockowner);
	        				    			 //send files
	        				    			 try (BufferedReader inputFile = 
	        				 	    				new BufferedReader(new FileReader(System.getProperty("user.dir")+
	        				 	    				          "/data/"+serverName+listenPort+"/"+fname)))// right path!!!!!!!!
	        				 	    		{
	        				 	    			String line;
	        				 	    			while ((line = inputFile.readLine()) != null) {
	        				 	    				sout.println(line);
	        				 	    			}
	        				 	    			sout.println("ENDFILE");
	        				 	    			//in.readLine();
	        				 	    			inputFile.close();
	        				 	    		} catch (IOException ex) {
	        				 	    			  // report
	        				 	    			}
	        				    			 
	        				    		 }else if(lockstatus.equals("WriteLock"))
	        				    		 {
	        				    			 sout.println("FILE "+fname + " WriteLock");
	        				    			 sout.println(lockowner);
	        				    			 try (BufferedReader inputFile = 
		        				 	    				new BufferedReader(new FileReader(System.getProperty("user.dir")+
		        				 	    				          "/data/"+serverName+listenPort+"/"+fname)))// right path!!!!!!!!
		        				 	    		{
		        				 	    			String line;
		        				 	    			while ((line = inputFile.readLine()) != null) {
		        				 	    				sout.println(line);
		        				 	    			}
		        				 	    			sout.println("ENDFILE");
		        				 	    			//in.readLine();
		        				 	    			inputFile.close();
		        				 	    		} catch (IOException ex) {
		        				 	    			  // report
		        				 	    			}
	        				    			 
	        				    		 }
	        				    		 else // no lock on file
	        				    		 {
	        				    			 sout.println("FILE "+fname + " NULL");
	        				    			 try (BufferedReader inputFile = 
		        				 	    				new BufferedReader(new FileReader(System.getProperty("user.dir")+
		        				 	    				          "/data/"+serverName+listenPort+"/"+fname)))// right path!!!!!!!!
		        				 	    		{
		        				 	    			String line;
		        				 	    			while ((line = inputFile.readLine()) != null) {
		        				 	    				sout.println(line);
		        				 	    				//System.out.println(line);
		        				 	    			}
		        				 	    			sout.println("ENDFILE");
		        				 	    			//in.readLine();
		        				 	    			inputFile.close();
		        				 	    		} catch (IOException ex) {
		        				 	    			  // report
		        				 	    	    }
	        				    		 }
	        				    			
	        				    	 }
	        				    	
	        				    	 
	        				    }
	        					 sout.println("ENDTRANSFER");
	        					 System.out.println("SEND ALL FILE");
	        				}catch (IOException e) {
	        					e.printStackTrace();
	        					System.err.println("Couldn't get I/O for the connection to "
	        					           +succIP+":"+succPort);
	        					System.exit(1);
	        				}
		        		}
		        		else
		        		{
		        			System.out.println("I AM THE LAST NODE");
		        			out.println(outputLine);	
		        		}
		        		
		        		System.out.println("Notify chord server");
		        		//notify chord server : I leave now!
		        		try (Socket chordSocket = new Socket("localhost", 40000); //change later
		        			    PrintWriter cout = new PrintWriter(chordSocket.getOutputStream(), true);
		        			    BufferedReader cin = new BufferedReader(
		        			        new InputStreamReader(chordSocket.getInputStream()));)
		        		{
		                    
		                    System.out.println("ChordServer:"+cin.readLine());
		                    cout.println("LEAVE "+InetAddress.getByName(serverName).getHostAddress()
		                    		     +" "+listenPort);//send ip and port to chord
		                    
		        		}catch (IOException e) {
		                        System.err.println("Could not connect ChordServer");
		                        System.exit(-1);
		                }
	        			
	        			System.exit(1);
	        		}
	        		outputLine = clientservice.processInputline(inputLine.split(" "));
	        		System.out.println(clientservice.client +":"+port+" "+inputLine);
	        		out.println(outputLine);	
	        	break;
	        	}
	        	
	        	default:
	        		break;
	         }      
	       }
      }
     socket.close();
    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace(); 
    }

  }
     
}
