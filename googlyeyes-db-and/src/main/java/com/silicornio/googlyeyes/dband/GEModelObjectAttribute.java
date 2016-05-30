package com.silicornio.googlyeyes.dband;

public class GEModelObjectAttribute {

	//TYPES
	public static final String TYPE_STRING = "string";
	public static final String TYPE_BOOLEAN = "boolean";
	public static final String TYPE_INTEGER = "integer";
	public static final String TYPE_DOUBLE = "double";
	public static final String TYPE_DATE = "date";

	//Default formats
	public static final String FORMAT_DATE_DEFAULT = "yyyy-MM-dd HH:mm:ss.SSS";
	
	//ENCRYPTION
	public static final String ENCRYPT_DEFAULT = "default";
		
	/** Name of the attribute/variable **/
	public String name;
	
	/** Type of attribute **/
	public String type;

	/** Format to apply **/
	public String format;
	
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

	/**
	 * Indicate if the type of the attribute is an object of the model
	 * @return boolean TRUE if it is an object, false if not
	 */
	public boolean isObjectType(){
		return !type.equalsIgnoreCase(TYPE_STRING) && !type.equalsIgnoreCase(TYPE_INTEGER)  &&
				!type.equalsIgnoreCase(TYPE_DOUBLE) && !type.equalsIgnoreCase(TYPE_DATE) &&
				!type.equalsIgnoreCase(TYPE_BOOLEAN);
	}
}
