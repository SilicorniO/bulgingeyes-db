package com.silicornio.bulgingeyes.db.drivers.sqlite;


import com.silicornio.bulgingeyes.db.DBRequest.DBRequest;
import com.silicornio.bulgingeyes.db.DBRequest.DBRequestOperator;
import com.silicornio.bulgingeyes.db.general.JEL;
import com.silicornio.bulgingeyes.db.model.ModelFactory;
import com.silicornio.bulgingeyes.db.model.ModelObject;
import com.silicornio.bulgingeyes.db.model.ModelObjectAttribute;

import java.util.List;

public class SQLiteDBStGenerator {

	/**
	 * Generate the statement to create a table
	 * @param model ModelObject to generate the table
	 * @param models ModelObject[] array of models where to find other attributes
	 * @return String with statement
	 */
	protected static String statementCreate(ModelObject model, ModelObject[] models){
		
		String sStatement = "CREATE TABLE " + model.name + "(";
		String sId = "";
		for(int i=0,numIds=0; i<model.attributes.length; i++){
			ModelObjectAttribute attr = model.attributes[i];
			
			//name
			sStatement += attr.name + " ";
			
			//type
			sStatement += statementColumn(attr, models);
			
			//key
			if(attr.id){
				
				//generate the primary key statement
				if(numIds==0){
					sId = ",PRIMARY KEY(" + attr.name;
				}else{
					sId = "," + attr.name;
				}
				
				//increment the number of IDs added
				numIds++;
				
			}
			
			//end
			if(i<model.attributes.length-1){
				sStatement += ",";
			}else{
				//end the sId statement
				if(sId.length()>0){
					sId += ")";
				}
			}
		}
		
		sStatement += sId + ")";
		
		return sStatement;
	}
	
	/**
	 * Generate the statement to drop a table
	 * @param model ModelObject to drop the table
	 * @return String with statement
	 */
	protected static String statementDrop(ModelObject model){
		return "DROP TABLE " + model.name;
	}
	
	/**
	 * Generate the column data to set in the statement
	 * @param attr ModelObjectAttribute with all information to generate
	 * @param models ModelObject[] array of models where to find other attributes
	 * @return String with piece of statement with data of the column
	 */
	protected static String statementColumn(ModelObjectAttribute attr, ModelObject[] models){
		
		String sStatement = "";
						
		//type
		String sStatementType = statementType(attr.type, attr.length);
		if(sStatementType==null){
			//find the type referred
			ModelObjectAttribute attrRef = ModelFactory.findAttribute(attr.type, models);
			if(attrRef==null){
				return null;
			}
			
			//add the statement type
			sStatementType = statementType(attrRef.type, attrRef.length);
			if(sStatementType==null){
				return null;
			}
		}
		sStatement += sStatementType + " ";
		
		//auto increment
		if(attr.autoincrement){
			sStatement += "AUTO_INCREMENT ";
		}
		
		//unique
		if(attr.unique){
			sStatement += "UNIQUE ";
		}
		
		//key
		if(!attr.id){
		
			//mandatory
			if(attr.mandatory){
				sStatement += "NOT NULL ";
			}
			
			//default value
			if(attr.defaultValue!=null){
				sStatement += "DEFAULT '" + attr.defaultValue + "'";
			}
		}
		
		return sStatement;
	}
	
	/**
	 * Generate the type to set in the statement
	 * @param type String type to set
	 * @param length int of the variable
	 * @return String with piece of statement to add or null 
	 */
	protected static String statementType(String type, int length){
		
		if(type.equalsIgnoreCase(ModelObjectAttribute.TYPE_STRING)){
			if(length>0){
				return "VARCHAR(" + length + ")";
			}else{
				return "TEXT";
			}
		}else if(type.equalsIgnoreCase(ModelObjectAttribute.TYPE_INTEGER)){
			if(length>0){
				return "INT(" + length + ")";
			}else{
				return "INT";
			}
			
		}else if(type.equalsIgnoreCase(ModelObjectAttribute.TYPE_DOUBLE)){
			if(length>0){
				return "DOUBLE(" + length + ")";
			}else{
				return "DOUBLE";
			}
		}
		
		return null;
	}
	
	/**
	 * Generate a select statement with the request received
	 * @param request DBRequest where to read values
	 * @return String statement generated
	 */
	protected static String statementSelect(DBRequest request){
		
		//generate the where clause
		String[] aWhereOrderLimit = statementWhereOrderLimit(request.operators);
		
		//generate the attributes response
		String attrs = statementAttributes(request.responseAttributes);
		
		return "SELECT " + attrs + " FROM " + request.modelObject + " " + aWhereOrderLimit[0] + " " + aWhereOrderLimit[1] + " " + aWhereOrderLimit[2];
	}
	
	/**
	 * Generate an insert statement with the request received
	 * @param request DBRequest where to read values
	 * @return String statement generated
	 */
	protected static String statementInsert(DBRequest request){
		
		//generate the where clause
		String sColumns = "";
		String sValues = "";
		
		for(String key : request.value.keySet()){
			
			//column
			if(sColumns.length()>0){
				sColumns += "," + key;
			}else{
				sColumns += key;
			}
			
			//value
			if(sValues.length()>0){
				sValues += ",'" + request.value.get(key).toString() + "'";
			}else{
				sValues += "'" + request.value.get(key).toString() + "'";
			}
			
		}
		
		//return the entire statement
		return "INSERT INTO " + request.modelObject + " (" + sColumns + ") VALUES (" + sValues + ")";
	}
	
	/**
	 * Generate an update statement with the request received
	 * @param request DBRequest where to read values
	 * @return String statement generated
	 */
	protected static String statementUpdate(DBRequest request){
		
		//generate the where clause
		String sWhere = statementWhereOrderLimit(request.operators)[0];
		
		//generate values
		String sValues = "";
		for(String key : request.value.keySet()){
			
			if(sValues.length()>0){
				sValues += ", ";
			}
			
			sValues += key + "='" + request.value.get(key).toString() + "'";
		}
		
		//return the entire statement
		return "UPDATE " + request.modelObject + " SET " + sValues + " " + sWhere;
	}
	
	/**
	 * Generate a select statement with the request received
	 * @param request DBRequest where to read values
	 * @return String statement generated
	 */
	protected static String statementDelete(DBRequest request){
		
		//generate the where clause
		String sWhere = statementWhereOrderLimit(request.operators)[0];
		
		//return the delete statement
		return "DELETE FROM " + request.modelObject + " " + sWhere;
	}
	
	/**
	 * Generate three Strings with WHERE, ORDER and LIMIT clauses 
	 * @param operators List<DBRequestOperator> list of operators to generate the statement
	 * @return String[] statements generated. 0: WHERE, 1: ORDER, 2: LIMIT
	 */
	private static String[] statementWhereOrderLimit(List<DBRequestOperator> operators){
		
		String sWhere = "";
		String sOrder = "";
		String sLimit = "";
		
		for(DBRequestOperator operator : operators){
			if(DBRequestOperator.SYMBOL_ORDER.equalsIgnoreCase(operator.symbol)){
				
				if(sOrder.length()==0){
					sOrder += "ORDER BY ";
				}else{
					sOrder += ",";
				}
				sOrder += operator.attribute + " ";
				if(operator.value.equalsIgnoreCase(DBRequestOperator.ORDER_DESCENDENCE)){
					sOrder += "DESC";
				}else{
					sOrder += "ASC";
				} 
				
			}else if(DBRequestOperator.SYMBOL_LIMIT.equalsIgnoreCase(operator.symbol)){
				
				sLimit = "LIMIT ";
				try{
					sLimit += String.valueOf(Integer.parseInt(operator.value));
				}catch(Exception e){
					e.printStackTrace();
					JEL.e("ERROR parsing number for limit value, setted to 1");
					sLimit += "1";
				}
				
			}else{
				
				if(sWhere.length()==0){
					sWhere += "WHERE ";
				}else{
					sWhere += " AND ";
				}
				sWhere += operator.attribute + operator.symbol + "'" + operator.value + "'";
			}
		}
		
		return new String[]{sWhere, sOrder, sLimit};
	}
	
	/**
	 * Generate the list of attributes to return. 
	 * If not defined "*" will be returned
	 * @param listAttributes List<String> list of attributes
	 * @return String with the list of attributes separated by commas
	 */
	private static String statementAttributes(List<String> listAttributes){
		
		String txt = "*";
		
		if(listAttributes!=null){
			for(String attribute : listAttributes){
				if(txt.length()>1){
					txt += "," + attribute;
				}else{
					txt = attribute;
				}
			}
		}
		
		return txt;
	}
	
}
