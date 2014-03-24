package project2;

import java.io.BufferedReader;
import java.io.PrintWriter;

class ProcessRequest {
	String client = "X";
	String ID = null;
	String FileName = null;
	String result = null;
	int clientlistenport;
	//in and out stream
	PrintWriter out;
	BufferedReader in;
	myDB db;
	ProcessRequest()
	{
		db = myDB.getInstance();
	}
	public void setOutStream(PrintWriter o)
	{
		out = o;
	}
	public void setInStream(BufferedReader i)
	{
		in = i;
	}
	public void uploadFile(String FileName)
	{
		MetaData curState = new MetaData("NULL");
		db.updateMetaFile(FileName, curState);
	}
	
	public String processInputline(String[] cmd)
	{
		//implement a switch statement: insert, delete, match.
		if(cmd.length <= 0)
			return "INVALID INPUT";
		
		switch(cmd[0])
		{
		//client auto register itself
		case "REGISTER":
			client = cmd[1];
			clientlistenport = Integer.parseInt(cmd[2]);
			return "Welcome Client "+ client;	
		case "BYE":// client disconnect from server
			db.closeDB();
			return "BYE";
		case "UPLOAD":
			FileName = cmd[1];
			if(db.metafile.containsKey(FileName))
			{
				if(db.checkReadLock(FileName, client)||
					db.checkWriteLock(FileName, client))
				    return "FILELOCKED";
			}
			out.println("STARTUPLOAD");
			db.uploadFile(FileName, in);
			this.uploadFile(FileName);//refresh db directory
			return "UPLOADED";
		case "READLOCK":
			FileName = cmd[1];
			if(!db.metafile.containsKey(FileName))
				return "NOFILE";
			if(db.checkReadLock(FileName, client))
				return "already got readlock";
			if(db.getReadLock(FileName, client))
			   return "get readlock";
			else
				return "NOFILE";
		case "WRITELOCK":
			FileName = cmd[1];
			if(!db.metafile.containsKey(FileName))
				return "NOFILE";
			if(db.checkWriteLock(FileName, client))
				return "already got writelock";
			if(db.getWriteLock(FileName, client))
				return "get writelock";
			else
				return "cannot get writelock";
		case "RELEASELOCK":
			FileName = cmd[1];
			if(!db.metafile.containsKey(FileName))
				return "NOFILE";
			if(db.checkReadLock(FileName, client)
					||db.checkWriteLock(FileName, client))
			{
				if(db.releaseLock(FileName, client))
				 return "Lock Released";
				else
				 return "cannot release lock";
			}
			else
				return "No lock on file";
		case "READ":
			FileName = cmd[1];
			if(!db.metafile.containsKey(FileName))
				return "NOFILE";
			if(db.checkReadLock(FileName, client))
			{
				if(cmd.length != 2)
					return "Format Mismatch(READ [FILENAME])";
				if(!FileName.equals(db.curFileName))
				{
					db.loadFile(FileName);
					db.printCurFile(out);	
					return "ENDFILE";
				}
				else
				{
					db.printCurFile(out);	
					return "ENDFILE";
				}
			}
			else
				return "Require ReadLock";
		case "WRITE":
			FileName = cmd[1];
			if(!db.metafile.containsKey(FileName))
				return "NOFILE";
			if(db.checkWriteLock(FileName, client))
			{
				Tuple t = new Tuple(cmd[2],cmd[3],cmd[4]);
				db.loadFile(FileName);
				if(db.insert(t))
				{
					db.saveCurFile(FileName);
					return "WRITE DONE";
				}
				else
					return "Conflicts!";
			}
			else
				return "Require WriteLock";
		case "SHOWDIR":
			db.printDir(out);//setOutStream first
			return "ENDDIR";
		case "SHUTDOWN":
			if(cmd.length == 2&&cmd[1].equals("SERVER") )//client make server shut down
			{
				db.closeDB();
				return "SERVER SHUTDOWN";
			}
			else
				return "INVALID COMMAND";
		case "INSERT":
			if(cmd.length != 5)
				return "Format Mismatch(INSERT FILENAME STRING STRING STRING)";
			Tuple t = new Tuple(cmd[2],cmd[3],cmd[4]);
			FileName = cmd[1];
			db.loadFile(FileName);
			if(db.insert(t))
			{
				db.saveCurFile(FileName);
				return "INSERT DONE";
			}
			else
				return "Conflicts!";
		case "DELETE":
			if(cmd.length == 2) //delete filename
			{
				FileName = cmd[1];
				if(db.checkReadLock(FileName, client)|| db.checkWriteLock(FileName, client))
					return "file locked";
			   if(db.deleteFile(cmd[1]))
				   return cmd[1]+ " Deleted";
			   else
				   return "Cannot Delete "+ cmd[1];
			}
			else if(cmd.length == 5)
			{	
				FileName = cmd[1];
				Tuple toDel = new Tuple(cmd[2],cmd[3],cmd[4]);
				FileName = cmd[1];
				if(!FileName.equals(db.curFileName))
					db.loadFile(FileName);
				if(db.delete(toDel))
				{
					db.saveCurFile(FileName);
					return "DELETE DONE";
				}
				else
					return "NO MATCH";
			}
			return "Format Mismatch(DELETE FILENAME STRING STRING STRING)";
		case "MATCH":
			FileName = cmd[1];
			if(cmd.length != 5)
				return "Format Mismatc(MATCH FILENAME STRING STRING STRING)";
			Tuple toMatch = new Tuple(cmd[2],cmd[3],cmd[4]);
			FileName = cmd[1];
			if(!FileName.equals(db.curFileName))
				db.loadFile(FileName);
			
			if(db.search(toMatch)!=null )
			{
				return "MATCH: "+ db.search(toMatch).toString();
			}
			else 
				return "NO MATCH";		
		case "SHOWCURFILE":
			if(FileName!=null&&!FileName.equals(db.curFileName))
			{	
				db.loadFile(FileName);
				FileName = db.curFileName;
			}
			if(db.curFile != null)
			{
				db.printCurFile(out);
				return "ENDFILE";
			}
			return "NOFILE";
		
		default:
			break;
		}
		
		return "INVALID COMMAND";
	}
}
