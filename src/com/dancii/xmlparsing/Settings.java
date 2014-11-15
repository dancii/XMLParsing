package com.dancii.xmlparsing;


import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity{

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
		finish();
	}
}
