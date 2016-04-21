package com.silicornio.googlyeyes.dband.dbrequest;


import com.silicornio.googlyeyes.dband.general.GECryptLib;
import com.silicornio.googlyeyes.dband.model.GEModelFactory;
import com.silicornio.googlyeyes.dband.model.GEModelObject;
import com.silicornio.googlyeyes.dband.model.GEModelObjectAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GERequest {
	
	//TYPES
	public static final String TYPE_RAW = "RAW";
	public static final String TYPE_ADD = "ADD";
	public static final String TYPE_GET = "GET";
	public static final String TYPE_UPDATE = "UPD";
	public static final String TYPE_DELETE = "DEL";

	/** Type of request **/
	public String type = TYPE_RAW;
	
	/** Object of the model to apply request **/
	public String modelObject;
	
	/** Text to execute directly **/
	public String raw;
	
	/** Array of operators to apply **/
	public List<GERequestOperator> operators = new ArrayList<>();

	/** Logic to use between operators **/
	public String operatorsLogic;
	
	/** List of attributes to set in the response **/
	public List<String> responseAttributes = new ArrayList<>();
	
	/** Object of type modelObject and filled **/ 
	public Map<String,Object> value = new HashMap<String, Object>();

	/** Flag to load or not the objects inside this one **/
	public boolean nestedObjects = false;
	
	public GERequest(String type, String modelObject){
		this.type = type;
		this.modelObject = modelObject;
	}
	
	/**
	 * Apply encryption to the attributes of the request that needs it
	 * @param objects ModelObject[] Array of objects where to search the model associated to this request
	 * @param password String to use for encrypting
	 */
	public void applyEncryption(GEModelObject[] objects, String password){
				
		//get the model
		GEModelObject mObject = GEModelFactory.findObject(modelObject, objects);
		if(mObject==null){
			return;
		}
		
		//for each operator, check if the attribute has to be encrypted
		if(value!=null){
			for(String key : value.keySet()){
				if(value.get(key) instanceof String){
					String valueEncrypted = getValueEncrypted(key, (String)value.get(key), password, mObject.attributes);
					if(valueEncrypted!=null){
						value.put(key, valueEncrypted);
					}
				}
			}
		}
		
		//check operators
		if(operators!=null){
			for(GERequestOperator op : operators){
				String valueEncrypted = getValueEncrypted(op.attribute, op.value, password, mObject.attributes);
				if(valueEncrypted!=null){
					op.value = valueEncrypted;
				}
			}
		}
	}
	
	/**
	 * Get the value with encryption if it is found and has encryption enabled
	 * @param attrName String name of the attribute to search in the list of attributes of the model
	 * @param value String value to encrypt
	 * @param password String to use for encruption
	 * @param attributes ModelObjectAttribute[] Array of attributes of the model where the attrName should be found
	 * @return String value encrypted or NULL if not necessary encryption or not found
	 */
	private static String getValueEncrypted(String attrName, String value, String password, GEModelObjectAttribute[] attributes){
		
		//check values are right
		if(attrName==null || value==null){
			return null;
		}
		
		for(GEModelObjectAttribute attr : attributes){
			if(attrName.equals(attr.name)){
				if(attr.encrypt!=null && attr.encrypt.equalsIgnoreCase(GEModelObjectAttribute.ENCRYPT_DEFAULT)){
					
					//encrypt value 
					return GECryptLib.encrypt(value, password);
					
				}
				
				//it was found but not necessary encryption
				return null;
			}
		}
		
		//not found
		return null;
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public GERequest clone(){
		GERequest request = new GERequest(type, modelObject);
		if(operators!=null){
			for(GERequestOperator op : operators){
				request.operators.add(op.clone());
			}
		}
		if(value!=null){
			request.value = new HashMap<String, Object>(value);
		}
		if(responseAttributes!=null){
			request.responseAttributes = new ArrayList<String>(responseAttributes);
		}
		return request;
	}
}
