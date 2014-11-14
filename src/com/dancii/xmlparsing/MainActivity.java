package com.dancii.xmlparsing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends Activity {


    private static final String url="http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private ArrayList<String> currencyStr=new ArrayList<String>();
    private ArrayList<Double> rateDouble=null;
    private Spinner spinnerOne,spinnerTwo;
    private InputStream inputStream=null;
    private StringBuffer storedString = new StringBuffer();
    private String formatDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    private TextView txtViewFromCurr, txtViewToCurr, txtViewCurrValue;
    private Button button;
    private EditText editTextCurr;

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
        
        loadPage();
        
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
    	String[] tempArray=null;
    	Date dateNow=new Date();
    	Date dateFromFile=null;
    	
    	tempArray=readFromFile();
    	
    	try {
			dateFromFile=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(tempArray[tempArray.length-1]);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.e("ERROR DATE", e.toString());
		}
    	
    	long diffInMilli=dateNow.getTime()-dateFromFile.getTime();
    	
    	if(diffInMilli>=86400000/*24h in millisec*/){
    		loadPage();
    	}else{
    		Log.d("OUTPUT","Not yet!!");
    	}
    }

    public void getData(View view){
    	double value=0;
    	double fromCurrentCurrToEUR=0;
    	double fromEURToSelectedCurr=0;
    	
        txtViewFromCurr.setText(String.valueOf(spinnerOne.getSelectedItem()));
        txtViewToCurr.setText(String.valueOf(spinnerTwo.getSelectedItem()));
        
        
        if(editTextCurr.getText().toString().equals("")){
        	Toast.makeText(this, "You need to write a value to calculate", Toast.LENGTH_SHORT).show();
        }else{
        	value=Double.parseDouble(editTextCurr.getText().toString());
        	fromCurrentCurrToEUR=value/rateDouble.get(spinnerOne.getSelectedItemPosition());
        	fromEURToSelectedCurr=fromCurrentCurrToEUR*rateDouble.get(spinnerTwo.getSelectedItemPosition());
        	
        	txtViewCurrValue.setText(String.valueOf(fromEURToSelectedCurr));
        	
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadPage(){
        new DownloadXmlTask().execute(url);
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
        	Toast.makeText(this, "It seems that you dont have a saved file, try again later", Toast.LENGTH_SHORT).show();
        }catch(Exception e2){
        	Toast.makeText(this, "Some problem with reading from the file, try again later", Toast.LENGTH_SHORT).show();
        }
		return arrayFromFile;
    }
    
    private void getCurrencyFromSavedFile(){
    	currencyStr=new ArrayList<String>();
        rateDouble=new ArrayList<Double>();
        String[] tempArray=null;
        
        tempArray=readFromFile();
        currencyStr.add("EUR");
        rateDouble.add(1.0);
        
        for(int i=0;i<tempArray.length-1;i++){
            isDouble(tempArray[i]);
        }

        //System.out.println(storedString+","+formatDate);
		
    	
		try{
        	
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item, currencyStr);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerOne.setAdapter(adapter);
            spinnerTwo.setAdapter(adapter);
            spinnerOne.setOnItemSelectedListener(new SpinnerListener());
            spinnerTwo.setOnItemSelectedListener(new SpinnerListener());
        }catch(Exception e){
        	System.out.println("SPINNER ERROR: "+e);
        }
    }

    private void loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException{
        InputStream stream=null;
        XMLParser xmlParser=new XMLParser(this);
        currencyStr=new ArrayList<String>();
        rateDouble=new ArrayList<Double>();
        String[] tempArray=null;


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

    private void isDouble(String str){
        try{
            Double d = Double.parseDouble(str);
            rateDouble.add(d);
        }catch(NumberFormatException e){
            currencyStr.add(str);
        }
    }

}

class SpinnerListener implements AdapterView.OnItemSelectedListener{

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
        Toast.makeText(parent.getContext(), parent.getItemAtPosition(pos).toString()+" selected",Toast.LENGTH_SHORT).show();
    }

    public void onNothingSelected(AdapterView<?> parent){

    }
}
