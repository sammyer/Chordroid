package com.chordroid;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 06/09/12
 * Time: 4:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class Peak implements Comparable<Peak> {
	double fq;
	double amp;

	public Peak(double fq, double amp) {
		this.fq=fq;
		this.amp=amp;
	}

	public int compareTo(Peak p) {
		if (amp>p.amp) return -1;
		else if (amp==p.amp) return 0;
		else return 1;
	}
}
