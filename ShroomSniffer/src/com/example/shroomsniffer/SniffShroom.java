package com.example.shroomsniffer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SniffShroom extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sniff_shroom);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sniff_shroom, menu);
		return true;
	}

}
