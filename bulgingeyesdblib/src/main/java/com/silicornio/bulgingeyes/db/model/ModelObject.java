package com.silicornio.bulgingeyes.db.model;

public class ModelObject {
	
	//Actions allowed
	public static final String ACTION_CREATE = "CREATE";
	public static final String ACTION_UPDATE = "UPDATE";
	public static final String ACTION_DELETE = "DELETE";

	/** Name of the object **/
	public String name;
	
	/** List of attributes of this item **/
	public ModelObjectAttribute[] attributes;
	
	/** Action allowed for this object in the database **/
	public String dbAction = ACTION_CREATE;
		
}
