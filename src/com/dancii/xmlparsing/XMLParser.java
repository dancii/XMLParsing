package com.dancii.xmlparsing;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dancii on 14-11-10.
 */
public class XMLParser {

    private static final String ns=null;
    private static final String filename="currencies";
    FileOutputStream outputStream=null;
    Context context =null;

    public XMLParser(Context context){
        this.context=context;
    }

    public List parse(InputStream in) throws XmlPullParserException, IOException{
        try{

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        }finally{
            in.close();
        }
    }

    private ArrayList<Currency> readFeed(XmlPullParser parser)throws XmlPullParserException, IOException{

        ArrayList<Currency> currencies = new ArrayList<Currency>();
        Currency currencyObj=null;
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

                    try{
                        currencyObj=new Currency(currency,Double.parseDouble(rate));
                        currencies.add(currencyObj);
                    }catch(Exception e){
                        System.out.println("ERROR: "+e);
                    }
                }

            }else{
                skip(parser);
            }
        }
        save(txtSaveStr);
        return currencies;
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
            outputStream = context.openFileOutput(filename, context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
