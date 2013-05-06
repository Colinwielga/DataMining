package com.example.shroomsniffer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ImageView;

public class PredictShroom extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_predict_shroom);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.predict_shroom, menu);
		return true;
	}

}
