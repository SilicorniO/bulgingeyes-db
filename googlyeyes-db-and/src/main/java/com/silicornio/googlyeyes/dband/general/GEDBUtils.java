package com.silicornio.googlyeyes.dband.general;


import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GEDBUtils {

    //------ COUNTERS ------

    private static Map<String,Long> counters = new HashMap<String, Long>();

    public static void startCounter(String tag){
        long currentTime = System.currentTimeMillis();
        counters.put(tag, currentTime);
    }

    public static long endCounter(String tag){
        if(counters.containsKey(tag)){
            return System.currentTimeMillis()-counters.get(tag);
        }else{
            return 0;
        }
    }

    //----- READING -----

    /**
     * Read a configuration object from assets
     * @param context Context to read assets
     * @param path String path inside the assets
     * @param confClass Class to generate with read JSON
     * @return T Object instance generated from the file given or NULL if there was an error
     */
    public static <T>T readConfObjectFromAssets(Context context, String path, Class<T> confClass){
        try{

            //read the text of the file
            String text = readInputStream(context.getAssets().open(path));
            if(text==null){
                GEL.e("Error reading file '" + path + "' from assets");
                return null;
            }

            //read the ModelConf
            Gson gson = new Gson();
            return gson.fromJson(text, confClass);

        }catch(Exception e){
            GEL.e("There was an error reading the file '" + path + "' as a " + confClass.getName());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert InputStream in a String
     * @param inputStream Inputstream to read
     * @return String converted
     */
    private static String readInputStream(InputStream inputStream){

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        try {

            while ((line = br.readLine()) != null) {
                total.append(line);
            }

            //return the read string
            return total.toString();

        }catch (IOException ioe){
            GEL.e("Exception reading inputstream: " + ioe.toString());
        }

        return null;
    }


    /**
     * Delete all the keys with null values of the map received
     * @param map Map<String, Object> map to clean
     */
    public static void cleanMapNullValues(Map<String, Object> map){

        //check map is null
        if(map==null){
            return;
        }

        for(String key : new ArrayList<>(map.keySet())){
            if(map.get(key)==null){
                map.remove(key);
            }
        }

    }
}
