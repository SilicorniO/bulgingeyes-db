package com.silicornio.googlyeyes.dband;

import java.util.Arrays;

public class GEModelObject {
	
	//Actions allowed
	public static final String ACTION_CREATE = "CREATE";
	public static final String ACTION_UPDATE = "UPDATE";
	public static final String ACTION_DELETE = "DELETE";

	/** Name of the object **/
	public String name;
	
	/** List of attributes of this item **/
	public GEModelObjectAttribute[] attributes;

	/** Action allowed for this object in the database **/
	public String dbAction = ACTION_CREATE;

	@Override
	public String toString() {
		return "GEModelObject{" +
				"name='" + name + '\'' +
				", attributes=" + Arrays.toString(attributes) +
				", dbAction='" + dbAction + '\'' +
				'}';
	}
}
