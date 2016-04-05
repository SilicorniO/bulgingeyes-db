package com.silicornio.bulgingeyes.db.DBRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBResponse {

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
	
	public static DBResponse generateInfo(String info){
		DBResponse response = new DBResponse();
		response.info = info;
		return response;
	}
	
	public static DBResponse generateErrorDB(String errorDB){
		DBResponse response = new DBResponse();
		response.errorDB = errorDB;
		return response;
	}
	
	public void clean(){
		if(numResults==1){
			result = results.get(0);
			results.clear();
		}
	}
}
