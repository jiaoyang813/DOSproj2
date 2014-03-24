package Chord;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;


public class test {
	public static final String HASH_FUNCTION = "CRC32";

	public static final int KEY_LENGTH = 32;

	public static final int NUM_OF_NODES = 10;
	
	public static void main(String[] args) throws UnknownHostException, 
	MalformedURLException, FileNotFoundException {
		String host = InetAddress.getLocalHost().getHostAddress();
		int port = 50000;
		PrintStream out = System.out;
		out = new PrintStream("testchord.log");
		
		Chord chord = new Chord();
		for (int i = 0; i < NUM_OF_NODES; i++) {
			URL url = new URL("http", host, port + i, "");
			try {
				chord.createNode(url.toString());
			} catch (ChordException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		out.println(NUM_OF_NODES + " nodes are created.");

		for (int i = 0; i < NUM_OF_NODES; i++) {
			ChordNode node = chord.getSortedNode(i);
			out.println(node);
		}

		for (int i = 1; i < NUM_OF_NODES; i++) {
			ChordNode node = chord.getNode(i);
			node.join(chord.getNode(0));
			ChordNode preceding = node.getSuccessor().getPredecessor();
			node.stabilize();
			if (preceding == null) {
				node.getSuccessor().stabilize();
			} else {
				preceding.stabilize();
			}
		}
		out.println("Chord ring is established.");

		for (int i = 0; i < NUM_OF_NODES; i++) {
			ChordNode node = chord.getNode(i);
			node.fixFingers();
		}
		out.println("Finger Tables are fixed.");

		for (int i = 0; i < NUM_OF_NODES; i++) {
			ChordNode node = chord.getSortedNode(i);
			node.printFingerTable(out);
		}
		URL url = new URL("http","192.168.1.5", 50000, "");
		chord.leaveChord(url.toString());
		ChordNode chordnode = chord.getSortedNode(2);//let 50001 find C.txt
		System.out.println("50000 leaves");
		out.println("50000 leave us");
		for (int i = 0; i < NUM_OF_NODES; i++) {
			ChordNode node = chord.getSortedNode(i);
			node.printFingerTable(out);
		}
		FingerTable ftable = chordnode.getFingerTable();
		Finger f1,f2;
		ChordNode targetnode = null;
		ChordNode a = new ChordNode("A.txt");
        ChordNode b = new ChordNode("B.txt");
        ChordNode c = new ChordNode("C.txt");
        ChordNode d = new ChordNode("D.txt");
        ChordNode e = new ChordNode("E.txt"); 
        ChordNode f = new ChordNode("major.txt"); 
        ChordKey fileKey = f.getNodeKey();
        ChordKey predkey = chordnode.getPredecessor().getNodeKey();
        
        
        if(fileKey.isBetween(predkey,chordnode.getNodeKey()))
		{
			// deal with upload later
        	System.out.println(predkey);
        	System.out.println(fileKey);
        	System.out.println(chordnode.getNodeKey());
			System.out.println("Local LookUp");
		}
		else
		{
			//file is not on local, go to other node for help
    		//first check ftable to find node responsible for this filekey
			System.out.println("Remote LookUp");
    		boolean find = false;
    		for(int i = 0; i < Hash.KEY_LENGTH ; i++)
    		{
    			f1 = ftable.getFinger(i);
    			if( i == 31 )
    				f2 = ftable.getFinger(0);
    			else
    				f2 = ftable.getFinger(i+1);
    			
    			if(fileKey.isBetween(f1.getStart(),f2.getStart()))
    			{
    				System.out.println("Between");
    				System.out.println(f1.getStart());
    	        	System.out.println(fileKey);
    	        	System.out.println(f2.getStart());	
					targetnode = f1.getNode();
					System.out.println("target node:"+targetnode);
    				break;
    			}    		
    		}
    		//System.out.println("end target node:"+targetnode);
		}
    	
      
        
	}

}
