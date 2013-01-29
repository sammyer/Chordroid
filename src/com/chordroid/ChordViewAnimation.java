package com.chordroid;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 25/09/12
 * Time: 9:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChordViewAnimation extends Animation implements Animation.AnimationListener {
	ChordView view;

	public ChordViewAnimation(ChordView view) {
		this.view = view;
		setAnimationListener(this);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		//Log.i("MyActivity","onAnimationEnd");
		view.isAnimating=false;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
		//Log.i("MyActivity","onAnimationStart");
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		//Log.i("MyActivity","onAnimation "+Float.toString(interpolatedTime));
		view.t=interpolatedTime;
		view.postInvalidate();
	}

}
