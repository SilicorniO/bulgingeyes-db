package com.silicornio.googlyeyes.dband.model;

public class ModelObjectAttribute {

	//TYPES
	public static final String TYPE_STRING = "string";
	public static final String TYPE_INTEGER = "integer";
	public static final String TYPE_DOUBLE = "double";
	
	//ENCRYPTION
	public static final String ENCRYPT_DEFAULT = "default";
		
	/** Name of the attribute/variable **/
	public String name;
	
	/** Type of attribute **/
	public String type;
	
	/** Length of the attribute, if it is not setted or 0 then the length will be unlimited **/
	public int length = 0;
	
	/** TRUE if it is the main identifier for database **/
	public boolean id = false;
	
	/** TRUE for autoincrement in database **/
	public boolean autoincrement = false;
	
	/** TRUE if it is mandatory to set the value before add this model object **/
	public boolean mandatory = false;
	
	/** TRUE if cannot be repeated by other model object **/
	public boolean unique = false;
	
	/** Default value to set **/
	public String defaultValue;
	
	/** Encryption default for this attribute **/
	public String encrypt = null;

	/** Indicate that this value is used to save JSON of object **/
	public boolean objectJson = false;
	
}
