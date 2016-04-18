package com.silicornio.googlyeyes.dband.dbrequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GEResponse {

	/** Num of object of the response **/
	public int numResults;
	
	/** Result **/
	public Map<String, Object> result;
	
	/** List of results when they are more than one **/
	public List<Map<String, Object>> results = new ArrayList<>();
	
	/** Info of a problem **/
	public String info;
	
	/** Error message of the database **/
	public String errorDB;
	
	public static GEResponse generateInfo(String info){
		GEResponse response = new GEResponse();
		response.info = info;
		return response;
	}
	
	public static GEResponse generateErrorDB(String errorDB){
		GEResponse response = new GEResponse();
		response.errorDB = errorDB;
		return response;
	}
	
	public void clean(){
		if(numResults==1){
			result = results.get(0);
			results.clear();
		}
	}

	@Override
	public String toString() {
		return "DBResponse{" +
				"numResults=" + numResults +
				", result=" + result +
				", results=" + results +
				", info='" + info + '\'' +
				", errorDB='" + errorDB + '\'' +
				'}';
	}
}
