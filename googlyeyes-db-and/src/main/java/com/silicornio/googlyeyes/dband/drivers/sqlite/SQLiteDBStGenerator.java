package com.silicornio.googlyeyes.dband.drivers.sqlite;


import com.silicornio.googlyeyes.dband.GERequest;
import com.silicornio.googlyeyes.dband.GERequestOperator;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dband.GEModelFactory;
import com.silicornio.googlyeyes.dband.GEModelObject;
import com.silicornio.googlyeyes.dband.GEModelObjectAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteDBStGenerator {

	/**
	 * Generate the statement to create a table
	 * @param model ModelObject to generate the table
	 * @param models ModelObject[] array of models where to find other attributes
	 * @return String with statement
	 */
	protected static String statementCreate(GEModelObject model, GEModelObject[] models){
		
		String sStatement = "CREATE TABLE " + model.name + "(";
		for(int i=0,numIds=0; i<model.attributes.length; i++){
			GEModelObjectAttribute attr = model.attributes[i];

			//name
			sStatement += attr.name + " ";

            //check if field is autoincrement because in that case it has to be integer
            if(attr.autoincrement){
                //check type is integer
                if(attr.type.equalsIgnoreCase(GEModelObjectAttribute.TYPE_INTEGER)){
                    sStatement += "INTEGER ";
                }else{
                    GEL.e("Autoincrement value it is only valid for integer values in SQLite");
                }
            }else{
                sStatement += statementColumn(attr, models);
            }
			
			//key
			if(attr.id){

				//add the primary key statement
                sStatement += "PRIMARY KEY";
				
			}
			
			//end
			if(i<model.attributes.length-1){
				sStatement += ",";
			}
		}

		sStatement += ")";

		return sStatement;
	}
	
	/**
	 * Generate the statement to drop a table
	 * @param model ModelObject to drop the table
	 * @return String with statement
	 */
	protected static String statementDrop(GEModelObject model){
		return "DROP TABLE " + model.name;
	}
	
	/**
	 * Generate the column data to set in the statement
	 * @param attr ModelObjectAttribute with all information to generate
	 * @param models ModelObject[] array of models where to find other attributes
	 * @return String with piece of statement with data of the column
	 */
	protected static String statementColumn(GEModelObjectAttribute attr, GEModelObject[] models){
		
		String sStatement = "";
						
		//type
		String sStatementType = statementType(attr.type, attr.format, attr.length);
		if(sStatementType==null){
			//find the type referred
			GEModelObject modelRef = GEModelFactory.findObject(attr.type, models);
			if(modelRef==null){
                GEL.e("Model '" + attr.type + "' not found for attribute '" + attr.name + "'");
                return null;
			}

            //find the attribute of the modelRef
            GEModelObjectAttribute attrRef = GEModelFactory.findAttributeId(modelRef);
            if(attrRef==null){
                GEL.e("Model '" + modelRef.name + "' has not got an attribute as Id and it is not possible to link with attribute'" + attr.name + "'");
                return null;
            }

			//add the statement type
			sStatementType = statementType(attrRef.type, attrRef.format, attrRef.length);
			if(sStatementType==null){
				return null;
			}
		}
		sStatement += sStatementType + " ";

        //autoincrement not used because SQLite use it automatically with IDs

		//key
		if(!attr.id){

            //unique
            if(attr.unique){
                sStatement += "UNIQUE ";
            }
		
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
	 * @param format String to use if the type requires a format
	 * @param length int of the variable
	 * @return String with piece of statement to add or null 
	 */
	protected static String statementType(String type, String format, int length){

		if(type.equalsIgnoreCase(GEModelObjectAttribute.TYPE_STRING)){
			if(length>0){
				return "VARCHAR(" + length + ")";
			}else{
				return "TEXT";
			}
		}else if(type.equalsIgnoreCase(GEModelObjectAttribute.TYPE_INTEGER)){
			if(length>0){
				return "INT(" + length + ")";
			}else{
				return "INT";
			}

		}else if(type.equalsIgnoreCase(GEModelObjectAttribute.TYPE_BOOLEAN)){
			return "INT(1)";

		}else if(type.equalsIgnoreCase(GEModelObjectAttribute.TYPE_DOUBLE)){
			if(length>0){
				return "DOUBLE(" + length + ")";
			}else{
				return "DOUBLE";
			}

		}else if(type.equalsIgnoreCase(GEModelObjectAttribute.TYPE_DATE)){
			if(format!=null && format.length()>0){
				return "VARCHAR(" + format.length() + ")";
			}else{
				//apply default format
				return "VARCHAR(" + GEModelObjectAttribute.FORMAT_DATE_DEFAULT.length() + ")";
			}
		}

		return null;
	}

	/**
	 * Generate a select statement with the request received
	 * @param request DBRequest where to read values
     * @param models SQLiteDBModelObject[] to use in the request
	 * @return String statement generated
	 */
	protected static String statementSelect(GERequest request, SQLiteDBModelObject[] models){

		//generate the where clause
		String[] aWhereOrderLimit = statementWhereOrderLimit(request.operators, request.operatorsLogic, models);

		//generate the attributes response
		String attrs = statementAttributes(request.responseAttributes, models);

        //generate the froms
        String froms = (models==null? request.modelObject : statementFroms(models));

		return "SELECT " + attrs + " FROM " + froms + " " + aWhereOrderLimit[0] + " " + aWhereOrderLimit[1] + " " + aWhereOrderLimit[2];
	}

	/**
	 * Generate an insert statement with the request received
	 * @param request DBRequest where to read values
	 * @param modelObject ModelObject of the request
     * @param modelObjects ModelObject[] Array of models to get references
	 * @return String statement generated
	 */
	protected static String statementInsert(GERequest request, GEModelObject modelObject, GEModelObject[] modelObjects){

		//generate the where clause
		String sColumns = "";
		String sValues = "";

		for(String key : request.value.keySet()){

			GEModelObjectAttribute moa = GEModelFactory.findAttribute(key, modelObject);
			if(moa!=null) {

                if(!moa.autoincrement) {

                    //column
                    if (sColumns.length() > 0) {
                        sColumns += "," + key;
                    } else {
                        sColumns += key;
                    }

                    if (sValues.length() > 0) {
                        sValues += ",";
                    }

                    //value
                    if (moa.isObjectType() && request.value.get(key) instanceof Map) {

						//get identifier of the object type
						GEModelObjectAttribute moaRef = null;
						GEModelObject moRef = GEModelFactory.findObject(moa.type, modelObjects);
						if (moRef != null) {
							moaRef = GEModelFactory.findAttributeId(moRef);
						}
						if (moaRef != null && ((Map) request.value.get(key)).get(moaRef.name) != null) {
							sValues += "'" + ((Map) request.value.get(key)).get(moaRef.name).toString() + "'";
						} else {
							GEL.e("It was impossible to find the ID attribute of the model requested for '" + modelObject.name + "' or is null the attribute '" + moa.name + "'");
							sValues += "null";
						}

					} else if(moa.type.equalsIgnoreCase(GEModelObjectAttribute.TYPE_BOOLEAN)){
						sValues += "'" + (request.value.get(key).equals(Boolean.TRUE) || request.value.get(key).equals("true")? 1 : 0) + "'";
                    } else {
                        sValues += "'" + request.value.get(key) + "'";
                    }
                }else{
                    GEL.w("Attribute '" + moa.name + "' is autoincrement and a value was received, it was not used!");
                }

			}else{
				GEL.e("Attribute '" + key + "' of model '" + request.modelObject + "' not exists");
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
	protected static String statementUpdate(GERequest request){
		
		//generate the where clause
		String sWhere = statementWhereOrderLimit(request.operators, request.operatorsLogic, null)[0];
		
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
	protected static String statementDelete(GERequest request){
		
		//generate the where clause
		String sWhere = statementWhereOrderLimit(request.operators, request.operatorsLogic, null)[0];
		
		//return the delete statement
		return "DELETE FROM " + request.modelObject + " " + sWhere;
	}
	
	/**
	 * Generate three Strings with WHERE, ORDER and LIMIT clauses 
	 * @param operators List<DBRequestOperator> list of operators to generate the statement
	 * @param operatorsLogic String with the logic to apply to the operators
     * @param models SQLiteDBModelObject[] to use in the request, can be null if select if for one element
	 * @return String[] statements generated. 0: WHERE, 1: ORDER, 2: LIMIT
	 */
	private static String[] statementWhereOrderLimit(List<GERequestOperator> operators, String operatorsLogic, SQLiteDBModelObject[] models){
		
		String sWhere = "";
		String sOrder = "";
		String sLimit = "";

		//apply operators logic
		boolean applyOperatorsLogic = operatorsLogic!=null && operatorsLogic.length()>0;
		if(applyOperatorsLogic){
			//set a copy of the operators logic in where field to change their values and not change the original
			sWhere = "WHERE (" + operatorsLogic + ")";
		}

		//map where to set all the values that will be loaded later
		Map<String, String> mapOperatorsLogic = new HashMap<>();

		for(int i=0; i<operators.size(); i++){

			//get the operator
			GERequestOperator operator = operators.get(i);

			if(GERequestOperator.SYMBOL_ORDER.equalsIgnoreCase(operator.symbol)){
				
				if(sOrder.length()==0){
					sOrder += "ORDER BY ";
				}else{
					sOrder += ",";
				}
				sOrder += operator.attribute + " ";
				if(operator.value.equalsIgnoreCase(GERequestOperator.ORDER_DESCENDENCE)){
					sOrder += "DESC";
				}else{
					sOrder += "ASC";
				} 
				
			}else if(GERequestOperator.SYMBOL_LIMIT.equalsIgnoreCase(operator.symbol)){
				
				sLimit = "LIMIT ";
				try{
					sLimit += String.valueOf(Integer.parseInt(operator.value));
				}catch(Exception e){
					GEL.e("ERROR parsing number for limit value, setted to 1");
					sLimit += "1";
				}
				
			}else{

				//convert the operator to text
				String operatorText = operator.attribute + operator.symbol + "'" + operator.value + "'";

				//if we are applying operators logic translate the value, else use AND
				if(applyOperatorsLogic){

					//add to the map the two possibilities, the number and the attribute of the operator
					mapOperatorsLogic.put("{@" + operator.attribute + "}", operatorText);
					mapOperatorsLogic.put("{@" + String.valueOf(i) + "}", operatorText);

					//convert the logic where text to the new reference
					sWhere = sWhere.replace(operator.attribute, "{@" + operator.attribute + "}");
					sWhere = sWhere.replace(String.valueOf(i), "{@" + String.valueOf(i) + "}");

				}else {
					if (sWhere.length() == 0) {
						sWhere += "WHERE (";
					} else {
						sWhere += " AND ";
					}
					sWhere += operatorText;
				}
			}
		}

		//apply the map of values with logic
		if(applyOperatorsLogic){

			//replace AND and OR
			sWhere = sWhere.replace("&&", " AND ").replace("||", " OR ");

			//replace all values with the map
			for(String key : mapOperatorsLogic.keySet()){
				sWhere = sWhere.replace(key, mapOperatorsLogic.get(key));
			}

		}else if(sWhere.length()>0){
			sWhere += ")";
		}

        //add connection between models to the statement
        if(models!=null && models.length>1){

            //for each model we add all the references to objects in their attributes
            for(GEModelObject mo : models){
                for(GEModelObjectAttribute moa : mo.attributes){
                    if(moa.isObjectType()){
                        GEModelObjectAttribute moaRef = GEModelFactory.findAttributeId(moa.type, models);
                        if(moaRef!=null) {
                            if (sWhere.length() == 0) {
                                sWhere += "WHERE ";
                            } else {
                                sWhere += " AND ";
                            }
                            sWhere += mo.name + "." + moa.name + "=" + moa.type + "." + moaRef.name;
                        }
                    }
                }
            }
        }
		
		return new String[]{sWhere, sOrder, sLimit};
	}
	
	/**
	 * Generate the list of attributes to return. 
	 * If not defined "*" will be returned
	 * @param listAttributes List<String> list of attributes
     * @param models SQLiteDBModelObject[]with models to search
	 * @return String with the list of attributes separated by commas
	 */
	private static String statementAttributes(List<String> listAttributes, SQLiteDBModelObject[] models){
		
		String txt = "*";
		
		if(listAttributes!=null && listAttributes.size()>0){
			for(String attribute : listAttributes){
				if(txt.length()>1){
					txt += "," + attribute;
				}else{
					txt = attribute;
				}
			}
		}else{

            //add models inside the model
            if(models!=null) {
                for (GEModelObject mo : models) {
                    if (txt.length() == 1) {
                        txt = mo.name + ".*";
                    } else {
                        txt += "," + mo.name + ".*";
                    }
                }
            }

        }
		
		return txt;
	}

    /**
     * Generate the list of froms to return
     * @param models SQLiteDBModelObject[] with models to search
     * @return String with the list of attributes separated by commas
     */
    private static String statementFroms(SQLiteDBModelObject[] models){

        String txt = "";

        //add models inside the model
        if(models!=null) {
            for (GEModelObject mo : models) {
                if (txt.length() == 0) {
                    txt = mo.name;
                } else {
                    txt += "," + mo.name;
                }
            }
        }

        return txt;
    }
	
}
