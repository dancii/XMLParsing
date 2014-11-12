package com.dancii.xmlparsing;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {


    private static final String url="http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private List<Currency> currencies=null;
    private ArrayList<String> currencyStr=null;
    private ArrayList<Double> rateDouble=null;
    private Spinner spinnerOne,spinnerTwo;
    private InputStream inputStream=null;
    private StringBuffer storedString = new StringBuffer();
    private Date date=new Date();
    private String formatDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.btnGetData);
        loadPage();
    }

    public void getData(View view){
        String[] testArray=null;
        try{
            inputStream=openFileInput("currencies");
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            String strLine = null;
            BufferedReader bufferedReader=new BufferedReader(streamReader);
            if ((strLine = bufferedReader.readLine()) != null) {
                storedString.append(strLine);
                //System.out.println(strLine.split(",")[0]);
                //System.out.println(strLine.split(",")[1]);
                testArray=strLine.split(",");

            }
            for(int i=0;i<testArray.length;i++){
                System.out.println(testArray[i]);
            }

            //System.out.println(storedString+","+formatDate);

            storedString = new StringBuffer();
            bufferedReader.close();
            streamReader.close();
            inputStream.close();
        }catch(Exception e){
            System.out.println("Error: "+e);
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



            /*
                Get data from file, extract and push into spinners
            */

            try{
                inputStream=openFileInput("currencies");
                InputStreamReader streamReader = new InputStreamReader(inputStream);
                String strLine = null;
                BufferedReader bufferedReader=new BufferedReader(streamReader);
                if ((strLine = bufferedReader.readLine()) != null) {
                    storedString.append(strLine);
                    tempArray=strLine.split(",");
                }

                for(int i=0;i<tempArray.length;i++){
                    isDouble(tempArray[i]);
                }

                //System.out.println(storedString+","+formatDate);

                storedString = new StringBuffer();
                bufferedReader.close();
                streamReader.close();
                inputStream.close();
            }catch(Exception e){
                System.out.println("Error: "+e);
            }

            for(String currency : currencyStr){
                System.out.println(currency);
            }

            for(Double rate : rateDouble){
                System.out.println(rate);
            }

            
            runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
		            	spinnerOne = (Spinner) findViewById(R.id.currencySpinnerOne);
		                spinnerTwo = (Spinner) findViewById(R.id.currencySpinnerTwo);
		                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item, currencyStr);
		                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		                spinnerOne.setAdapter(adapter);
		                spinnerTwo.setAdapter(adapter);
		                spinnerOne.setOnItemSelectedListener(new SpinnerListener());
		            }catch(Exception e){
		            	System.out.println("SPINNER ERROR: "+e);
		            }
				}
			});
            
            
            




        }catch(Exception e){
            System.out.println("HTTP URL ERROR: "+e);
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
