package com.silicornio.bulgingeyes.db.DBRequest;

import com.silicornio.bulgingeyes.db.general.CryptLib;
import com.silicornio.bulgingeyes.db.model.ModelFactory;
import com.silicornio.bulgingeyes.db.model.ModelObject;
import com.silicornio.bulgingeyes.db.model.ModelObjectAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBRequest {
	
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
	public List<DBRequestOperator> operators = new ArrayList<>();
	
	/** List of attributes to set in the response **/
	public List<String> responseAttributes = new ArrayList<>();
	
	/** Object of type modelObject and filled **/ 
	public HashMap<String,Object> value = new HashMap<String, Object>();
	
	public DBRequest(String type, String modelObject){
		this.type = type;
		this.modelObject = modelObject;
	}
	
	/**
	 * Apply encryption to the attributes of the request that needs it
	 * @param objects ModelObject[] Array of objects where to search the model associated to this request
	 * @param password String to use for encrypting
	 */
	public void applyEncryption(ModelObject[] objects, String password){
				
		//get the model
		ModelObject mObject = ModelFactory.findObject(modelObject, objects);
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
			for(DBRequestOperator op : operators){
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
	private static String getValueEncrypted(String attrName, String value, String password, ModelObjectAttribute[] attributes){
		
		//check values are right
		if(attrName==null || value==null){
			return null;
		}
		
		for(ModelObjectAttribute attr : attributes){
			if(attrName.equals(attr.name)){
				if(attr.encrypt!=null && attr.encrypt.equalsIgnoreCase(ModelObjectAttribute.ENCRYPT_DEFAULT)){
					
					//encrypt value 
					return CryptLib.encrypt(value, password);
					
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
	public DBRequest clone(){
		DBRequest request = new DBRequest(type, modelObject);
		if(operators!=null){
			for(DBRequestOperator op : operators){
				request.operators.add(op.clone());
			}
		}
		if(value!=null){
			request.value = (HashMap<String, Object>) value.clone();
		}
		if(responseAttributes!=null){
			request.responseAttributes = new ArrayList<String>(responseAttributes);
		}
		return request;
	}
}
