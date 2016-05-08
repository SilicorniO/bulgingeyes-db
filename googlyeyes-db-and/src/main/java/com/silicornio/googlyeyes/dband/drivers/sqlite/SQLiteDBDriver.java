package com.silicornio.googlyeyes.dband.drivers.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silicornio.googlyeyes.dband.GEDbConf;
import com.silicornio.googlyeyes.dband.GERequest;
import com.silicornio.googlyeyes.dband.GERequestOperator;
import com.silicornio.googlyeyes.dband.GEResponse;
import com.silicornio.googlyeyes.dband.drivers.DBDriver;
import com.silicornio.googlyeyes.dband.general.GEDBUtils;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dband.GEModelFactory;
import com.silicornio.googlyeyes.dband.GEModelObject;
import com.silicornio.googlyeyes.dband.GEModelObjectAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SQLiteDBDriver implements DBDriver {

	public static final String NAME = "SQLite";
	
	private static final String CONNECT_PREFIX = "jdbc:mysql://";
	
	/** Connection to database **/
	private SQLiteDatabase mDb = null;
	
	/** Model objects **/
	private GEModelObject[] mModels;

	/** Database helper **/
	private JESQLiteOpenHelper mDbHelper;

	//------------------- INITIALIZE --------------------
	
	@Override
	public void init(GEModelObject[] allModels) {
		mModels = allModels;		
	}	
	
	//-------------------- CONNECTION --------------------
	
	@Override
	public boolean connect(Context context, GEDbConf dbConf) {

		//initialize the helper
		mDbHelper = new JESQLiteOpenHelper(context, this, dbConf.name, 1);

		//get the reference to the database
		mDb = mDbHelper.getWritableDatabase();

		//connected, return OK
		return true;
	}

	@Override
	public void disconnect() {
		try {
			mDb.close();
		} catch (Exception e) {
            GEL.e("Exception closing connection with database: " + e.toString());
		}
	}

	
	//--------------------- MODEL ----------------------
	
	@Override
	public boolean modelExists(GEModelObject model) {
		
		try{
            String sStTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + model.name + "'";
			return SQLiteDBExecutor.executeQueryStatement(mDb, sStTable, new SQLiteDBExecutor.StatementListener() {

				@Override
				public boolean onResultSet(Cursor cursor) {
                    return cursor.moveToNext();
				}
			});
		}catch(SQLiteDBException mdbe){
            GEL.e("Exception checking if model exists: " + mdbe.toString());
			return false;
		}
	}
	
	@Override
	public void createModel(GEModelObject model) {
		
		try{
			String sStatement = SQLiteDBStGenerator.statementCreate(model, mModels);
			if(sStatement==null){
				return;
			}

            if (!SQLiteDBExecutor.executeStatementSimple(mDb, sStatement)){
				GEL.e("ERROR creating table '" + model.name + "'");
			}
		}catch(SQLiteDBException mdbe){
            GEL.e(mdbe.toString());
		}
	}

	@Override
	public boolean updateModel(final GEModelObject model) {
		
		try{
		
			//get information of the table
            String sStTableInfo = "PRAGMA table_info(" + model.name + ")";
			boolean result = SQLiteDBExecutor.executeQueryStatement(mDb, sStTableInfo, new SQLiteDBExecutor.StatementListener() {

				@Override
				public boolean onResultSet(Cursor cursor) {

					int num = 0;
					try {

						List<String> attrNames = new ArrayList<>();

						while (cursor.moveToNext()) {
							String attrName = cursor.getString(cursor.getColumnIndex("name"));
							String attrType = cursor.getString(cursor.getColumnIndex("type"));
							boolean attrId = cursor.getInt(cursor.getColumnIndex("pk"))==1;
							boolean attrUnique = false;//cursor.getString(cursor.getColumnIndex("COLUMN_KEY")).equalsIgnoreCase("UNI");
							boolean attrAutoincrement = attrType.contains("AUTO_INCREMENT");
							num++;

                            //clean type
                            attrType = attrType.replace("AUTO_INCREMENT", "").trim();

							//get model and attribute with id
							GEModelObjectAttribute attr = GEModelFactory.findAttribute(attrName, model);
							if (attr != null) {

                                //get the type of the attribute for the model
                                String attrTypeModel = SQLiteDBStGenerator.statementType(attr.type, attr.format, attr.length);
                                if(attrTypeModel==null){
                                    //search the type because it can be a reference
                                    GEModelObjectAttribute attrRef = GEModelFactory.findAttributeId(attr.type, mModels);
                                    if(attrRef!=null){
                                        attrTypeModel = SQLiteDBStGenerator.statementType(attrRef.type, attr.format, attrRef.length);
                                    }else{
                                        break;
                                    }
                                }

                                //check if a type has changed to alter the table
                                if (!attr.unique && attrUnique || !attrType.equalsIgnoreCase(attrTypeModel) || attr.id != attrId ||
                                        (attr.unique && !attrUnique) || attr.autoincrement != attrAutoincrement) {
                                    GEL.i("Modify a column of a table is not supported in SQLite");
                                }

							} else {

								//delete the attribute because it is not in the model
								GEL.i("Remove a column of a table is not supported in SQLite");
							}

							//add the name of the attribute to the list
							attrNames.add(attrName);
						}

						//add attributes not in database
						if (num > 0) {
							for (GEModelObjectAttribute attr : model.attributes) {
								if (!attrNames.contains(attr.name)) {
									String sStatement = "ALTER TABLE " + model.name + " ADD " + attr.name + " " + SQLiteDBStGenerator.statementColumn(attr, mModels);

                                    //remove UNIQUE because it is not supported adding a column
                                    if(sStatement.contains("UNIQUE")){
                                        sStatement = sStatement.replace("UNIQUE", "");
                                        GEL.i("UNIQUE is not supported in SQLite if the table is already created");
                                    }

									SQLiteDBExecutor.executeStatementSimple(mDb, sStatement);
								}
							}
						}

					} catch (Exception e) {
                        GEL.e("Exception updating model: " + e.toString());
					}

					return num > 0;
				}
			});
				
			return result;
		}catch(SQLiteDBException mdbe){
			return false;
		}
	}

	@Override
	public void deleteModel(GEModelObject model) {
		try{
			String sStatement = SQLiteDBStGenerator.statementDrop(model);
			if(!SQLiteDBExecutor.executeStatementSimple(mDb, sStatement)){
                GEL.e("ERROR droping table '" + model.name + "'");
			}
		}catch(SQLiteDBException mdbe){
            GEL.e(mdbe.toString());
		}
	}
	
	
	
	//---------------- REQUEST -----------------------------------
	
	@Override
	public GEResponse request(GERequest request) {

		//check db is not null
		if(mDb==null){
			return null;
		}

		//clean values with null from the map
		GEDBUtils.cleanMapNullValues(request.value);

		//check the type of request to execute
		GEResponse response = null;
		synchronized (mDb) {
			if (request.type == null || request.type.equals(GERequest.TYPE_RAW)) {
				response = executeRaw(request);
			} else if (request.type.equals(GERequest.TYPE_GET)) {
				response = executeSelect(request);
			} else if (request.type.equals(GERequest.TYPE_ADD)) {
				response = executeInsert(request);
			} else if (request.type.equals(GERequest.TYPE_UPDATE)) {
				response = executeUpdate(request);
			} else if (request.type.equals(GERequest.TYPE_DELETE)) {
				response = executeDelete(request);
			}
		}

		//return the response generated or null
		return response;
	}
	
	//---------------- CREATE STATEMENTS -------------------------
		
	
	
	//---------------- ACTIONS ------------------------
	
	/**
	 * Generate and execute a select with the request received
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private GEResponse executeSelect(final GERequest request){
		
		try{

            //get the models to use (used to generate a query with all nested objects)
            List modelsList = findNestedObjecs(request.modelObject, mModels, request.nestedObjects);
            final SQLiteDBModelObject[] models = (SQLiteDBModelObject[])modelsList.toArray(new SQLiteDBModelObject[modelsList.size()]);
            if(models.length==0){
                GEL.e("Trying to do a request with model '" + request.modelObject + "' that not exist");
                return null;
            }

			//generate the statement for select
			String sStatement = SQLiteDBStGenerator.statementSelect(request, models);
			
			//create the response to return
			final GEResponse response = new GEResponse();
			SQLiteDBExecutor.executeQueryStatement(mDb, sStatement, new SQLiteDBExecutor.StatementListener() {

				@Override
				public boolean onResultSet(Cursor cursor) {

					try {
						while (cursor.moveToNext()) {
							response.numResults++;

                            if((request.responseAttributes==null || request.responseAttributes.size()==0) && request.nestedObjects && models.length>1) {

                                //convert row to a HashMap of each object
                                Map<String, Object>[] maps = new Map[models.length];
                                int numColumnsRead = 0;
                                for (int i=0; i<models.length; i++) {
                                    maps[i] = rowToMap(cursor, numColumnsRead, numColumnsRead + models[i].attributes.length);
                                    numColumnsRead += models[i].attributes.length;
                                }

                                response.results.add(joinMapModels(0, maps, models));

                            }else{
                                response.results.add(rowToMap(cursor, 0, cursor.getColumnCount()));
                            }
						}

						//change from the list to the object if there is only one
						response.clean();

					} catch (Exception e) {
						GEL.e("ERROR converting query result to response: " + e.toString());
						return false;
					}

					return true;
				}
			});

			//check if no response with nested object to try to do the request without nested objects
			if(response.numResults==0 && request.nestedObjects){
				request.nestedObjects = false;
				return executeSelect(request);
			}else {
				return response;
			}
			
		}catch(SQLiteDBException mdbe){
			return GEResponse.generateErrorDB(mdbe.toString());
		}
	}

    /**
     * Find the object and all nested objects of that object
     * @param nameObject String name of the modelObject where to search nested objects
     * @param models ModelObject[] array of models where to search when one is found
	 * @param nestedObjects boolean TRUE for searching inside of the children or false to return the first one
     * @return SQLiteDBModelObject[] list of nested objects including the search one or empty list if not found
     */
    private static List<SQLiteDBModelObject> findNestedObjecs(String nameObject, GEModelObject[] models, boolean nestedObjects){

        //list to return
        List<SQLiteDBModelObject> objects = new ArrayList<>();

        //search the object with the name received
        GEModelObject mo = GEModelFactory.findObject(nameObject, models);
        if(mo!=null) {

            //add the model creating a new one without attributes
            SQLiteDBModelObject moCopy = new SQLiteDBModelObject(mo);
            objects.add(moCopy);

            //search in all attributes for nested objects
			if(nestedObjects) {
				List<GEModelObjectAttribute> attrs = new ArrayList<>();
				for (GEModelObjectAttribute moa : mo.attributes) {

					//if it is an object we search the object and nested objects, adding all to the list
					if (moa.isObjectType()) {

						//add the attribute to the list of the model
						attrs.add(moa);

						//find nested objects of that one
						objects.addAll(findNestedObjecs(moa.type, models, nestedObjects));
					}
				}

				//set the attributes to the model copy
				moCopy.attributesObject = attrs.toArray(new GEModelObjectAttribute[attrs.size()]);
			}
        }

        //return the list
        return objects;
    }

    /**
     * Join a group of maps in one using the models received
     * @param index int index to know the map reading (recursive)
     * @param maps Map<String, Object> array of maps referred to each model
     * @param models SQLiteDBModelObject[] array of models
     * @return Map<String, Object> with nested maps if necessary
     */
    private Map<String, Object> joinMapModels(int index, Map<String, Object>[] maps, SQLiteDBModelObject[] models){

        //get the first map
        Map<String, Object> map = maps[index];

        //for each attribute with object add the associated map
        for(GEModelObjectAttribute attr : models[index].attributesObject){
            for(int i=0; i<models.length; i++){
                if(models[i].name.equalsIgnoreCase(attr.type)){
                    map.put(attr.name, joinMapModels(i, maps, models));
                    break;
                }
            }
        }

        //return the map
        return map;
    }
	
	/**
	 * Generate and execute an insert with the request received
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private GEResponse executeInsert(GERequest request){

		//get the model object
		GEModelObject modelObject = GEModelFactory.findObject(request.modelObject, mModels);
		if(modelObject==null){
			GEL.e("Trying to execute insert request with model '" + request.modelObject + "' that not exist");
			return null;
		}

        //check attributes with references to other models to add them before adding this one
        for(GEModelObjectAttribute moa : modelObject.attributes){
            if(moa.isObjectType() && request.value.get(moa.name)!=null && request.value.get(moa.name) instanceof Map){

                //get the model reference
                GEModelObjectAttribute moaRef = GEModelFactory.findAttributeId(moa.type, mModels);
                if(moaRef!=null) {

                    //generate a request for this attribute and execute the insert
                    GERequest requestRef = new GERequest(GERequest.TYPE_ADD, moa.type);
                    requestRef.value = (Map)request.value.get(moa.name);
                    GEResponse responseRef = executeInsert(requestRef);

                    //get the identifier of the response and set it to the value
                    if(responseRef!=null && responseRef.numResults==1){
                        request.value.put(moa.name, responseRef.result.get(moaRef.name));
                    }
                }
            }
        }

		//generate the statement for select
		String sStatement = SQLiteDBStGenerator.statementInsert(request, modelObject, mModels);
		
		//execute the insert
		try{
			int idGenerated = SQLiteDBExecutor.executeStatementInsert(mDb, sStatement);
			if(idGenerated==-1){
				return null;
			}
		
			//get the model and look for the ID of the object to find it
			GEModelObject model = GEModelFactory.findObject(request.modelObject, mModels);
			if(model!=null){
				
				//generate the request to get the object
				GERequest idRequest = new GERequest(GERequest.TYPE_GET, model.name);
				idRequest.responseAttributes = request.responseAttributes;
				idRequest.nestedObjects = request.nestedObjects;
				
				//add the operator, first we need to get the attribute with ID value
				GEModelObjectAttribute ma = GEModelFactory.findAttributeId(model);
				if(ma!=null){
					if(ma.autoincrement){
						idRequest.operators.add(new GERequestOperator(ma.name, "=", String.valueOf(idGenerated)));
					}else{
						Object obj = request.value.get(ma.name);
						if(obj!=null){
							idRequest.operators.add(new GERequestOperator(ma.name, "=", request.value.get(ma.name).toString()));
                        } else {
                            GEL.e("Not possible to return the object for model '" + model.name + "'");
							return null;
						}
					}
					
					//execute the request
					return executeSelect(idRequest);
				}					
				
			}
		}catch(SQLiteDBException mdbe){
			return GEResponse.generateErrorDB(mdbe.toString());
		}
		
		return null;
	}
	
	/**
	 * Generate and execute an update with the request received
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private GEResponse executeUpdate(GERequest request){
		
		try{
			//generate the statement for select
			String sStatement = SQLiteDBStGenerator.statementUpdate(request);

            //execute the insert
			SQLiteDBExecutor.executeStatementUpdateDelete(mDb, sStatement);
			
			//get the model and look for the ID of the object to find it
			GEModelObject model = GEModelFactory.findObject(request.modelObject, mModels);
			if(model!=null){
				
				//generate the request to get the values modified, changing actual to GET
				GERequest selectRequest = request.clone();
				selectRequest.type = GERequest.TYPE_GET;
				
				//execute the request
				return executeSelect(selectRequest);				
			}
			
		}catch(SQLiteDBException mdbe){
			return GEResponse.generateErrorDB(mdbe.toString());
		}
		
		return null;
	}
	
	/**
	 * Generate and execute a select with the request received
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private GEResponse executeDelete(final GERequest request){

		//create the response to return
		final GEResponse response = new GEResponse();

		try{
			//generate the statement for select
			String sStatement = SQLiteDBStGenerator.statementDelete(request);

            //execute the statement
			response.numResults = SQLiteDBExecutor.executeStatementUpdateDelete(mDb, sStatement);

		}catch(SQLiteDBException mdbe){
			return GEResponse.generateErrorDB(mdbe.toString());
		}
		
		//return empty because object was deleted
		return response;
	}
	
	/**
	 * Generate and execute a raw request
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private GEResponse executeRaw(final GERequest request){
		
		//create the response to return
		final GEResponse response = new GEResponse();
		
		try{
			
			//execute directly the statement
			SQLiteDBExecutor.executeQueryStatement(mDb, request.raw, new SQLiteDBExecutor.StatementListener() {

				@Override
				public boolean onResultSet(Cursor cursor) {

					try {
						while (cursor.moveToNext()) {
							response.numResults++;

							//convert row to a HashMap
							response.results.add(rowToMap(cursor, 0, cursor.getColumnCount()));
						}

						//change from the list to the object if there is only one
						response.clean();

					} catch (Exception e) {
						GEL.e("ERROR converting query result to response: " + e.toString());
						return false;
					}

					return true;
				}
			});

		}catch(SQLiteDBException mdbe){
			return GEResponse.generateErrorDB(mdbe.toString());
		}
		
		//return empty because object was deleted
		return response;
	}
	
	
	/**
	 * Convert the Cursor given to a HashMap
	 * @param cursor Cursor to read
     * @param columnStart int index of column to start reading
     * @param columnEnd int index of column to stop reading
	 * @return HashMap<String, Object> map
	 */
	private static Map<String, Object> rowToMap(Cursor cursor, int columnStart, int columnEnd){
		Map<String, Object> map = new HashMap<>();
		try{
            for(int i=columnStart; i<columnEnd && i<cursor.getColumnCount(); i++){
                map.put(
                        cursor.getColumnName(i),
                        getCursorValue(cursor, i)
                );
            }

		}catch(Exception e){
			GEL.e("ERROR converting ResultSet to Map: " + e.toString());
		}
		return map;
	}

    /**
     * Get the cursor
     * @param cursor Cursor from where read value
     * @param index int to read
     * @return Object read
     */
    private static Object getCursorValue(Cursor cursor, int index){
        switch(cursor.getType(index)){
            case Cursor.FIELD_TYPE_INTEGER: return Integer.valueOf(cursor.getInt(index));
            case Cursor.FIELD_TYPE_FLOAT: return Float.valueOf(cursor.getFloat(index));
            case Cursor.FIELD_TYPE_STRING: return cursor.getString(index);
            case Cursor.FIELD_TYPE_BLOB: return cursor.getBlob(index);
            default:
                return null;
        }
    }
	
}
