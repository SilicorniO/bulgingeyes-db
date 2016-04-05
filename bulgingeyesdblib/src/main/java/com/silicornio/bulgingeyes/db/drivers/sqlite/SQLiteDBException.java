package com.silicornio.bulgingeyes.db.drivers.sqlite;

@SuppressWarnings("serial")
public class SQLiteDBException extends Exception{
	
	public SQLiteDBException(String text){
		super(text);
	}
	
	public static SQLiteDBException create(String text){
		return new SQLiteDBException(text);
	}
}
