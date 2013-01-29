package com.chordroid;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 06/09/12
 * Time: 4:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class PeakFinder {
	double sampleRate;

	public PeakFinder(double sampleRate) {
		this.sampleRate=sampleRate;
	}

	Peak quadSpline(double yprev,double y0,double ynext,double x0) {
		//interpolate with a quadratic spline
		double a,b,c,x,y;
		c=y0;
		b=0.5*(ynext-yprev);
		a=0.5*(ynext+yprev)-y0;
		if (a==0) x=0;
		else x=-0.5*b/a;
		y=a*x*x+b*x+c;
		return new Peak(x+x0,y);
	}

/*
	int getCubicMax(float yneg1,float y0,float y1,float y2,float *xval,float *yval) {
		//returns 1 if cubic max found in range [0,1], 0 otherwise
		//interpolate with a quadratic spline
		float a,b,c,d,x,y;
		float s0,s1,w,z,disc,sqrtdisc;
		//s0=0.5*(y1-yneg1);
		//s1=0.5*(y2-y0);
		s0=tan(0.5*(tan(y0-yneg1)+tan(y1-y0)));
		s1=tan(0.5*(tan(y2-y1)+tan(y1-y0)));
		w=s1-s0;
		z=y1-y0-s0;
		d=y0;
		c=s0;
		b=3*z-w;
		a=w-2*z;

		//3a+2b+c=0
		if (a==0&&b==0) return 0;
		else if (a==0) x=-c/(2*b);
		else {
			disc=b*b-3*a*c;
			if (disc<0) return 0;

			sqrtdisc=sqrt(disc);
			x=(sqrtdisc-b)/(3*a);
			if (3*a*x+b>0) x=-(sqrtdisc+b)/(3*a);  //this is a min
		}
		if (x<0||x>=1) return 0;

		y=a*x*x*x+b*x*x+c*x+d;
		*xval=x;
		*yval=y;
		return 1;
	}

	int cubicSpline(float *arr, int n, int pos,float *xval, float *yval) {
		float x,y;
		float yneg2,yneg1,y0,y1,y2;
		int maxFound;

		yneg2=arr[pos-2<0?0:pos-2];
		yneg1=arr[pos-1<0?0:pos-1];
		y0=arr[pos];
		y1=arr[pos+1>=n?n-1:pos+1];
		y2=arr[pos+2>=n?n-1:pos+2];
		maxFound=getCubicMax(yneg2,yneg1,y0,y1,&x,&y);
		if (maxFound) {
			*xval=x-1.0;
			*yval=y;
			return 1;
		}
		maxFound=getCubicMax(yneg1,y0,y1,y2,&x,&y);
		if (maxFound) {
			*xval=x;
			*yval=y;
			return 1;
		}
		return 0;
	}
*/
	ArrayList<Peak> findPeaks(double[] spec) {
		int i;
		int n=spec.length;
		int peakIdx;
		double minVal, prevMin;
		Peak curPeak=null,prevPeak=null;
		ArrayList<Peak> peakArr=new ArrayList<Peak>(100);

		//find local maxima as peaks
		//also find minimum values between those local maxima, assume this is noise, and subtract from peak values
		peakIdx=0;
		minVal=-1.0; //initialize value to negative to represent no minimum found yet
		prevMin=-1.0;
		for (i=0;i<n;i++) {
			if (i>0&&i<n-1&&spec[i]>spec[i-1]&&spec[i]>spec[i+1]) {
				//peak found - do quadratic interpolation
				prevPeak=curPeak;
				curPeak=quadSpline(spec[i-1],spec[i],spec[i+1],(double)i);
				peakArr.add(curPeak);

				//subtract local minima on either side of each maximum in order to compensate for noise
				// peakIdx-1 because we are here :
				// peals[peakIdx-2] .... prevMin ....  peaks[peakIdx-1] .... minVal ..... HERE
				if (peakIdx>0) prevPeak.amp-=0.5*(minVal+prevMin);

				prevMin=minVal;
				minVal=-1.0;
				peakIdx++;
			}
			else if (minVal<0) { //first value of minimum
				minVal=spec[i];
			}
			else { //find minimum value between peaks
				if (spec[i]<minVal) minVal=spec[i];
			}
		}
		if (peakIdx>0) prevPeak.amp-=0.5*(minVal+prevMin);

		return peakArr;
	}

	void pruneMaxes(ArrayList<Peak> peakList,double semiDist,double minAmpRatio) {
		//prunes peaks as follow:
		//semiDist - peaks must be at least semiDist semitones apart
		//ampDiff - peak amp must be >max(peak.amp)*minAmpRatio
		//maximum number of peaks returned=50
		int maxN=50;
		int peaksKept=0;
		int i;
		int j;
		double minAmp;
		int minAmpIdx; //index at which amp goes below threshold - only need to check up to this index
		double fqRatio=Math.pow(2.0,semiDist/12.0);

		int n=peakList.size();
		Collections.sort(peakList);
		Peak[] peakArr=peakList.toArray(new Peak[n]);
		boolean[] isMaxRemoved=new boolean[n];

		minAmp=peakArr[0].amp*minAmpRatio;
		minAmpIdx=n;
		for (i=0;i<n;i++) {
			if (peakArr[i].amp<minAmp) {
				minAmpIdx=i;
				break;
			}
		}

		for (j=0;j<minAmpIdx;j++) {
			if (isMaxRemoved[j]) continue;
			for (i=j+1;i<minAmpIdx;i++) {
				if (isMaxRemoved[i]) continue;
				//if peak i (smaller amp) is with fqRatio of peak j, remove peak i
				if (peakArr[i].fq<peakArr[j].fq*fqRatio&&peakArr[i].fq*fqRatio>peakArr[j].fq) {
					isMaxRemoved[i]=true;
				}
			}
			peaksKept++;
			if (peaksKept==maxN) break;
		}

		//consolidate array by removing peaks no longer used
		peakList.clear();
		peakList.ensureCapacity(peaksKept);
		j=0;
		for (i=0;i<n;i++) {
			if (isMaxRemoved[i]) continue;
			peakList.add(peakArr[i]);
			j++;
			if (j==peaksKept) break;
		}
	}

	double errorPenalty(double errorAmt) {
		//return cos(errorAmt*PI)
		double abserr=Math.abs(errorAmt);
		if (abserr>0.25) return 2.0-abserr*4.0;
		else return 1.0;
	}

	double[][] getChromas(double[] spec) {
		double[][] chroma=new double[2][12];
		//chroma={basschroma,midchroma}
		int i;
		int numPeaks;
		int tempNumPeaks;
		double semis;
		ArrayList<Peak> peakArr;
		int n=spec.length;
		int windowSize=n*2;
		int chromaId;
		Peak peak;

		int pitchId; //where 0=C 1=C# ... 11=B
		double tuningError;

		//precalculate these 2 values
		double n1=sampleRate/windowSize/440.0;
		double n2=12.0/Math.log(2.0);

		peakArr=findPeaks(spec);
		pruneMaxes(peakArr,0.4,0.002);
		//printf("%d, ",numPeaks);

		for (i=0;i<peakArr.size();i++) {
			peak=peakArr.get(i);
			//semitones from A440 = log2(fq*fsamp/windowsize)*12
			semis=Math.log(peak.fq * n1)*n2;
			//printf("%.2f %.2f %.2f\n",peakArr[i].fq,semis,peakArr[i].amp);
			if (semis>=-40&&semis<-16) chromaId=0; //bass
			else if (semis>=-16&&semis<8) chromaId=1; //mids
			else continue; //outside of range

			pitchId=(int)Math.round(semis);
			pitchId=(pitchId+57)%12;
			tuningError=semis-Math.round(semis);
			chroma[chromaId][pitchId]+=peak.amp*errorPenalty(tuningError);
		}
		return chroma;
	}

}
