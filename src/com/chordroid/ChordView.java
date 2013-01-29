package com.chordroid;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 13/09/12
 * Time: 5:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChordView extends View {
	private Chord curChord,prevChord,nextChord,prevChord2;
	public boolean isAnimating=false;
	private ChordViewAnimation anim;
	public float t;
	private ColorInterpolator barColorInter;

	public ChordView(Context context) {
		super(context);
		setWillNotDraw(false);
		init();
	}

	public ChordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ChordView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		barColorInter=new ColorInterpolator(0xFF0000,0x00CC00);
		barColorInter.add(0.5f,0xAAAA00);
	}

	private float interp(float startVal,float endVal,float t) {
		return startVal*(1.0f-t)+endVal*t;
	}

	private int interpw(int w,int startVal,int endVal,float t) {
		float x=startVal*(1.0f-t)+endVal*t;
		x*=w/30;
		return (int)x;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);    //To change body of overridden methods use File | Settings | File Templates.
		int w = getRight()-getLeft();
		int h = getBottom()-getTop();
		if (isAnimating) {
			if (prevChord2!=null) drawChord(canvas,prevChord2,new Rect(interpw(w, 3, 0, t),0,interpw(w, 8, 0, t),h),interp(0.5f,0.0f,t));
			if (prevChord!=null) drawChord(canvas,prevChord,new Rect(interpw(w,10,3,t),0,interpw(w,20,8,t),h),interp(1.0f,0.5f,t));
			if (curChord!=null) drawChord(canvas,curChord,new Rect(interpw(w,22,10,t),0,interpw(w,27,20,t),h),interp(0.5f,1.0f,t));
			if (nextChord!=null) drawChord(canvas,nextChord,new Rect(interpw(w, 30, 22, t),0,interpw(w, 30, 27, t),h),interp(0.0f,0.5f,t));
		}
		else {
			if (prevChord!=null) drawChord(canvas,prevChord,new Rect(w*3/30,0,w*8/30,h),0.5f);
			if (curChord!=null) drawChord(canvas,curChord,new Rect(w*10/30,0,w*20/30,h),1.0f);
			if (nextChord!=null) drawChord(canvas,nextChord,new Rect(w*22/30,0,w*27/30,h),0.5f);
		}
	}

	private void drawChord(Canvas canvas, Chord chord,Rect bounds,float alpha) {
		int boundsW=bounds.right-bounds.left;
		int boundsH=bounds.bottom-bounds.top;

		int x,y,w,h;
		if (boundsW>boundsH*0.8) {
			h=boundsH;
			w=(int)(h*0.8);
			x=(bounds.left+bounds.right-w)/2;
			y=bounds.top;
		}
		else {
			w=boundsW;
			h=(int)(w*1.25);
			x=bounds.left;
			y=(bounds.top+bounds.bottom-w)/2;
		}

		float barWidth=w*8/11;
		float barHeight=h/12;
		float barY=y+h*4/5;
		Paint paint=new Paint();
		int val=(int)(alpha*255);
		int alphaVal=val<<24;

		//draw BG
		/*
		paint.setColor(alphaVal+0x0000AA);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRoundRect(new RectF(x, y, x + w, y + w), w/12, w/12, paint);
		paint.setColor(0xFF000000);
		canvas.drawRoundRect(new RectF(x +(w- barWidth)/2-2, barY-2, x + (w+barWidth)/2+2, barY + barHeight+2),5,5, paint);
		*/
		//Bitmap bgbmp=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(),R.drawable.boxbg),w,w,false);

		//Bitmap bgbmp=BitmapFactory.decodeResource(getContext().getResources(), R.drawable.boxbg2);
		//canvas.drawBitmap(bgbmp,null,new Rect(x,y,x+w,y+h),null);

		Bitmap bgbmp=BitmapFactory.decodeResource(getContext().getResources(), R.drawable.boxbgshadow);
		canvas.drawBitmap(bgbmp,null,new Rect(x-w*8/77,y-w*9/99,x+w*93/77,y+h*113/99),null);


		//draw bar
		//paint.setColor(barColor);

		ColorInterpolator barShade=new ColorInterpolator(0xFF000000,0xFFFFFFFF);
		barShade.add(0.5f,0xFF000000+barColorInter.interpolate(0,barColorInter.get(chord.prob),alpha));
		float[] gradPositions={0,0.25f,0.5f,1.0f};
		int[] gradColors={barShade.get(0.8),barShade.get(0.5),barShade.get(0.5),barShade.get(alpha*0.1)};
		paint.setShader(new LinearGradient(x+w/2,barY,x+w/2,barY+barHeight,gradColors,gradPositions, Shader.TileMode.CLAMP));
		RectF barRect=new RectF(x + (w - barWidth) / 2, barY, x + (w - barWidth) / 2 + barWidth * (float) chord.prob, barY + barHeight);
		canvas.drawRoundRect(barRect, barHeight*0.5f,barHeight*0.5f,paint);
		paint.setShader(null);

		//draw text
		paint.setColor(alphaVal+0x9999FF);
		float textSize=w/2;
		if (chord.name().length()>2) textSize*=2.5/chord.name().length();
		paint.setTextSize(textSize);
		paint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(chord.name(), x+w/2, y+h/2, paint);
	}

	public void updateChord(Chord curChord,Chord prevChord,Chord nextChord) {
		if (isAnimating) return;
		int prevChordId=this.curChord==null?-1:this.curChord.id;
		this.prevChord2=this.prevChord;
		this.curChord=curChord;
		this.prevChord=prevChord;
		this.nextChord=nextChord;
		if (curChord.id==prevChordId||prevChord==null||prevChord2==null) {
			postInvalidate();
		}
		else {
			isAnimating=true;
			anim=new ChordViewAnimation(this);
			anim.setDuration(250);
			startAnimation(anim);
		}
	}

}
