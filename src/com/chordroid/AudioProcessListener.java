package com.chordroid;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 19/08/12
 * Time: 11:43 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AudioProcessListener {
	void process(short[][] bufferArr,int lastBufferIdx);
}
