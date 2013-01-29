package com.chordroid;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 12/09/12
 * Time: 5:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class AboutScreen extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("About");
		setContentView(R.layout.about);
		TextView tview=(TextView)findViewById(R.id.abouttext);
		Linkify.addLinks(tview,Linkify.ALL);
	}

	public void hideAbout(View view) {
		finish();
	}

}
