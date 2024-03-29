package Chord;

import java.io.Serializable;


public class ChordKey implements Comparable, Serializable {

	private static final long serialVersionUID = -9033914304270942679L;

	String identifier;

	byte[] key;

	public ChordKey(byte[] key) {
		this.key = key;
	}

	public ChordKey(String identifier) {
		this.identifier = identifier;
		this.key = Hash.hash(identifier);
	}

	public boolean isBetween(ChordKey fromKey, ChordKey toKey) {
		if (fromKey.compareTo(toKey) < 0) {
			if (this.compareTo(fromKey) > 0 && this.compareTo(toKey) < 0) {
				return true;
			}
		} else if (fromKey.compareTo(toKey) > 0) {
			if (this.compareTo(toKey) < 0 || this.compareTo(fromKey) > 0) {
				return true;
			}
		}
		return false;
	}

	/*public ChordKey createStartKey(int index) {
		byte[] newKey = new byte[key.length];
		System.arraycopy(key, 0, newKey, 0, key.length);
		int carry = 0;
		for (int i = (Hash.KEY_LENGTH - 1) / 8; i >= 0; i--) {
			int value = key[i] & 0xff;
			value += (1 << (index % 8)) + carry;
			newKey[i] = (byte) value;
			if (value <= 0xff) {
				break;
			}
			carry = (value >> 8) & 0xff;
		}
		return new ChordKey(newKey);
	}
	*/
	//for 32bit int only
	public ChordKey createStartKey(int index) {
		byte[] newkey = new byte[key.length];
		byte[] tempkey = new byte[key.length];
		long n = 0;
		for(int i= key.length-1;i>=0;i--)
		{
		   int val = key[i]& 0xff;
		   n += val << (3-i)*8;
		}
		
		n =  n + (long)Math.pow(2, index);
		
		for(int i= 3;i >=0;i--)
		{  
		   tempkey[i] =  (byte) (n>>i*8);
		   
		}
		
		for(int i= 3;i >=0;i--)
		{  
		   newkey[i] = (byte) (tempkey[3-i]&0xff);
		}
		
		return new ChordKey(newkey);
	}

	public int compareTo(Object obj) {
		ChordKey targetKey = (ChordKey) obj;
		for (int i = 0; i < key.length; i++) {
			int loperand = (this.key[i] & 0xff);
			int roperand = (targetKey.getKey()[i] & 0xff);
			if (loperand != roperand) {
				return (loperand - roperand);
			}
		}
		return 0;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (key.length > 4) {
			for (int i = 0; i < key.length; i++) {
				sb.append(Integer.toString(((int) key[i]) & 0xff) + ".");
			}
		}
		else{
			long n = 0;
			for (int i = key.length-1,j=0; i >= 0 ; i--, j++) {
				
				n |= ((key[i]<<(8*j)) & (0xffL<<(8*j)));
			}
			sb.append(Long.toString(n));
		}
		return sb.toString();
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

}
