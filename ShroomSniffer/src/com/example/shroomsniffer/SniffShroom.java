package com.example.shroomsniffer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SniffShroom extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sniff_shroom);
		Button b = (Button)findViewById(R.id.button1);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(SniffShroom.this, PredictShroom.class);
				startActivity(i);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sniff_shroom, menu);
		return true;
	}

}
