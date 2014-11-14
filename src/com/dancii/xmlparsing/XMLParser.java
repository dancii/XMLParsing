package com.dancii.xmlparsing;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by dancii on 14-11-10.
 */
public class XMLParser {

    private static final String filename = "currencies";
    private FileWriter fileWriter= null;
    private static Context context = null;
    private String dateNow = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

    public XMLParser(Context context){
        this.context=context;
    }

    public void parse(InputStream in) throws XmlPullParserException, IOException{
        try{

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readFeed(parser);
        }finally{
            in.close();
        }
    }

    private void readFeed(XmlPullParser parser)throws XmlPullParserException, IOException{
    	
        String currency=null;
        String rate=null;
        String txtSaveStr="";
        
        

        while(parser.next() != XmlPullParser.END_DOCUMENT){
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }

            String name = parser.getName();

            if(name.equals("Cube")){

                if(parser.getAttributeCount() >= 2){
                    currency=parser.getAttributeValue(null, "currency");
                    rate=parser.getAttributeValue(null, "rate");
                    txtSaveStr+=currency+","+rate+",";
                }

            }else{
                skip(parser);
            }
        }
        txtSaveStr+=dateNow;
        save(txtSaveStr);
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException{
        if(parser.getEventType() != XmlPullParser.START_TAG){
            throw new IllegalStateException();
        }

        int depth=1;
        while(depth != 0){
            switch (parser.next()){
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
    public void save(String data){
        try {
        	fileWriter = new FileWriter(new File(context.getFilesDir(),filename));
        	fileWriter.write(data);
            fileWriter.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
