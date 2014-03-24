package Chord;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Chord implements Serializable {

	private static final long serialVersionUID = -2141636716171420906L;
	List<ChordNode> nodeList = new ArrayList<ChordNode>();
	SortedMap<ChordKey, ChordNode> sortedNodeMap = new TreeMap<ChordKey, ChordNode>();
	Object[] sortedKeyArray;
    ChordNode removed = null;
	public void createNode(String nodeId) throws ChordException {
		ChordNode node = new ChordNode(nodeId);
		nodeList.add(node);
		
		if (sortedNodeMap.get(node.getNodeKey()) != null ) {
			throw new ChordException("Duplicated Key: " + node);
		}
		
		sortedNodeMap.put(node.getNodeKey(), node);
	}

	public void joinChord(String id)
	{
		//refresh nodelist
		List<ChordNode> templist = new ArrayList<ChordNode>();
		templist.addAll(nodeList);
		nodeList.clear();
		sortedNodeMap.clear();
		sortedKeyArray = null;
		try {
			for(ChordNode cn : templist)
				createNode(cn.nodeId);
			createNode(id);
			
		} catch (ChordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 for (int i = 1; i < nodeList.size(); i++) {
				ChordNode node = getNode(i);
				node.join(getNode(0));
				ChordNode preceding = node.getSuccessor().getPredecessor();
				node.stabilize();
				if (preceding == null) {
					node.getSuccessor().stabilize();
				} else {
					preceding.stabilize();
				}
			}
		    
		    for (int i = 0; i < nodeList.size(); i++) {
				ChordNode node = getNode(i);
				node.fixFingers();
				//node.printFingerTable(System.out);
			}
	}
	
	public void leaveChord(String id)
	{
		//refresh nodelist
		List<ChordNode> templist = new ArrayList<ChordNode>();
		removed = new ChordNode(id);
		nodeList.remove(removed);
		templist.addAll(nodeList);
		templist.remove(removed);
		nodeList.clear();
		sortedNodeMap.clear();
		sortedKeyArray = null;
		try {
			for(ChordNode cn : templist)
				createNode(cn.nodeId);
			
		} catch (ChordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 for (int i = 1; i < nodeList.size(); i++) 
		 {
			ChordNode node = getNode(i);
			node.join(getNode(0));
			ChordNode preceding = node.getSuccessor().getPredecessor();
			node.stabilize();
			if (preceding == null) {
				node.getSuccessor().stabilize();
			} else {
				preceding.stabilize();
			}
		  }
		    
		for (int i = 0; i < nodeList.size(); i++) {
			ChordNode node = getNode(i);
			node.fixFingers();
			//System.out.println(node);
			//node.printFingerTable(System.out);
		}
	}
	
	public void notifyAllPeers() throws FileNotFoundException
	{
		//System.out.println("start broadcast");
		//String from=null;
		PrintStream log = System.out;
		log = new PrintStream("chordresult.log");
		nodeList.remove(removed);
		for(ChordNode node : nodeList)
		{
			if(node.equals(removed))
			{
				//System.out.println("Encounter dead nodes");
				continue;
			}
			String id = node.nodeId.substring(7,node.nodeId.length());
			String ip = id.split(":")[0];
			int port = Integer.parseInt(id.split(":")[1]);
			//System.out.println(ip+port);
			try(Socket clientSocket = new Socket(ip, port);
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
					        new InputStreamReader(clientSocket.getInputStream()));
				ObjectOutputStream oos = 
						new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));)
			{
				//transfer succ, pred and fingertable
				in.readLine();//welcome
				out.println("ChordUpdate"); //send succ and pred first;
				//System.out.println(from);
				//send successor, predecessor
				oos.writeObject(node.successor);
				//System.out.println("Send");
				oos.writeObject(node.predecessor);
				//System.out.println("Send");
				//send finger table
				oos.writeObject(node.fingerTable);
				node.printFingerTable(log);
				oos.flush();
			}catch (IOException e) {
				e.printStackTrace();
				System.err.println("Couldn't get I/O for the connection to " +ip+":"+port);
				//System.exit(1);
			}
			
			//System.out.println("Notify all");
				
		}
	}
	
	public void save()
	{
		//save object to disc
		try ( ObjectOutputStream writer = new ObjectOutputStream(
		          new FileOutputStream(System.getProperty("user.dir")+
        		          "/chord.txt")); ){
			  
			    writer.writeObject(nodeList);
			    writer.writeObject(sortedNodeMap);
			    writer.writeObject(sortedKeyArray);
			    writer.flush();
			} catch (IOException ex) {
			  // report
			}
		
	}
	//read object in mem if any
	@SuppressWarnings("unchecked")
	public void read() throws ClassNotFoundException
	{
		try (ObjectInputStream inputFile = 
				new ObjectInputStream(new FileInputStream(
						System.getProperty("user.dir")+"/chord.txt"));)// right path!!!!!!!!
		{
			nodeList = (ArrayList<ChordNode>)inputFile.readObject();
			sortedNodeMap = (TreeMap<ChordKey, ChordNode>)inputFile.readObject();
			sortedKeyArray=(Object[])inputFile.readObject();
			inputFile.close();
		} catch (IOException ex) {
			  // report
		}
	}
	public ChordNode getNode(int i) {
		return (ChordNode) nodeList.get(i);
	}

	//public int getChordSize(){ return nodeList.size();}
	
	public ChordNode getSortedNode(int i) {
		if (sortedKeyArray == null) {
			sortedKeyArray = sortedNodeMap.keySet().toArray();
		}
		return (ChordNode) sortedNodeMap.get(sortedKeyArray[i]);
	}
}
