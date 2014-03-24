package project2;

class Tuple {
	String elem1;//elem1 is key element
	String elem2;
	String elem3;
	Tuple(String s1, String s2, String s3)
	{
		elem1 = s1;
		elem2 = s2;
		elem3 = s3;
	}
	
	public boolean isEqual(Tuple other)
	{
		if(other != null)
		{
			if( this.elem1.equals(other.elem1)
				&&this.elem2.equals(other.elem2)
				&&this.elem3.equals(other.elem3) )
			{
				return true;
			}
			else
				return false;
				
		}
		else
			return false;
	}
	
	public String toString()
	{
		return this.elem1 +" "+this.elem2+" "+ this.elem3;
	}
}