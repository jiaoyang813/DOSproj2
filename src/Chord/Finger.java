package Chord;

import java.io.Serializable;

public class Finger implements Serializable {

	private static final long serialVersionUID = 1696369378218223311L;

	ChordKey start;

	ChordNode node;

	public Finger(ChordKey start, ChordNode node) {
		this.node = node;
		this.start = start;
	}

	public ChordKey getStart() {
		return start;
	}

	public void setStart(ChordKey start) {
		this.start = start;
	}

	public ChordNode getNode() {
		return node;
	}

	public void setNode(ChordNode node) {
		this.node = node;
	}

}
