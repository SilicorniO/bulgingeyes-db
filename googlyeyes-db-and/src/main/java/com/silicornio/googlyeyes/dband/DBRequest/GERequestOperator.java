package com.silicornio.googlyeyes.dband.dbrequest;

public class GERequestOperator {

	//SPECIAL SYMBOLS
	public static final String SYMBOL_ORDER = "ORDER";
	public static final String SYMBOL_LIMIT = "LIMIT";
	
	//ORDERs
	public static final String ORDER_ASCENDENCE = "ASC";
	public static final String ORDER_DESCENDENCE = "DESC";
	
	//LIMITs
	
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
	
	@Override
	public GERequestOperator clone(){
		return new GERequestOperator(attribute, symbol, value);
	}
	
}
