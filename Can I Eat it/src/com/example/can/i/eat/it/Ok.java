package com.example.can.i.eat.it;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Ok extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_ok, menu);
        return true;
    }
}
