package com.silicornio.googlyeyes.dband;

import com.silicornio.googlyeyes.dband.general.GECryptLib;

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

	//----- ADDITIONAL -----

	public void clean(){
		if(results.size()==1){
			result = results.get(0);
			results.clear();
		}
	}

	/**
	 * Apply decryption to all fields of this response
	 * @param objects GEModelObject[] array of model objects
	 * @param request GERequest to get model information
	 * @param cryptLib GECryptLib to use for encrypting
     */
	public void applyDecryption(GEModelObject[] objects, GERequest request, GECryptLib cryptLib){

		//get the model
		GEModelObject mObject = GEModelFactory.findObject(request.modelObject, objects);
		if(mObject==null){
			return;
		}

		//for each operator, check if the attribute has to be encrypted
		for(Map<String, Object> result : results){
			for(GEModelObjectAttribute moa : mObject.attributes){
				if(moa.encrypt!=null && result.get(moa.name)!=null && result.get(moa.name) instanceof String){
					if(moa.encrypt.equalsIgnoreCase(GEModelObjectAttribute.ENCRYPT_DEFAULT)){
						result.put(moa.name, cryptLib.decrypt((String)result.get(moa.name)));
					}
				}
			}
		}
	}
}
