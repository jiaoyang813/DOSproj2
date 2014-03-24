package Chord;

import java.io.Serializable;

public class FingerTable implements Serializable {

	private static final long serialVersionUID = 2783170996650210565L;
	Finger[] fingers;

	public FingerTable(ChordNode node) {
		this.fingers = new Finger[Hash.KEY_LENGTH];
		for (int i = 0; i < fingers.length; i++) {
			ChordKey start = node.getNodeKey().createStartKey(i);
			fingers[i] = new Finger(start, node);
		}
	}

	public Finger getFinger(int i) {
		return fingers[i];
	}

}
