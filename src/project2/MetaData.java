package project2;

class MetaData{
	String lock;
	String lastModifyTime;
	
	MetaData(String thelock, String time)
	{
		lock = thelock;
		lastModifyTime = time;
	}
	
	MetaData(String thelock)
	{
		lock = thelock;
	}
	
	public String toString()
	{
		return lock+" "+lastModifyTime;
	}
}

