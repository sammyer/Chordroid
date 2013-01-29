package com.chordroid;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 07/09/12
 * Time: 2:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class Chord implements Comparable<Chord>{
	public int id;
	public double prob;
	public final String chordTypes[]={"","m","dim","aug","sus"};
	public final String noteNames[]={"C","C#","D","Eb","E","F","F#","G","Ab","A","Bb","B"};

	public Chord(int id, double prob) {
		this.id=id;
		this.prob=prob;
	}

	public String name() {
		int noteId=id%12;
		int chordTypeId=id/12;
		return noteNames[noteId]+chordTypes[chordTypeId];
	}

	public String probStr() {
		int noteId=id%12;
		int chordTypeId=id/12;
		return noteNames[noteId]+chordTypes[chordTypeId]+" - "+Integer.toString((int)(prob*100))+"%";
	}
	public int compareTo(Chord chord) {
		if (prob>chord.prob) return -1;
		else if (prob==chord.prob) return 0;
		else return 1;
	}
}
