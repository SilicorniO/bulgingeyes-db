package com.silicornio.bulgingeyes.db.DBRequest;

public class DBRequestOperator {

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
	
	public DBRequestOperator(String attribute, String symbol, String value){
		this.attribute = attribute;
		this.symbol = symbol;
		this.value = value;
	}
	
	@Override
	public DBRequestOperator clone(){
		return new DBRequestOperator(attribute, symbol, value);
	}
	
}
