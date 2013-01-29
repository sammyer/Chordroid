package com.chordroid;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

class AudioHandler extends Thread {
	private boolean stopped    = false;
	AudioProcessListener listener;
	int SAMPLE_RATE=11025;

	public AudioHandler() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	}

	public void setHandler(AudioProcessListener l) {
		listener=l;
	}

	@Override
	public void run() {
		AudioRecord recorder = null;
		short[][]   buffers  = new short[8][2048];
		int         bufferIdx       = 0;

		stopped=false;
		try { // ... initialise

			int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
			if (bufferSize<=0) {
				throw new RuntimeException("Invalid sample rate");
			}
			//Log.i("MyActivity","--- 1 --- : "+bufferSize);

			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,bufferSize*10);
			//Log.i("MyActivity","--- 2 ---- : "+recorder.getState());
			recorder.startRecording();

			//Log.i("MyActivity","--- 3 ---");
			// ... loop

			while(!stopped) {
			//for (int i=0;i<25;i++) {
				short[] buffer = buffers[bufferIdx];

				bufferSize = recorder.read(buffer,0,buffer.length);
				listener.process(buffers,bufferIdx);
				bufferIdx=(bufferIdx+1)%buffers.length;
			}
			recorder.stop();
		} catch(Throwable x) {
			//Log.w("MyActivity", "Error reading voice audio", x);
		} finally {
			if (recorder!=null) recorder.release();
			stopped=true;
		}
		recorder=null;
	}

	public void stopRecorder() {
		stopped=true;
	}
}
