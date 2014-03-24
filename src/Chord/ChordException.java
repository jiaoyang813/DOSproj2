package Chord;

import java.io.Serializable;

public class ChordException extends Exception implements Serializable {

	private static final long serialVersionUID = 9007778221409966142L;

	public ChordException() {
		super();
	}

	public ChordException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChordException(String message) {
		super(message);
	}

	public ChordException(Throwable cause) {
		super(cause);
	}

}
