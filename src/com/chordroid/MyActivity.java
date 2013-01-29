package com.chordroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;

public class MyActivity extends Activity implements  AudioProcessListener {
    private TextView tview;
	private AudioHandler audioHandler;
	private PeakFinder peakFinder;
	private Viterbi viterbi;
	private FFT fft;
	private ChordView cview;
	int maxfq;
	int chordId;
	int curChordId=-1;
	int prevChordId=-1;
	boolean isPaused=false;

	/**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//Log.i("MyActivity", "--- 0 --- created");
		setTitle("Chordroid");
        setContentView(R.layout.main);
        /*tview=(TextView)findViewById(R.id.maintext);
        //Log.i("MyActivity","tview="+tview);
        tview.setText("Loading");
		tview.setTextSize(60.0f);
		*/
		cview=(ChordView)findViewById(R.id.chordview);
		fft=new FFT(4096);
		audioHandler=new AudioHandler();
		audioHandler.setHandler(this);
		peakFinder=new PeakFinder(audioHandler.SAMPLE_RATE);
		viterbi=new Viterbi();
		audioHandler.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Log.i("MyActivity","--- 6 --- destroyed");
		audioHandler.stopRecorder();
		audioHandler=null;
	}

	@Override
	protected void onStop() {
		super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
		//Log.i("MyActivity","--- 5 --- stopped");
	}

	@Override
	protected void onPause() {
		super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
		//Log.i("MyActivity","--- 4 --- paused");
		isPaused = true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();    //To change body of overridden methods use File | Settings | File Templates.
		//Log.i("MyActivity","--- 7 --- restart");
	}

	@Override
	protected void onResume() {
		super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
		//Log.i("MyActivity","--- 8 --- resume");
		isPaused = false;
	}

	private int maxidx(double[] arr) {
		int i;
		int midx=-1;
		double maxvalue=-1.0;
		for (i=0;i<arr.length;i++) {
			if (arr[i]>maxvalue) {
				midx=i;
				maxvalue=arr[i];
			}
		}
		return midx;
	}

	public void process(short[][] bufferArr,int lastBufferIdx) {
		if (isPaused) return;
		double[] spectrum;
		double[][] chromas;
		spectrum=fft.fftwin(bufferArr,lastBufferIdx);
		chromas=peakFinder.getChromas(spectrum);
		chordId=viterbi.viterbi(chromas[0],chromas[1]);
		//Log.i("MyActivity","Spectrum chord id="+Integer.toString(chordId));

		//int idx=maxidx(chromas[1]);
		//maxfq=idx;
		//maxfq=(int)(idx*audioHandler.SAMPLE_RATE/fft.n);
		//maxamp=(int)spectrum[idx];
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateDisplay();
			}
		});

	}

	private void updateDisplay() {
		Chord[] chordArr=new Chord[60];
		int i;

		for (i=0;i<60;i++) chordArr[i]=new Chord(i,viterbi.pathProbs[i]);
		Arrays.sort(chordArr);

		double refprob=chordArr[0].prob;
		double total=0.0;
		for (i=0;i<60;i++) {
			chordArr[i].prob=Math.exp(chordArr[i].prob-refprob);
			total+=chordArr[i].prob;
		}
		for (i=0;i<60;i++) {
			chordArr[i].prob/=total;
		}

		/*
		for (i=0;i<60;i++) chordArr[i]=new Chord(i,viterbi.chordProbs[i]);
		Arrays.sort(chordArr);
		*/

		//tview.setText(chordArr[0].probStr()+"\n"+chordArr[1].probStr());
		if (curChordId!=chordArr[0].id) {
			prevChordId=curChordId;
			curChordId=chordArr[0].id;
		}
		Chord prevChord=null;
		for (i=0;i<60;i++) {
			if (chordArr[i].id==prevChordId) prevChord=chordArr[i];
		}
		cview.updateChord(chordArr[0],prevChord,chordArr[1]);
	}

	public void showAbout(View view) {
		Intent intent=new Intent(this,AboutScreen.class);
		startActivity(intent);
	}
}
