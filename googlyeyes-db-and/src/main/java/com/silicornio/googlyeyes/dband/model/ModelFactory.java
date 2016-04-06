package com.silicornio.googlyeyes.dband.model;

import com.silicornio.googlyeyes.dband.general.GEL;

public class ModelFactory {

	/**
	 * Read the model.conf file
	 * @param path String where the file is located
	 * @return ModelConf generated
	 */
	public static ModelConf readModelObjects(String path){
		
//		//get the file into a string
//		String sFile = FileReader.fileToString(path);
//		if(sFile==null){
//			JEL.e("Path of the model file is wrong");
//			return null;
//		}
//
//		//read the ModelConf
//		Gson gson = new Gson();
		return null;//gson.fromJson(sFile, ModelConf.class);
	}
	
	/**
	 * Find the attribute received in text
	 * @param sAttribute String with the attribute path. ex: Message.text
	 * @param models ModelObject[] array of models where to search
	 * @return ModelObjectAttribute if was found or null
	 */
	public static ModelObjectAttribute findAttribute(String sAttribute, ModelObject[] models){
	
		//separate the text into object and attribute
		String[] aSAttribute = sAttribute.split("\\.");
		if(aSAttribute.length!=2){
			GEL.e("ERROR syntax in attribute reference: " + sAttribute);
			return null;
		}
		
		//find the object
		ModelObject m = findObject(aSAttribute[0], models);
		if(m!=null){
			ModelObjectAttribute ma = findAttribute(aSAttribute[1], m);
			if(ma!=null){
				return ma;
			}else{
				//not found in this object
				GEL.e("ERROR finding attribute reference: " + sAttribute);
				return null;
			}
		}
		
		//not found
		GEL.e("ERROR finding object attribute reference: " + sAttribute);
		return null;
	}
	
	/**
	 * Find the attribute inside of a model
	 * @param nameAttribute String name of the attribute
	 * @param model ModelObject where to search
	 * @return ModelObjectAttribute found or null if it doesn't exists in the model
	 */
	public static ModelObjectAttribute findAttribute(String nameAttribute, ModelObject model){
		
		for(ModelObjectAttribute ma : model.attributes){
			if(ma.name.equalsIgnoreCase(nameAttribute)){
				return ma;
			}
		}
		
		//not found
		return null;
	}
	
	/**
	 * Find the attribute inside of a model that is ID
	 * @param model ModelObject where to search
	 * @return ModelObjectAttribute found or null if it doesn't exists in the model
	 */
	public static ModelObjectAttribute findAttributeId(ModelObject model){
		
		for(ModelObjectAttribute ma : model.attributes){
			if(ma.id){
				return ma;
			}
		}
		
		//not found
		return null;
	}

	/**
	 * Find the attribute inside of a model that is ObjectJson
	 * @param model ModelObject where to search
	 * @return ModelObjectAttribute found or null if it doesn't exists in the model
	 */
	public static ModelObjectAttribute findAttributeObjectJson(ModelObject model){

		for(ModelObjectAttribute ma : model.attributes){
			if(ma.objectJson){
				return ma;
			}
		}

		//not found
		return null;
	}
	
	/**
	 * Find the object inside of a list of models
	 * @param nameObject String name of the attribute
	 * @param models ModelObject[] where to search
	 * @return ModelObject found or null if it doesn't exists in models
	 */
	public static ModelObject findObject(String nameObject, ModelObject[] models){
		
		//find the object
		for(ModelObject m : models){
			if(m.name.equalsIgnoreCase(nameObject)){
				return m;
			}
		}
		
		//not found
		return null;
	}
		
}
