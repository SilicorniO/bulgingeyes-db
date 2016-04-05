package com.silicornio.bulgingeyes.db.drivers.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.silicornio.bulgingeyes.db.DBRequest.DBRequest;
import com.silicornio.bulgingeyes.db.DBRequest.DBRequestOperator;
import com.silicornio.bulgingeyes.db.DBRequest.DBResponse;
import com.silicornio.bulgingeyes.db.db.DbConf;
import com.silicornio.bulgingeyes.db.drivers.DBDriver;
import com.silicornio.bulgingeyes.db.general.JEL;
import com.silicornio.bulgingeyes.db.model.ModelFactory;
import com.silicornio.bulgingeyes.db.model.ModelObject;
import com.silicornio.bulgingeyes.db.model.ModelObjectAttribute;

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
	private ModelObject[] mModels;

	/** Database helper **/
	private JESQLiteOpenHelper mDbHelper;

	//------------------- INITIALIZE --------------------
	
	@Override
	public void init(ModelObject[] allModels) {
		mModels = allModels;		
	}	
	
	//-------------------- CONNECTION --------------------
	
	@Override
	public boolean connect(Context context, DbConf dbConf) {

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
			e.printStackTrace();
		}
	}

	
	//--------------------- MODEL ----------------------
	
	@Override
	public boolean modelExists(ModelObject model) {
		
		try{
            String sStTable = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + model.name + "'";
			return SQLiteDBExecutor.executeQueryStatement(mDb, sStTable, new SQLiteDBExecutor.StatementListener() {

				@Override
				public boolean onResultSet(Cursor cursor) {
                    return cursor.moveToNext();
				}
			});
		}catch(SQLiteDBException mdbe){
            JEL.e("Exception checking if model exists: " + mdbe.toString());
			return false;
		}
	}
	
	@Override
	public void createModel(ModelObject model) {
		
		try{
			String sStatement = SQLiteDBStGenerator.statementCreate(model, mModels);
			if(sStatement==null){
				return;
			}

            if (!SQLiteDBExecutor.executeStatementSimple(mDb, sStatement)){
				JEL.e("ERROR creating table '" + model.name + "'");
			}
		}catch(SQLiteDBException mdbe){
            JEL.e(mdbe.toString());
		}
	}

	@Override
	public boolean updateModel(final ModelObject model) {
		
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

							//get attribute with that name
							ModelObjectAttribute attr = ModelFactory.findAttribute(attrName, model);
							if (attr != null) {

                                //get the type of the attribute for the model
                                String attrTypeModel = SQLiteDBStGenerator.statementType(attr.type, attr.length);
                                if(attrTypeModel==null){
                                    //search the type because it can be a reference
                                    ModelObjectAttribute attrRef = ModelFactory.findAttribute(attr.type, mModels);
                                    if(attrRef!=null){
                                        attrTypeModel = SQLiteDBStGenerator.statementType(attrRef.type, attrRef.length);
                                    }else{
                                        break;
                                    }
                                }

                                //check if a type has changed to alter the table
                                if (!attr.unique && attrUnique || !attrType.equalsIgnoreCase(attrTypeModel) || attr.id != attrId ||
                                        (attr.unique && !attrUnique) || attr.autoincrement != attrAutoincrement) {
                                    JEL.i("Modify a column of a table is not supported in SQLite");
                                }

							} else {

								//delete the attribute because it is not in the model
								JEL.i("Remove a column of a table is not supported in SQLite");
							}

							//add the name of the attribute to the list
							attrNames.add(attrName);
						}

						//add attributes not in database
						if (num > 0) {
							for (ModelObjectAttribute attr : model.attributes) {
								if (!attrNames.contains(attr.name)) {
									String sStatement = "ALTER TABLE " + model.name + " ADD " + attr.name + " " + SQLiteDBStGenerator.statementColumn(attr, mModels);

                                    //remove UNIQUE because it is not supported adding a column
                                    if(sStatement.contains("UNIQUE")){
                                        sStatement = sStatement.replace("UNIQUE", "");
                                        JEL.i("UNIQUE is not supported in SQLite if the table is already created");
                                    }

									SQLiteDBExecutor.executeStatementSimple(mDb, sStatement);
								}
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
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
	public void deleteModel(ModelObject model) {
		try{
			String sStatement = SQLiteDBStGenerator.statementDrop(model);
			if(!SQLiteDBExecutor.executeStatementSimple(mDb, sStatement)){
                JEL.e("ERROR droping table '" + model.name + "'");
			}
		}catch(SQLiteDBException mdbe){
            JEL.e(mdbe.toString());
		}
	}
	
	
	
	//---------------- REQUEST -----------------------------------
	
	@Override
	public DBResponse request(DBRequest request) {
		
		//check the type of request to execute
		DBResponse response = null;
		if(request.type==null || request.type.equals(DBRequest.TYPE_RAW)){
			response = executeRaw(request);
		}else if(request.type.equals(DBRequest.TYPE_GET)){
			response = executeSelect(request);
		}else if(request.type.equals(DBRequest.TYPE_ADD)){
			response = executeInsert(request);
		}else if(request.type.equals(DBRequest.TYPE_UPDATE)){
			response = executeUpdate(request);
		}else if(request.type.equals(DBRequest.TYPE_DELETE)){
			response = executeDelete(request);
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
	private DBResponse executeSelect(final DBRequest request){
		
		try{
		
			//generate the statement for select
			String sStatement = SQLiteDBStGenerator.statementSelect(request);
			
			//create the response to return
			final DBResponse response = new DBResponse();
			SQLiteDBExecutor.executeQueryStatement(mDb, sStatement, new SQLiteDBExecutor.StatementListener() {

				@Override
				public boolean onResultSet(Cursor cursor) {

					try {
						while (cursor.moveToNext()) {
							response.numResults++;

							//convert row to a HashMap
							response.results.add(rowToHashMap(cursor));
						}

						//change from the list to the object if there is only one
						response.clean();

					} catch (Exception e) {
						e.printStackTrace();
						JEL.e("ERROR converting query result to response");
						return false;
					}

					return true;
				}
			});
			
			return response;
			
		}catch(SQLiteDBException mdbe){
			return DBResponse.generateErrorDB(mdbe.toString());
		}
	}
	
	/**
	 * Generate and execute an insert with the request received
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private DBResponse executeInsert(DBRequest request){
		
		//generate the statement for select
		String sStatement = SQLiteDBStGenerator.statementInsert(request);
		
		//execute the insert
		try{
			int idGenerated = SQLiteDBExecutor.executeStatementInsert(mDb, sStatement);
			if(idGenerated==-1){
				return null;
			}
		
			//get the model and look for the ID of the object to find it
			ModelObject model = ModelFactory.findObject(request.modelObject, mModels);
			if(model!=null){
				
				//generate the request to get the object
				DBRequest idRequest = new DBRequest(DBRequest.TYPE_GET, model.name);
				idRequest.responseAttributes = request.responseAttributes;
				
				//add the operator, first we need to get the attribute with ID value
				ModelObjectAttribute ma = ModelFactory.findAttributeId(model);
				if(ma!=null){
					if(ma.autoincrement){
						idRequest.operators.add(new DBRequestOperator(ma.name, "=", String.valueOf(idGenerated)));
					}else{
						Object obj = request.value.get(ma.name);
						if(obj!=null){
							idRequest.operators.add(new DBRequestOperator(ma.name, "=", request.value.get(ma.name).toString()));
                        } else {
                            JEL.e("Not possible to return the object for model '" + model.name + "'");
							return null;
						}
					}
					
					//execute the request
					return executeSelect(idRequest);
				}					
				
			}
		}catch(SQLiteDBException mdbe){
			return DBResponse.generateErrorDB(mdbe.toString());
		}
		
		return null;
	}
	
	/**
	 * Generate and execute an update with the request received
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private DBResponse executeUpdate(DBRequest request){
		
		try{
			//generate the statement for select
			String sStatement = SQLiteDBStGenerator.statementUpdate(request);

            //execute the insert
			SQLiteDBExecutor.executeStatementUpdateDelete(mDb, sStatement);
			
			//get the model and look for the ID of the object to find it
			ModelObject model = ModelFactory.findObject(request.modelObject, mModels);
			if(model!=null){
				
				//generate the request to get the values modified, changing actual to GET
				DBRequest selectRequest = request.clone();
				selectRequest.type = DBRequest.TYPE_GET;
				
				//execute the request
				return executeSelect(selectRequest);				
			}
			
		}catch(SQLiteDBException mdbe){
			return DBResponse.generateErrorDB(mdbe.toString());
		}
		
		return null;
	}
	
	/**
	 * Generate and execute a select with the request received
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private DBResponse executeDelete(final DBRequest request){
		
		try{
			//generate the statement for select
			String sStatement = SQLiteDBStGenerator.statementDelete(request);

            //execute the statement
			SQLiteDBExecutor.executeStatementUpdateDelete(mDb, sStatement);

		}catch(SQLiteDBException mdbe){
			return DBResponse.generateErrorDB(mdbe.toString());
		}
		
		//return empty because object was deleted
		return new DBResponse();
	}
	
	/**
	 * Generate and execute a raw request
	 * @param request DBRequest with information to generate the statement
	 * @return DBResponse with the result to convert to JSON
	 */
	private DBResponse executeRaw(final DBRequest request){
		
		//create the response to return
		final DBResponse response = new DBResponse();
		
		try{
			
			//execute directly the statement
			SQLiteDBExecutor.executeQueryStatement(mDb, request.raw, new SQLiteDBExecutor.StatementListener() {

				@Override
				public boolean onResultSet(Cursor cursor) {

					try {
						while (cursor.moveToNext()) {
							response.numResults++;

							//convert row to a HashMap
							response.results.add(rowToHashMap(cursor));
						}

						//change from the list to the object if there is only one
						response.clean();

					} catch (Exception e) {
						e.printStackTrace();
						JEL.e("ERROR converting query result to response");
						return false;
					}

					return true;
				}
			});

		}catch(SQLiteDBException mdbe){
			return DBResponse.generateErrorDB(mdbe.toString());
		}
		
		//return empty because object was deleted
		return response;
	}
	
	
	/**
	 * Convert the Cursor given to a HashMap
	 * @param cursor Cursor to read
	 * @return HashMap<String, Object> map
	 */
	private static Map<String, Object> rowToHashMap(Cursor cursor){
		Map<String, Object> map = new HashMap<>();
		try{
            for(int i=0; i<cursor.getColumnCount(); i++){
                map.put(
                        cursor.getColumnName(i),
                        getCursorValue(cursor, i)
                );
            }

		}catch(Exception e){
			e.printStackTrace();
			JEL.e("ERROR converting ResultSet to Map");
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
