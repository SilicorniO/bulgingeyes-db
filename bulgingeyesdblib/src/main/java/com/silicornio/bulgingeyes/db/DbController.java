package com.silicornio.bulgingeyes.db;

import android.content.Context;

import com.silicornio.bulgingeyes.db.DBRequest.DBRequest;
import com.silicornio.bulgingeyes.db.DBRequest.DBResponse;
import com.silicornio.bulgingeyes.db.db.DbConf;
import com.silicornio.bulgingeyes.db.drivers.DBDriver;
import com.silicornio.bulgingeyes.db.drivers.sqlite.SQLiteDBDriver;
import com.silicornio.bulgingeyes.db.general.JEL;
import com.silicornio.bulgingeyes.db.model.ModelConf;
import com.silicornio.bulgingeyes.db.model.ModelObject;


public class DbController {

	
	/** Database configuration **/
	public DbConf mDbConf;
	
	/** Model configuration **/
	private ModelConf mModelConf;
		
	/** Driver to connect to database **/
	private DBDriver dbDriver;

	public DbController(DbConf dbConf, ModelConf modelConf){

		//save data
		mDbConf = dbConf;
		mModelConf = modelConf;
	}

    /**
     * Return the model configuration used by the DbController
     * @return ModelConf used
     */
    public ModelConf getModelConf(){
        return mModelConf;
    }

	/**
	 * Connect the database
	 * @param context Context to conect the database
	 */
	public boolean connectDb(Context context){

		//get the right instance
		if(mDbConf.driver.equalsIgnoreCase(SQLiteDBDriver.NAME)){
			dbDriver = new SQLiteDBDriver();
		}
		
		//check if there was a valid driver
		if(dbDriver==null){
			JEL.e("Driver not valid, check the db.conf");
			return false;
		}
		
		//initialize the driver
		dbDriver.init(mModelConf.objects);
		
		//connect the database
		if(!dbDriver.connect(context, mDbConf)){
			JEL.e("Error connecting to database, check the parameters of database configuration file (db.conf)");
			return false;
		}
				
		//for each object create, update or delete the table associated
		for(ModelObject m : mModelConf.objects){
			
			//check if model exists
			boolean modelExists = dbDriver.modelExists(m);
			
			if(modelExists){
				
				if(m.dbAction.equalsIgnoreCase(ModelObject.ACTION_DELETE)){
					dbDriver.deleteModel(m);
					
				}else if(m.dbAction.equalsIgnoreCase(ModelObject.ACTION_UPDATE)){
					dbDriver.updateModel(m);
				}
				
			}else if(m.dbAction!=ModelObject.ACTION_DELETE){
				dbDriver.createModel(m);
			}			
				
		}
		
		
		//return OK connected
		return true;
	}
	
	/**
	 * Execute the request received
	 * @param request DbRequest to execute
	 */
	public DBResponse request(DBRequest request){


		if(request==null){
			return DBResponse.generateInfo("Request with wrong format");
		}
		
		//apply encryption
		if(mDbConf.encryptKey!=null){
			request.applyEncryption(mModelConf.objects, mDbConf.encryptKey);
		}
		
		if(dbDriver!=null){
			DBResponse response = dbDriver.request(request);
			if(response!=null){
				return response;
			}else{
				return DBResponse.generateInfo("There was an error executing the request");
			}
		}
		
		return DBResponse.generateInfo("Driver not initialized correctly, restart to check errors");
	}
	
	public void disonnectDb(){
		if(dbDriver!=null){
			dbDriver.disconnect();
		}
	}
		
}
