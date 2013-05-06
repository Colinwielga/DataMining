package com.example.can.i.eat.it;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends Activity {
    RadioGroup rg1_cap_shape;
    RadioGroup rg2_cap_surface;
    RadioGroup rg3_cap_color;
    RadioGroup rg4_bruises;
    RadioGroup rg5_odor;
    RadioGroup rg6_gill_attachment;
    RadioGroup rg7_gill_spacing;
    RadioGroup rg8_gill_size;
    RadioGroup rg9_gill_color;
    RadioGroup rg10_stalk_shape;
    RadioGroup rg11_stalk_root;
    RadioGroup rg12_stalk_surface_above;
    RadioGroup rg13_stalk_surface_below;
    RadioGroup rg14_stalk_color_above;
    RadioGroup rg15_stalk_color_below;
    RadioGroup rg16_veil_type;
    RadioGroup rg17_veil_color;
    RadioGroup rg18_ring_number;
    RadioGroup rg19_ring_type;
    RadioGroup rg20_spore_print_color;
    RadioGroup rg21_population;
    RadioGroup rg22_habitat;
    HashMap<String,String> hm_cap_shape;
    HashMap<String,String> hm_cap_surface;
    HashMap<String,String> hm_cap_color;
    HashMap<String,String> hm_bruises;
    HashMap<String,String> hm_odor;
    HashMap<String,String> hm_gill_attachment;
    HashMap<String,String> hm_gill_spacing;
    HashMap<String,String> hm_gill_size;
    HashMap<String,String> hm_gill_color;
    HashMap<String,String> hm_stalk_shape;
    HashMap<String,String> hm_stalk_root;
    HashMap<String,String> hm_stalk_surface_above;
    HashMap<String,String> hm_stalk_surface_below;
    HashMap<String,String> hm_stalk_color_above;
    HashMap<String,String> hm_stalk_color_below;
    HashMap<String,String> hm_veil_type;
    HashMap<String,String> hm_veil_color;
    HashMap<String,String> hm_ring_number;
    HashMap<String,String> hm_ring_type;
    HashMap<String,String> hm_spore_print_color;
    HashMap<String,String> hm_population;
    HashMap<String,String> hm_habitat;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        rg1_cap_shape = (RadioGroup) findViewById(R.id.rg1_cap_shape);
        rg2_cap_surface = (RadioGroup) findViewById(R.id.rg2_cap_surface);
        rg3_cap_color = (RadioGroup) findViewById(R.id.rg3_cap_color);
        rg4_bruises = (RadioGroup) findViewById(R.id.rg4_bruises);
        rg5_odor = (RadioGroup) findViewById(R.id.rg5_odor);
        rg6_gill_attachment = (RadioGroup) findViewById(R.id.rg6_gill_attachment);
        rg7_gill_spacing = (RadioGroup) findViewById(R.id.rg7_gill_spacing);
        rg8_gill_size = (RadioGroup) findViewById(R.id.rg8_gill_size);
        rg9_gill_color = (RadioGroup) findViewById(R.id.rg9_gill_color);
        rg10_stalk_shape = (RadioGroup) findViewById(R.id.rg10_stalk_shape);
        rg11_stalk_root = (RadioGroup) findViewById(R.id.rg11_stalk_root);
        rg12_stalk_surface_above = (RadioGroup) findViewById(R.id.rg12_stalk_surface_above_ring);
        rg13_stalk_surface_below = (RadioGroup) findViewById(R.id.rg13_stalk_surface_below_ring);
        rg14_stalk_color_above = (RadioGroup) findViewById(R.id.rg14_stalk_color_above_ring);
        rg15_stalk_color_below = (RadioGroup) findViewById(R.id.rg15_stalk_color_below_ring);
        rg16_veil_type = (RadioGroup) findViewById(R.id.rg16_veil_type);
        rg17_veil_color = (RadioGroup) findViewById(R.id.rg17_veil_color );
        rg18_ring_number = (RadioGroup) findViewById(R.id.rg18_ring_number);
        rg19_ring_type = (RadioGroup) findViewById(R.id.rg19_ring_type);
        rg20_spore_print_color = (RadioGroup) findViewById(R.id.rg20_spore_print_color);
        rg21_population = (RadioGroup) findViewById(R.id.rg21_population);
        rg22_habitat = (RadioGroup) findViewById(R.id.rg22_habitat);
       
        hm_cap_shape = new HashMap<String,String>();
        hm_cap_shape.put("Bell","b");
        hm_cap_shape.put("Conical","c");
        hm_cap_shape.put("Convex","x");
        hm_cap_shape.put("Flat","f");
        hm_cap_shape.put("Knobbed","k");
        hm_cap_shape.put("Sunken","s"); 
         hm_cap_surface = new HashMap<String,String>();
        hm_cap_surface.put("Fibrous","f");
        hm_cap_surface.put("Grooves","g");
        hm_cap_surface.put("Scaly","y");
        hm_cap_surface.put("Smooth","s");
         hm_cap_color = new HashMap<String,String>();
        hm_cap_color.put("Brown","n");
        hm_cap_color.put("Buff","b");
        hm_cap_color.put("Cinnamon","c");
        hm_cap_color.put("Gray","g");
        hm_cap_color.put("Green","r");
        hm_cap_color.put("Pink","p");
        hm_cap_color.put("Purple","u");
        hm_cap_color.put("Red","e");
        hm_cap_color.put("White","w");
        hm_cap_color.put("Yellow","y" );
        hm_bruises = new HashMap<String,String>();
        hm_bruises.put("Yes","t");
        hm_bruises.put("No","f" );
        hm_odor = new HashMap<String,String>();
        hm_odor.put("Almond","a");
        hm_odor.put("Anise","l");
        hm_odor.put("Creosote","c");
        hm_odor.put("Fishy","y");
        hm_odor.put("Foul","f");
        hm_odor.put("Musty","m");
        hm_odor.put("None","n");
        hm_odor.put("Pungent","p");
        hm_odor.put("Spicy","s" );
        hm_gill_attachment = new HashMap<String,String>();
        hm_gill_attachment.put("Attached","a");
        hm_gill_attachment.put( "Descending","d");
        hm_gill_attachment.put("Free","f");
        hm_gill_attachment.put("Notched","n" );
       hm_gill_spacing = new HashMap<String,String>();
        hm_gill_spacing.put("Close","c");
        hm_gill_spacing.put("Crowded","w");
        hm_gill_spacing.put("Distant","d" );
        hm_gill_size = new HashMap<String,String>();
        hm_gill_size.put("Broad","b");
        hm_gill_size.put("Narrow","n"  );       		
        hm_gill_color = new HashMap<String,String>();
        hm_gill_color.put("Black","k");
        hm_gill_color.put("Brown","n");
        hm_gill_color.put("Buff","b");
        hm_gill_color.put("Chocolate","c");
        hm_gill_color.put("Gray","g");
        hm_gill_color.put("Green","r");
        hm_gill_color.put("Orange","o");
        hm_gill_color.put("Pink","p");
        hm_gill_color.put("Purple","u");
        hm_gill_color.put("Red","e");
        hm_gill_color.put("White","w");
        hm_gill_color.put("Yellow","y" );
        hm_stalk_shape = new HashMap<String,String>();
        hm_stalk_shape.put("Enlarging","e");
        hm_stalk_shape.put("Tapering","t" );
        hm_stalk_root = new HashMap<String,String>();
        hm_stalk_root.put("Bulbous","b");
        hm_stalk_root.put("Club","c");
        hm_stalk_root.put("Cup","u");
        hm_stalk_root.put("Equal","e");
        hm_stalk_root.put("Rhizomorphs","z");
        hm_stalk_root.put("Rooted","r");
        hm_stalk_root.put("Missing","?" );
        hm_stalk_surface_above = new HashMap<String,String>();
        hm_stalk_surface_above.put("Fibrous","f");
        hm_stalk_surface_above.put("Grooves","g");
        hm_stalk_surface_above.put("Scaly","y");
        hm_stalk_surface_above.put("Smooth","s");
        hm_stalk_surface_below = new HashMap<String,String>();
        hm_stalk_surface_below.put("Fibrous","f");
        hm_stalk_surface_below.put("Grooves","g");
        hm_stalk_surface_below.put("Scaly","y");
        hm_stalk_surface_below.put("Smooth","s");
        hm_stalk_color_above = new HashMap<String,String>();
        hm_stalk_color_above.put("Brown","n");
        hm_stalk_color_above.put("Buff","b");
        hm_stalk_color_above.put("Cinnamon","c");
        hm_stalk_color_above.put("Gray","g");
        hm_stalk_color_above.put("Orange","r");
        hm_stalk_color_above.put("Pink","p");
        hm_stalk_color_above.put("Red","e");
        hm_stalk_color_above.put("White","w");
        hm_stalk_color_above.put("Yellow","y" );
        hm_stalk_color_below = new HashMap<String,String>();
        hm_stalk_color_below.put("Brown","n");
        hm_stalk_color_below.put("Buff","b");
        hm_stalk_color_below.put("Cinnamon","c");
        hm_stalk_color_below.put("Gray","g");
        hm_stalk_color_below.put("Orange","r");
        hm_stalk_color_below.put("Pink","p");
        hm_stalk_color_below.put("Red","e");
        hm_stalk_color_below.put("White","w");
        hm_stalk_color_below.put("Yellow","y" );
        hm_veil_type = new HashMap<String,String>();
        hm_veil_type.put("Partial","p");
        hm_veil_type.put("Universal","u"); 
        hm_veil_color = new HashMap<String,String>();
        hm_veil_color.put("Brown","n");
        hm_veil_color.put("Orange","r");
        hm_veil_color.put("White","w");
        hm_veil_color.put( "Yellow","y"); 
        hm_ring_number = new HashMap<String,String>();
        hm_ring_number.put("None","n");
        hm_ring_number.put("One","o");
        hm_ring_number.put("Two","t" );
        hm_ring_type = new HashMap<String,String>();
        hm_ring_type.put("Cobwebby","c");
        hm_ring_type.put("Evanesent","e");
        hm_ring_type.put("Flaring","f");
        hm_ring_type.put("Large","l");
        hm_ring_type.put("None","n");
        hm_ring_type.put("Pendant","p");
        hm_ring_type.put("Sheathing","s");
        hm_ring_type.put("Zone","z" );
        hm_spore_print_color = new HashMap<String,String>();
        hm_spore_print_color.put("Black","k");
        hm_spore_print_color.put("Brown","n");
        hm_spore_print_color.put("Buff","b");
        hm_spore_print_color.put("Chocolate","c");
        hm_spore_print_color.put("Green","r");
        hm_spore_print_color.put("Orange","o");
        hm_spore_print_color.put("Purple","u");
        hm_spore_print_color.put("White","w");
        hm_spore_print_color.put("Yellow","y" );
         hm_population = new HashMap<String,String>();
        hm_population.put("Abundant","a");
        hm_population.put("Clustered","c");
        hm_population.put("Numerous","n");
        hm_population.put( "Scattered","s");
        hm_population.put("Several","v");
        hm_population.put("Solitary","y" );
         hm_habitat = new HashMap<String,String>();
        hm_habitat.put("Grasses","g");
        hm_habitat.put("Leaves","l");
        hm_habitat.put("Meadows","m");
        hm_habitat.put("Paths","p");
        hm_habitat.put("Urban","u");
        hm_habitat.put("Waste","w");
        hm_habitat.put("Woods","d");

		Button go = (Button) findViewById(R.id.go);
		go.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String instanceString = getInstanceString();
				Log.i("test",instanceString);
//				NominalInstance ni = new NominalInstance(getInstanceString());
				
				
				Intent openFinder = new Intent(MainActivity.this,
						Ok.class);
				MainActivity.this.startActivity(openFinder);
			}

		});
    }

    public String getInstanceString(){
    	String result = "";
    	result += hm_cap_shape.get(((RadioButton) findViewById(rg1_cap_shape.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_cap_surface.get(((RadioButton) findViewById(rg2_cap_surface.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_cap_color.get(((RadioButton) findViewById(rg3_cap_color.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_bruises.get(((RadioButton) findViewById(rg4_bruises.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_odor.get(((RadioButton) findViewById(rg5_odor.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_gill_attachment.get(((RadioButton) findViewById(rg6_gill_attachment.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_gill_spacing.get(((RadioButton) findViewById(rg7_gill_spacing.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_gill_size.get(((RadioButton) findViewById(rg8_gill_size.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_gill_color.get(((RadioButton) findViewById(rg9_gill_color.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_stalk_shape.get(((RadioButton) findViewById(rg10_stalk_shape.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_stalk_root.get(((RadioButton) findViewById(rg11_stalk_root.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_stalk_surface_above.get(((RadioButton) findViewById(rg12_stalk_surface_above.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_stalk_surface_below.get(((RadioButton) findViewById(rg13_stalk_surface_below.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_stalk_color_above.get(((RadioButton) findViewById(rg14_stalk_color_above.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_stalk_color_below.get(((RadioButton) findViewById(rg15_stalk_color_below.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_veil_type.get(((RadioButton) findViewById(rg16_veil_type.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_veil_color.get(((RadioButton) findViewById(rg17_veil_color.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_ring_number.get(((RadioButton) findViewById(rg18_ring_number.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_ring_type.get(((RadioButton) findViewById(rg19_ring_type.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_spore_print_color.get(((RadioButton) findViewById(rg20_spore_print_color.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_population.get(((RadioButton) findViewById(rg21_population.getCheckedRadioButtonId())).getText()) + ",";
    	result += hm_habitat.get(((RadioButton) findViewById(rg22_habitat.getCheckedRadioButtonId())).getText()); 	
    	return result;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}