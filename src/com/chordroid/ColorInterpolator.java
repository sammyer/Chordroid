package com.chordroid;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 04/11/12
 * Time: 2:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorInterpolator {
	private ArrayList<ColorInterpolatorItem> colors;
	private static final int CDIM=4;

	public ColorInterpolator(int startColor,int endColor) {
		colors=new ArrayList<ColorInterpolatorItem>();
		colors.add(new ColorInterpolatorItem(0.0f,startColor));
		colors.add(new ColorInterpolatorItem(1.0f,endColor));
	}

	public void add(float position, int color) {
		if (position<0.0f||position>1.0f) return;
		ColorInterpolatorItem item;
		int idx=-1;
		for (int i=0;i<colors.size();i++) {
			item=colors.get(i);
			if (item.position==position) {
				item.color=color;
				return;
			}
			else if (position<item.position) {
				idx=i;
				break;
			}
		}
		if (idx<0) return;
		colors.add(idx,new ColorInterpolatorItem(position,color));
	}

	public int get(double position) {
		return get((float)position);
	}
	public int get(float position) {
		if (position<0.0f||position>1.0f) throw new Error("Position must be between 0 and 1");
		ColorInterpolatorItem prevItem=null, item;
		for (int i=0;i<colors.size();i++) {
			item=colors.get(i);
			if (position==item.position) return item.color;
			else if (position<item.position) return interpolate(prevItem.color,item.color,(position-prevItem.position)/(item.position-prevItem.position));
			prevItem=item;
		}
		return 0;
	}

	public int interpolate(int color1, int color2, float amt) {
		float[] rgb1,rgb2,rgb3;
		rgb1=hex2rgb(color1);
		rgb2=hex2rgb(color2);
		rgb3=new float[CDIM];
		for (int i=0;i<CDIM;i++) {
			rgb3[i]=rgb1[i]*(1.0f-amt)+rgb2[i]*amt;
		}
		return rgb2hex(rgb3);
	}

	private float[] hex2rgb(int hex) {
		float[] rgb=new float[CDIM];
		for (int i=0;i<CDIM;i++) {
			rgb[CDIM-1-i]=hex&0xFF;
			hex>>=8;
		}
		return rgb;
	}

	private int rgb2hex(float[] rgb) {
		int hex=0;
		for (int i=0;i<CDIM;i++) {
			hex<<=8;
			hex+=(int)rgb[i];
		}
		return hex;
	}
}