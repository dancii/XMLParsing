package com.dancii.xmlparsing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {


    private static final String url="http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private static final String STATE_TEXTVIEW_CURR_CALC = "txtViewCurrCalc";
    private static final String STATE_SPINNER_COLOR = "spinnerColor";
    private static final String STATE_SPINNER1_POS = "spinnerOnePos";
    private static final String STATE_SPINNER2_POS = "spinnerTwoPos";
    private ArrayList<String> currencyStr=new ArrayList<String>();
    private ArrayList<Double> rateDouble=null;
    private Spinner spinnerOne,spinnerTwo;
    private InputStream inputStream=null;
    private StringBuffer storedString = new StringBuffer();
    private TextView txtViewFromCurr, txtViewToCurr, txtViewCurrValue;
    private Button button;
    private EditText editTextCurr;
    private AsyncTask<String, Void, Void> downloadXMLTask=null;
    private SharedPreferences myPreferences;
    Random rand=new Random();
    String[] tempArray=null;
    private int[] backgroundColors=null;
    private int viewBackgroundColor=0;
    private int spinnerOnePos=0;
    private int spinnerTwoPos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        button = (Button) findViewById(R.id.btnGetData);
        spinnerOne = (Spinner) findViewById(R.id.currencySpinnerOne);
        spinnerTwo = (Spinner) findViewById(R.id.currencySpinnerTwo);
        txtViewFromCurr = (TextView) findViewById(R.id.txtViewFromCurr);
        txtViewToCurr = (TextView) findViewById(R.id.txtViewToCurr);
        editTextCurr = (EditText) findViewById(R.id.editTxtFromCurrValue);
        txtViewCurrValue = (TextView) findViewById(R.id.txtViewToCurrValue);
        
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        viewBackgroundColor=Integer.parseInt(myPreferences.getString("prefChangeBackgroundColor", "4"));
        changeViewBackgroundColor(viewBackgroundColor);
        
        if(savedInstanceState != null){
        	txtViewCurrValue.setText(savedInstanceState.getString(STATE_TEXTVIEW_CURR_CALC));
        	backgroundColors=savedInstanceState.getIntArray(STATE_SPINNER_COLOR);
        	spinnerOnePos=savedInstanceState.getInt(STATE_SPINNER1_POS);
        	spinnerTwoPos=savedInstanceState.getInt(STATE_SPINNER2_POS);
        	getCurrencyFromSavedFile();
        }
           
    }
    
    //Code taken from http://developer.android.com/training/basics/activity-lifecycle/recreating.html
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
    	savedInstanceState.putString(STATE_TEXTVIEW_CURR_CALC, txtViewCurrValue.getText().toString());
    	
    	savedInstanceState.putIntArray(STATE_SPINNER_COLOR, backgroundColors);
    	savedInstanceState.putInt(STATE_SPINNER1_POS, spinnerOne.getSelectedItemPosition());
    	savedInstanceState.putInt(STATE_SPINNER2_POS, spinnerTwo.getSelectedItemPosition());
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    
    //If activity is in the background, cancel the asynctask
    @Override
    public void onPause(){
    	super.onPause();
    	
    	if(downloadXMLTask!=null){
    		downloadXMLTask.cancel(true);
    		downloadXMLTask=null;
    	}
    }
    
    //If activity is resumed, load in the xml because it got interrupted or old version of xml
    @Override
    public void onResume(){
    	super.onResume();
    	
    	
    	if(downloadXMLTask==null){
    		loadPage();
    	}
    	
    	
    }
    
    //Checks if the file is x hours older to update the xml file, else get it from the file
    @Override
    protected void onStart(){
    	super.onStart();
    	tempArray=null;
    	Date dateNow=new Date();
    	Date dateFromFile=null;
    	
    	viewBackgroundColor=Integer.parseInt(myPreferences.getString("prefChangeBackgroundColor", "4"));
        changeViewBackgroundColor(viewBackgroundColor);
    	
    	tempArray=readFromFile();
    	if(tempArray==null){
    		loadPage();
    	}else{
    		try {
        		
    			dateFromFile=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(tempArray[tempArray.length-1]);
    			
    			long diffInMilli=dateNow.getTime()-dateFromFile.getTime();
    	    	
    	    	if(diffInMilli>=Integer.parseInt(myPreferences.getString("prefUpdateInter", "1"))*24*60*60*1000/*In milliseconds*/){
    	    		loadPage();
    	    	}else{
    	    		getCurrencyFromSavedFile();
    	    	}
    		}catch(Exception e){
    			Log.e("ERROR DATE", e.toString());
    			loadPage();
    		}
    	}
    	
    }
    
    private void changeViewBackgroundColor(int color){
    	View view = this.getWindow().getDecorView();
    	switch(color){
    	case 1:
    		view.setBackgroundColor(Color.BLACK);
    		break;
    	case 2:
    		view.setBackgroundColor(Color.BLUE);
    		break;
    	case 3:
    		view.setBackgroundColor(Color.GREEN);
    		break;
    	case 4:
    		view.setBackgroundColor(Color.WHITE);
    		break;
    	}
    }
    
    //Button method
    public void getData(View view){
    	if(view.getId()!=R.id.btnGetData){
    		
    	}else{
    		double value=0;
        	double fromCurrentCurrToEUR=0;
        	double fromEURToSelectedCurr=0;
        	
            
            
            if(editTextCurr.getText().toString().equals("")){
            	Toast.makeText(this, "You need to write a value to calculate", Toast.LENGTH_SHORT).show();
            }else{
            	value=Double.parseDouble(editTextCurr.getText().toString());
            	fromCurrentCurrToEUR=value/rateDouble.get(spinnerOne.getSelectedItemPosition());
            	fromEURToSelectedCurr=fromCurrentCurrToEUR*rateDouble.get(spinnerTwo.getSelectedItemPosition());
            	
            	txtViewCurrValue.setText(String.valueOf(fromEURToSelectedCurr));
            	
            }
    	}

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.add(0,0,0, "Settings");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	switch(item.getItemId()){
		case 0:
			Intent intent = new Intent(this,Settings.class);
			startActivity(intent);
			return true;
		}
        return super.onOptionsItemSelected(item);
    }

    //Starts the asynctask
    public void loadPage(){
    	downloadXMLTask= new DownloadXmlTask().execute(url);
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            try{
               loadXmlFromNetwork(urls[0]);
                return null;
            }catch(Exception e){
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(Void v) {
        	
        /******
            Get data from file, extract and push into spinners
    	******/
        	getCurrencyFromSavedFile();
        }
    }
    
    //Method to read from the file and return all the values like an array
    private String[] readFromFile(){
    	String[] arrayFromFile=null;
        
    	try{
            inputStream=openFileInput("currencies");
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            String strLine = null;
            BufferedReader bufferedReader=new BufferedReader(streamReader);
            if ((strLine = bufferedReader.readLine()) != null) {
                storedString.append(strLine);
                arrayFromFile=strLine.split(",");
            }
            
            storedString = new StringBuffer();
            bufferedReader.close();
            streamReader.close();
            inputStream.close();
        }catch(FileNotFoundException e1){
        	Toast.makeText(this, "It seems that you dont have a saved file, will try to download from the web", Toast.LENGTH_SHORT).show();
        }catch(Exception e2){
        	Toast.makeText(this, "Some problem with reading from the file, try again later", Toast.LENGTH_SHORT).show();
        }
		return arrayFromFile;
    }
    
    //Splits the valus from the string array to two different arraylists and customspinners.
    private void getCurrencyFromSavedFile(){
    	currencyStr=new ArrayList<String>();
        rateDouble=new ArrayList<Double>();
        tempArray=null;
        
        tempArray=readFromFile();
        
        for(int i=0;i<tempArray.length-1;i++){
            isDouble(tempArray[i]);
        }
        
        
        if(backgroundColors==null){
        	backgroundColors=new int[currencyStr.size()];
            for(int i=0;i<currencyStr.size();i++){
            	backgroundColors[i]=Color.argb(255,rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            }
        }

		try{
            spinnerOne.setAdapter(new CustomAdapter(this,R.layout.custom_spinner,currencyStr));
            spinnerTwo.setAdapter(new CustomAdapter(this,R.layout.custom_spinner,currencyStr));
            spinnerOne.setOnItemSelectedListener(new SpinnerListener());
            spinnerTwo.setOnItemSelectedListener(new SpinnerListener());
            spinnerOne.setSelection(spinnerOnePos);
            spinnerTwo.setSelection(spinnerTwoPos);
            
        }catch(Exception e){
        	System.out.println("SPINNER ERROR: "+e);
        }
    }
    
    public class CustomAdapter extends ArrayAdapter<String>{
    	ArrayList<String> currencyList=new ArrayList<String>();
    	public CustomAdapter(Context context, int textViewResourceId, ArrayList<String> data){
    		super(context, textViewResourceId, data);
    		currencyList=data;
    	}
    	
    	@Override
    	public View getDropDownView(int position, View convertView, ViewGroup parent){
    		return getCustomView(position, convertView, parent);
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent){
    		return getCustomView(position, convertView, parent);
    	}
    	
    	public View getCustomView(int position, View convertView, ViewGroup parent){
    		LayoutInflater inflater=getLayoutInflater();
    		View row = inflater.inflate(R.layout.custom_spinner, parent, false);
    		row.setBackgroundColor(backgroundColors[position]);
    		
    		
    		TextView txtCurrency = (TextView) row.findViewById(R.id.txtViewCurrency);
    		txtCurrency.setText(currencyList.get(position));
    		
    		return row;
    	}
    }

    
    //The asynctask calls this method, gets the xml from internet, if user dont have internet or the website is down then get from file if exists 
    private void loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException{
        InputStream stream=null;
        XMLParser xmlParser=new XMLParser(this);
        currencyStr=new ArrayList<String>();
        rateDouble=new ArrayList<Double>();
        tempArray=null;


        try{
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            stream = conn.getInputStream();

            xmlParser.parse(stream);
    

        }catch(Exception e){
        	
        	//System.out.println("HTTP URL ERROR: "+e);
        	runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(MainActivity.this)
		            .setTitle("Internet connection error/site down")
		            .setMessage("Check you internet connection, it seems that you are not connected or the webpage is down\n" +
		            		"Would you like an older version of currency rate?")
		            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) { 
		                	getCurrencyFromSavedFile();
		                }
		             })
		            .setNegativeButton("No", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) { 
		                    
		                }
		             }).show();
				}
			});
        	
        	
        }finally{
            if(stream!=null){
                stream.close();
            }
        }


    }

    //Method to check if the array value from the parsed xml is a double or string
    private void isDouble(String str){
        try{
            Double d = Double.parseDouble(str);
            rateDouble.add(d);
        }catch(NumberFormatException e){
            currencyStr.add(str);
        }
    }
    
    private class SpinnerListener implements AdapterView.OnItemSelectedListener{

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
            
            if(parent==spinnerOne){
            	txtViewFromCurr.setText(String.valueOf(spinnerOne.getSelectedItem()));
            }else{
            	txtViewToCurr.setText(String.valueOf(spinnerTwo.getSelectedItem()));
            }
            
        }

        public void onNothingSelected(AdapterView<?> parent){

        }
    }

}