package com.silicornio.googlyeyes.dband;

public class GERequestOperator {

	//SPECIAL SYMBOLS
	public static final String SYMBOL_ORDER = "ORDER";
	public static final String SYMBOL_LIMIT = "LIMIT";
	
	//ORDERs
	public static final String ORDER_ASCENDENCE = "ASC";
	public static final String ORDER_DESCENDENCE = "DESC";
	
	//LIMITs

	//BOOLEAN
	public static final String VALUE_TRUE = "1";
	public static final String VALUE_FALSE = "0";
	
	/** Attribute to apply operation **/
	public String attribute;
	
	/** Operator to apply **/
	public String symbol;
	
	/** Value to compare **/
	public String value;
	
	public GERequestOperator(String attribute, String symbol, String value){
		this.attribute = attribute;
		this.symbol = symbol;
		this.value = value;
	}

	public String getAttribute() {
		return attribute;
	}

	@Override
	public GERequestOperator clone(){
		return new GERequestOperator(attribute, symbol, value);
	}
	
}
