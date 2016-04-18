package com.silicornio.googlyeyes.dband.model;

public class GEModelFactory {

	/**
	 * Read the model.conf file
	 * @param path String where the file is located
	 * @return ModelConf generated
	 */
	public static GEModelConf readModelObjects(String path){
		
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
	 * Find the attribute with the ID of the object given
	 * @param sModel String with the name of the model
	 * @param models ModelObject[] array of models where to search
	 * @return ModelObjectAttribute if was found or null
	 */
	public static GEModelObjectAttribute findAttributeId(String sModel, GEModelObject[] models){

		//get the model reference
		GEModelObject moRef = GEModelFactory.findObject(sModel, models);
		if(moRef!=null) {

			//get the attribute with the identifier
			return GEModelFactory.findAttributeId(moRef);
		}

		return null;
	}

	/**
	 * Find the attribute inside of a model
	 * @param nameAttribute String name of the attribute
	 * @param model ModelObject where to search
	 * @return ModelObjectAttribute found or null if it doesn't exists in the model
	 */
	public static GEModelObjectAttribute findAttribute(String nameAttribute, GEModelObject model){
		
		for(GEModelObjectAttribute ma : model.attributes){
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
	public static GEModelObjectAttribute findAttributeId(GEModelObject model){
		
		for(GEModelObjectAttribute ma : model.attributes){
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
	public static GEModelObjectAttribute findAttributeObjectJson(GEModelObject model){

		for(GEModelObjectAttribute ma : model.attributes){
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
	public static GEModelObject findObject(String nameObject, GEModelObject[] models){

		//find the object
		for(GEModelObject m : models){
			if(m.name.equalsIgnoreCase(nameObject)){
				return m;
			}
		}
		
		//not found
		return null;
	}

}
