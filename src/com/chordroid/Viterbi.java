package com.chordroid;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 06/09/12
 * Time: 6:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class Viterbi {
	public double[] pathProbs;
	private int[][] pathChordLengths;
	public double[] chordProbs;

	public Viterbi() {
		pathProbs=new double[60];
		pathChordLengths=new int[60][2];
		for (int i=0;i<60;i++) pathProbs[i]=1.0;
	}

	//----------------------------------- PROBABILITY SECTION ---------------------------------
	
	void normalizeExcept(double[] arr) {
		//returns true on success
		//normalizes so sum==1
		//if all vals=0 throw error

		int i;
		double total=0.0;
		int n=arr.length;

		for (i=0;i<n;i++) total+=arr[i];
		if (total==0.0) throw new ArithmeticException("Normalization error: array sums to zero");
		else {
			for (i=0;i<n;i++) arr[i]/=total;
		}
	}

	void normalize(double[] arr) {
		try {
			normalizeExcept(arr);
		}
		catch (ArithmeticException e) {

		}
	}

// ------------- bass chroma -----------------

	double[] getRootProbs(double[] chroma) {
		//finds probability that each note is the root and places it in rootProbs array

		int i;
		int[] tableIdx=new int[12];
		int success;
		double[] rootProbs=new double[12];

		try {
			normalizeExcept(chroma);
		} catch (ArithmeticException e) {
			//if chroma is empty, return equal probabilities
			for (i=0;i<12;i++) rootProbs[i]=1.0/12.0;
			return rootProbs;
		}

		for (i=0;i<12;i++) {
			tableIdx[i]=(int)Math.ceil(10*chroma[i]);
		}
		for (i=0;i<12;i++) {
			rootProbs[i]=ProbTables.bassProbTable[tableIdx[i]][tableIdx[(i+7)%12]];
		}
		normalize(rootProbs);
		return rootProbs;
	}

	// ------------------ mid chroma ----------------------
	double getMidChordProb(double[] chroma, int chordTypeId, int noteNum) {
		//finds probability of chord with root note noteNum and type chordTypeId
		//by finding the product pdf values of each of the 12 pitches for that chord

		double prob=1.0;
		int i;
		int j;
		double x,y,mu,sigma,d,a1,a2;

		for (i=0;i<12;i++) {
			j=(i+noteNum)%12;
			x=chroma[j];
			if (x<0.01) y=ProbTables.chordZeroTable[chordTypeId][i];
			else {
				a1=ProbTables.chordGaussTable[chordTypeId][i][0];
				mu=ProbTables.chordGaussTable[chordTypeId][i][1];
				sigma=ProbTables.chordGaussTable[chordTypeId][i][2];
				a2=ProbTables.chordGaussTable[chordTypeId][i][3];
				d=(x-mu)/sigma;
				//calculate gaussian
				y=a1*Math.exp(-0.5*d*d)+a2*Math.exp(-7*Math.abs(x-mu));
			}
			prob*=y;
		}
		return prob;
	}

	double[] getChordProbs(double[] bassChroma, double[] midChroma) {
		int i,j;
		double[] rootProbs;
		double[] chordProbs=new double[60];

		normalize(midChroma);
		rootProbs=getRootProbs(bassChroma);

		for (i=0;i<5;i++) {
			for (j=0;j<12;j++) {
				//chordProbs[i*12+j]=rootProbs[j]*getMidChordProb(midChroma,i,j)*ProbTables.chordTypeProbs[i];
				//no root:
				chordProbs[i*12+j]=getMidChordProb(midChroma,i,j)*ProbTables.chordTypeProbs[i];
			}
		}

		normalize(chordProbs);
		return chordProbs;
	}

	//---------------------------------- VITERBI SECTION -------------------------------------------


	double getTransProb(int chordLen, int prevChordLen) {
		int chordLenDiff;
		double prob;

		chordLenDiff=Math.abs(chordLen-prevChordLen);
		if (chordLen==0&&prevChordLen==0) return 1.0;

		//if (chordLenDiff==0) prob=0.7;
		//else if (chordLenDiff==1) prob=0.5;
		//else prob=0.15;

		//hack!
		prob=0.3;

		if (chordLen<8) prob*=chordLen/8.0;

		return prob;
	}

	int viterbi(double[] bassChroma, double[] midChroma) {
		//paths=[-1,0,0,0]
		int pathIdx,chordIdx;
		double[] prevPathProbs=new double[60];
		double[][] transMtx=new double[60][60];
		double transProb;
		double chordTransProb;

		double prob;
		int maxPathProbIdx;
		double maxPathProb;
		int maxProbIdx;
		double maxProb;

		chordProbs=getChordProbs(bassChroma,midChroma);

		//adjust chord probabilities for each path according to transition probabilities
		//i.e. more likely if chord is same as previous chord
		for (pathIdx=0;pathIdx<60;pathIdx++) {
			transProb=getTransProb(pathChordLengths[pathIdx][0],pathChordLengths[pathIdx][1]);
			for (chordIdx=0;chordIdx<60;chordIdx++) {
				if (pathIdx==chordIdx) chordTransProb=1.0-transProb;
				else chordTransProb=transProb/59.0;
				transMtx[pathIdx][chordIdx]=chordTransProb*chordProbs[chordIdx];
			}
			normalize(transMtx[pathIdx]);
		}

		for (pathIdx=0;pathIdx<60;pathIdx++) prevPathProbs[pathIdx]=pathProbs[pathIdx];

		maxProb=0.0;
		maxProbIdx=-1;
		for (chordIdx=0;chordIdx<60;chordIdx++) {
			maxPathProbIdx=-1;
			maxPathProb=0.0;
			for (pathIdx=0;pathIdx<60;pathIdx++) {
				if (transMtx[pathIdx][chordIdx]<=0.0) prob=prevPathProbs[pathIdx]-100.0;
				else prob=pathProbs[pathIdx]+Math.log(transMtx[pathIdx][chordIdx]);
				if (pathIdx==0||prob>maxPathProb) {
					maxPathProb=prob;
					maxPathProbIdx=pathIdx;
				}
			}
			pathProbs[chordIdx]=maxPathProb;
			if (maxPathProbIdx==chordIdx) { //same chord as before
				pathChordLengths[chordIdx][0]++;
			}
			else { //new chord
				pathChordLengths[chordIdx][1]=pathChordLengths[chordIdx][0];
				pathChordLengths[chordIdx][0]=1;
			}
			if (chordIdx==0||maxPathProb>maxProb) {
				maxProb=maxPathProb;
				maxProbIdx=chordIdx;
			}
		}

		return maxProbIdx;
	}

}
