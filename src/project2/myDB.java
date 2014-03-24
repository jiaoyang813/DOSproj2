package project2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

//implement a small database to load files on disc
// and processing simple query
public class myDB {
	private static myDB db = null;
	HashMap<String, MetaData> metafile; 
	HashMap<String, Tuple> curFile;
	HashMap<String, ArrayList<String>> rmap;
	HashMap<String,String> wmap;
	String curFileName = null;
	String path;
	//singleton pattern
	public static myDB getInstance()
	{
		db = new myDB();
		return db;
	}
	private myDB()
	{
		//init myDB, create metafile in memory
		metafile = new HashMap<String, MetaData>();
		rmap = new HashMap<String, ArrayList<String>>();
		wmap = new HashMap<String, String>();
	}
	
	public void setPath(String syspath)
	{
		path = syspath;
		loadMetaFile();
		loadLockFile();
	}
	

	public void uploadFile(String FileName,BufferedReader in)
	{
		try {
			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(path+"/"+FileName), "utf-8"));
				//write it to disc
			    String line ="NONE";
			    System.out.println("---Start of "+FileName);
			    while(!line.equals("ENDFILE"))
			    	
			    {    line = in.readLine();
			         if(line.equals("ENDFILE"))
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
	public boolean checkReadLock(String FileName, String client)
	{
		if(rmap == null || rmap.get(FileName)== null)
			return false;
		if(rmap.get(FileName).contains(client))
			return true;
		else 
			return false;
	}
	public boolean checkWriteLock(String FileName, String client)
	{
		if(wmap == null || wmap.get(FileName) == null)
			return false;
		if(wmap.get(FileName).equals(client))
			return true;
		else 
			return false;
	}
	
	public boolean deleteFile(String FileName)
	{
		boolean fileExist = new File(path+"/"+FileName).isFile();
		if(!fileExist)
			return false;
		else
		{
			try{ 
	    		File file = new File(path+"/"+FileName);
	    		if(metafile.get(FileName).lock.equals("NULL")&&file.delete()){
	    			metafile.remove(FileName);
	    			writeMetaFileToDisc();
	    			return true;
	    		}else{
	    			return false;
	    		}
	 
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
	    }
		return false;
	}
	
	public boolean getReadLock(String FileName, String client)
	{
		if(metafile.containsKey(FileName))
		{
			MetaData temp = metafile.get(FileName);
			if(!temp.lock.equals("WriteLock"))
			{
				if(temp.lock.equals("ReadLock")){
					ArrayList<String> arrtemp = rmap.get(FileName);
					if(!arrtemp.contains(client))
					{	
						arrtemp.add(client);
					    writeReadLockFileToDisc();
					}
					return true;
				}
				else{ // no lock on file
					temp.lock = "ReadLock";
					ArrayList<String> arrtemp = new ArrayList<String>();
					arrtemp.add(client);
					rmap.put(FileName, arrtemp);
					writeReadLockFileToDisc();
					updateMetaFile(FileName, temp);
					return true;
				}
			}
			else //has write lock on file
				return false;
		}
		// no such file
		return false;
	}
	
	public boolean getWriteLock(String FileName, String client)
	{
		if(metafile.containsKey(FileName))
		{
			MetaData temp = metafile.get(FileName);
			if(temp.lock.equals("ReadLock"))
			{
					return false;
			}
			else if(temp.lock.equals("WriteLock"))
			{
				if(wmap.containsKey(client))
					return true;
				else 
					return false;
			}
			else if(temp.lock.equals("NULL"))
			{	
				temp.lock = "WriteLock";
				wmap.put(FileName, client);
			    writeWriteLockFileToDisc(); 
				updateMetaFile(FileName, temp);
				return true;
			}
			else
				return false;
			
		}
		// no such file
		return false;
	}
	
	//release all lock of filename by client
	public boolean releaseLock(String FileName, String client)
	{
		if(metafile.containsKey(FileName))
		{
			MetaData temp = metafile.get(FileName);
			if(!temp.equals("NULL"))
			{
				if(temp.lock.equals("WriteLock"))
				{
					if(client.equals(wmap.get(FileName)))
					{
						//the right client 
						temp.lock = "NULL";
						wmap.remove(FileName);
						updateMetaFile(FileName, temp);
						writeWriteLockFileToDisc();
						return true;
					}
					else // not the right client
						return false;
				}   
				else if(temp.lock.equals("ReadLock"))
				{
					ArrayList<String> arrtemp = rmap.get(FileName);
					if(arrtemp.contains(client))
					{   if(arrtemp.size() == 1) 
						{	
							rmap.remove(FileName);
							temp.lock = "NULL";
						}
						else
						{
						    arrtemp.remove(client);
						    rmap.put(FileName, arrtemp);
						}
						writeReadLockFileToDisc();
						updateMetaFile(FileName, temp);
					}
					else // no read lock from this client
						return false;
					
				}
				
			}
			// no lock on file
			return true;	
		}
		// no such file
		return false;    
	}
	
	public void releaseAllLock()
	{
		rmap.clear();
		wmap.clear();
		writeReadLockFileToDisc();
		writeWriteLockFileToDisc();
		try {
			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(path+"/metafile.txt"), "utf-8"));
				//write it to disc
			    Iterator<Entry<String, MetaData>> it = metafile.entrySet().iterator();
			    while(it.hasNext())
			    {
			    	 Entry<String, MetaData> thisEntry = (Entry<String, MetaData>) it.next();
			    	 MetaData metadata = (MetaData)thisEntry.getValue();
			    	 String outputLine;
			    	 metadata.lock = "NULL";
			    	 outputLine = thisEntry.getKey()+" "+metadata.lock+" "+ metadata.lastModifyTime;
			    	 writer.write(outputLine);
			    	 //may add a new line at file bottom 
			    	 writer.newLine();	 
			    }
			    writer.close();
			} catch (IOException ex) {
			  // report
			}
	}
	
	//remove old metadata and put new one
	public void updateMetaFile(String FileName, MetaData curState)
	{
		if(metafile == null)
			return;
		Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.DD-HH:mm:ss");
    	curState.lastModifyTime = sdf.format(cal.getTime()) ;
		metafile.put(FileName, curState);
		writeMetaFileToDisc();
		readMetaFileFromDisc();
	}
	
	public void updateWriteLock(String FileName, String lockowner)
	{
		if(wmap == null)
			return;
		wmap.put(FileName, lockowner);
		writeWriteLockFileToDisc();
		getWriteLockFileFromDisc();
	}
	
	public void updateReadLock(String FileName, String owners)
	{
		if(rmap == null)
			return;
	    String[] lockowners = owners.split(" ");
	    ArrayList<String> temp;
	    if(rmap.containsKey(FileName))
	    	temp = rmap.get(FileName);
	    else
	    	temp = new ArrayList<String>();
	    
	    for(int i = 0; i < lockowners.length; i++)
	    {
	    	temp.add(lockowners[i]);
	    }
	    
	    rmap.put(FileName, temp);
	    writeReadLockFileToDisc();
	    getReadLockFileFromDisc();
	}
	
	public void loadLockFile()
	{
		boolean readlockfileExist = new File(path+"/ReadLock.txt").isFile();
		boolean writelockfileExist = new File(path+"/WriteLock.txt").isFile();
		if(readlockfileExist)
		{
			getReadLockFileFromDisc();
		}
		else
			createReadLockFile();
		
		if(writelockfileExist)
			getWriteLockFileFromDisc();
		else
			createWriteLockFile();		
	}

	public void loadMetaFile()
	{
		//check metafile exist
		boolean metafileExist = new File(path+"/metafile.txt").isFile();
		if(metafileExist)
		{  //if it exists, read it in
			readMetaFileFromDisc();
			//update the metafile if new file is added
			ArrayList<String> curDir = getDirFiles();
			for(String s: curDir)
			{
				if(!metafile.containsKey(s))
				{
					Calendar cal = Calendar.getInstance();
			    	cal.getTime();
			    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.DD-HH:mm:ss");
			        MetaData temp = new MetaData("NULL",sdf.format(cal.getTime()) );
			        metafile.put(s, temp);
				}
			}
			writeMetaFileToDisc();
		}
		else{
		//create metafile and read it in
			createMetaFile();
		}
	}
	
	//load FileName from disc
	public boolean loadFile(String FileName)
	{
		//read file from disc
		curFileName = FileName;
		boolean isfileExist = new File(path+"/"+FileName).isFile();
		if(isfileExist)
		{
			curFile = new HashMap<String, Tuple>();
			try (BufferedReader inputFile = 
					new BufferedReader(new FileReader(path+"/"+FileName)))// right path!!!!!!!!
			{
				String inputLine;
				while ((inputLine = inputFile.readLine()) != null) {
					
					String[] token = parseUserInput(inputLine);
					Tuple record = new Tuple(token[0], token[1], token[2]);
					curFile.put(token[0], record);
				}
			inputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			return true;
		}
		else
			return false;
	}
	//save dbfile to disc
	public void saveCurFile(String FileName)
	{	
	//save curFile to disc	
		try {
		   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(path+"/"+FileName), "utf-8"));
		    Iterator<Entry<String, Tuple>> it = curFile.entrySet().iterator();
		    while(it.hasNext())
		    {
		    	 Entry<String, Tuple> thisEntry = (Entry<String, Tuple>) it.next();
		    	 Tuple record = (Tuple)thisEntry.getValue();
		    	 String outputLine;
		    	 outputLine = record.elem1+" "+record.elem2+" "+ record.elem3;
		    	 writer.write(outputLine);
		    	 //may add a new line at file bottom 
		    	 writer.newLine();	 
		    } 
		curFileName = null;
		curFile.clear();
		writer.close();
		} catch (IOException ex) {
		  // report
		}
		MetaData temp;
		Calendar cal = Calendar.getInstance();
    	cal.getTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.DD-HH:mm:ss");
    	if(rmap.containsKey(FileName))
            temp = new MetaData( "ReadLock",sdf.format(cal.getTime()) );
    	else if(wmap.containsKey(FileName))
    		temp = new MetaData( "WriteLock",sdf.format(cal.getTime()) );
    	else
    		temp = new MetaData( "NULL",sdf.format(cal.getTime()) );
        db.updateMetaFile(FileName, temp);	
	}
	
	public void printDir(PrintWriter out)
	{
		out.println("SENDDIR");//send directory
		out.println("---START OF DIRECTORY---");
		Iterator<Entry<String, MetaData>> it = metafile.entrySet().iterator();
		while(it.hasNext())
	    {
	    	 Entry<String, MetaData> thisEntry = (Entry<String, MetaData>) it.next();
	    	 MetaData record = (MetaData)thisEntry.getValue();
	    	 out.println(thisEntry.getKey()+" "+record.toString());
	    }
		out.println("---END OF DIRECTORY---");
		//print result
	}
	
	public void printCurFile(PrintWriter out)
	{
		if(curFileName!=null)
		{
			out.println("SENDFILE");
			Iterator<Entry<String, Tuple>> it = curFile.entrySet().iterator();
			while(it.hasNext())
		    {
		    	 Entry<String, Tuple> thisEntry = (Entry<String, Tuple>) it.next();
		    	 Tuple record = (Tuple)thisEntry.getValue();
		    	 out.println(record.toString());
		    }
		}
		else
		{
			out.println("NO FILE");
		}
		
	}
	
	public Tuple search(Tuple t)
	{
		//search
		String key = t.elem1;
		if(curFile.containsKey(key))
		{
			if(curFile.get(key).isEqual(t))
				return curFile.get(key);
			else return null;
		}
		else
			return null;
		
	}
	
	public boolean delete(Tuple t)
	{
		//delete tuples
		if(search(t) != null)
		{
			curFile.remove(t.elem1);
			return true;
		}
		
		return false;
	
	}
	
	public boolean insert(Tuple t)
	{
		//insert tuples
		//check if item conflicts
		if(curFile.containsKey(t.elem1))
			return false;
		else
			curFile.put(t.elem1, t);
		return true;
		//dbfile on disc is OUT OF DATE now
	}
	
	public void closeDB()
	{
		if(curFileName != null)
			saveCurFile(curFileName);
		writeMetaFileToDisc();
		writeWriteLockFileToDisc();
		writeReadLockFileToDisc();
	}
	
	public static String[] parseUserInput(String in)
	{
		String[] result=null;
		String delim=" ";
		result = in.split(delim);
		return result;
	}
	
	public void createMetaFile()
	{
		try {
			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(path+"/metafile.txt"), "utf-8"));
				ArrayList<String> curDir = getDirFiles();
				//load it in memory
				metafile.clear();
				for(String s : curDir)
				{
					Calendar cal = Calendar.getInstance();
			    	cal.getTime();
			    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.DD-HH:mm:ss");
			        MetaData temp = new MetaData("NULL",sdf.format(cal.getTime()) );
			        metafile.put(s, temp);
				}
				//write it to disc, keep it up to date
			    Iterator<Entry<String, MetaData>> it = metafile.entrySet().iterator();
			    while(it.hasNext())
			    {
			    	 Entry<String, MetaData> thisEntry = (Entry<String, MetaData>) it.next();
			    	 MetaData metadata = (MetaData)thisEntry.getValue();
			    	 String outputLine;
			    	 outputLine = thisEntry.getKey()+" "+metadata.lock+" "+ metadata.lastModifyTime;
			    	 writer.write(outputLine);
			    	 //may add a new line at file bottom 
			    	 writer.newLine();	 
			    }
			    writer.close();
			} catch (IOException ex) {
			  // report
			}
	}
	
	public void createReadLockFile()
	{
		try {
			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(path+"/ReadLock.txt"), "utf-8"));
			    writer.close();
			} catch (IOException ex) {
			  // report
			}
	}
	
	public void createWriteLockFile()
	{
		try {
			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(path+"/WriteLock.txt"), "utf-8"));
			    writer.close();
			} catch (IOException ex) {
			  // report
			}
	}
	
	public ArrayList<String> getDirFiles()
	{
		ArrayList<String> dir = new ArrayList<String>();
		File folder = new File(path+"/");
		File[] listOfFiles = folder.listFiles();
	// save it in metafile in memory
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        dir.add(file.getName());
		    }
		}
		return dir;
	}
	
	//read metafile.txt to memory 
	public void readMetaFileFromDisc()
	{
		metafile.clear();//clear old data
		try (BufferedReader inputFile = 
				new BufferedReader(new FileReader(path+"/metafile.txt")))// right path!!!!!!!!
		{
			String inputLine;
			while ((inputLine = inputFile.readLine()) != null) {
				String[] token = parseUserInput(inputLine);
				MetaData temp = new MetaData(token[1],token[2]);
				metafile.put(token[0], temp);
			}
			
			inputFile.close();
		} catch (IOException ex) {
			  // report
			}
		
	
	}
	
	public void getReadLockFileFromDisc()
	{
		try (BufferedReader inputFile = 
				new BufferedReader(new FileReader(path+"/ReadLock.txt")))// right path!!!!!!!!
		{
			String inputLine;
			while ((inputLine = inputFile.readLine()) != null) {
				String[] token = parseUserInput(inputLine);
				for(int i = 1; i < token.length; i++)
				{
					if(rmap.containsKey(token[0]))
					{
						ArrayList<String> temp = rmap.get(token[0]);
						temp.add(token[i]);
						rmap.put(token[0], temp);
					}
					else
					{
						ArrayList<String> temp = new ArrayList<String>();
						temp.add(token[i]);
						rmap.put(token[0], temp);
					}
				}
			}
			
			inputFile.close();
		} catch (IOException ex) {
			  // report
			}
	}
	
	public void getWriteLockFileFromDisc()
	{
		wmap.clear();
		try (BufferedReader inputFile = 
				new BufferedReader(new FileReader(path+"/WriteLock.txt")))// right path!!!!!!!!
		{
			String inputLine;
			while ((inputLine = inputFile.readLine()) != null) {
				String[] token = parseUserInput(inputLine);
						wmap.put(token[0], token[1]);
			}
			
			inputFile.close();
		} catch (IOException ex) {
			  // report
			}
	}
	
	//write the in-memory metafile to disc
	public void writeMetaFileToDisc()
	{
		try {
			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(path+"/metafile.txt"), "utf-8"));
				//write it to disc
			    Iterator<Entry<String, MetaData>> it = metafile.entrySet().iterator();
			    while(it.hasNext())
			    {
			    	 Entry<String, MetaData> thisEntry = (Entry<String, MetaData>) it.next();
			    	 MetaData metadata = (MetaData)thisEntry.getValue();
			    	 String outputLine;
			    	 outputLine = thisEntry.getKey()+" "+metadata.lock+" "+ metadata.lastModifyTime;
			    	 writer.write(outputLine);
			    	 //may add a new line at file bottom 
			    	 writer.newLine();	 
			    }
			    writer.close();
			} catch (IOException ex) {
			  // report
			}
		
	}
	
	public void writeReadLockFileToDisc()
	{
		try {
			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(path+"/ReadLock.txt"), "utf-8"));
				//write it to disc
			    Iterator<Entry<String, ArrayList<String>>> it = rmap.entrySet().iterator();
			    while(it.hasNext())
			    {
			    	Entry<String, ArrayList<String>> thisEntry = (Entry<String, ArrayList<String>>) it.next();
			    	 String outputLine;
			    	 ArrayList<String> temp = thisEntry.getValue();
			    	 outputLine = thisEntry.getKey()+" ";
			    	 for(String s : temp)
			    		 outputLine += s+" ";
			    	 writer.write(outputLine);
			    	 //may add a new line at file bottom 
			    	 writer.newLine();	 
			    }
			    writer.close();
			} catch (IOException ex) {
			  // report
			}
	}
	
	public void writeWriteLockFileToDisc()
	{
		try {
			   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(path+"/WriteLock.txt"), "utf-8"));
				//write it to disc
			    Iterator<Entry<String, String>> it = wmap.entrySet().iterator();
			    while(it.hasNext())
			    {
			    	Entry<String, String> thisEntry = (Entry<String, String>) it.next();
			    	 String outputLine;
			    	 String temp = thisEntry.getValue();
			    	 outputLine = thisEntry.getKey()+" "+temp;
			    	 writer.write(outputLine);
			    	 //may add a new line at file bottom 
			    	 writer.newLine();	 
			    }
			    writer.close();
			} catch (IOException ex) {
			  // report
			}
	}
	
}
