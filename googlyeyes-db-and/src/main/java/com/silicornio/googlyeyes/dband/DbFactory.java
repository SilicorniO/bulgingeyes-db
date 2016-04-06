package com.silicornio.googlyeyes.dband;

import android.content.Context;

import com.google.gson.Gson;
import com.silicornio.googlyeyes.dband.general.GEL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DbFactory {

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
	
}
