package com.silicornio.googlyeyes.dband;

import android.content.Context;

import com.silicornio.googlyeyes.dband.drivers.DBDriver;
import com.silicornio.googlyeyes.dband.drivers.sqlite.SQLiteDBDriver;
import com.silicornio.googlyeyes.dband.general.GEDBUtils;
import com.silicornio.googlyeyes.dband.general.GEL;

public class GEDBController {

	
	/** Database configuration **/
	protected GEDbConf mDbConf;
	
	/** Model configuration **/
	protected GEModelConf mModelConf;
		
	/** Driver to connect to database **/
	private DBDriver dbDriver;

	public GEDBController(GEDbConf dbConf, GEModelConf modelConf){

		if(dbConf==null || modelConf==null){
			throw new IllegalArgumentException("Parameters cannot be null");
		}

		//save data
		mDbConf = dbConf;
		mModelConf = modelConf;
	}

    /**
     * Return the model configuration used by the DbController
     * @return ModelConf used
     */
    public GEModelConf getModelConf(){
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
			GEL.e("Driver not valid, check the db.conf");
			return false;
		}
		
		//initialize the driver
		dbDriver.init(mModelConf.objects);
		
		//connect the database
		if(!dbDriver.connect(context, mDbConf)){
			GEL.e("Error connecting to database, check the parameters of database configuration file (db.conf)");
			return false;
		}
				
		//for each object create, update or delete the table associated
		for(GEModelObject m : mModelConf.objects){
			
			//check if model exists
			boolean modelExists = dbDriver.modelExists(m);
			
			if(modelExists){
				
				if(m.dbAction.equalsIgnoreCase(GEModelObject.ACTION_DELETE)){
					dbDriver.deleteModel(m);
					
				}else if(m.dbAction.equalsIgnoreCase(GEModelObject.ACTION_UPDATE)){
					dbDriver.updateModel(m);
				}
				
			}else if(m.dbAction!= GEModelObject.ACTION_DELETE){
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
	public GEResponse request(GERequest request){

		if(request==null){
			return GEResponse.generateInfo("Request with wrong format");
		}
		
		//apply encryption
		if(mDbConf.encryptKey!=null){
			request.applyEncryption(mModelConf.objects, mDbConf.encryptKey);
		}
		
		if(dbDriver!=null){
			GEDBUtils.startCounter("request");
			GEResponse response = dbDriver.request(request);
			GEL.i("Request executed in " + GEDBUtils.endCounter("request") + " ms");
			if(response!=null){
				return response;
			}else{
				return GEResponse.generateInfo("There was an error executing the request");
			}
		}
		
		return GEResponse.generateInfo("Driver not initialized correctly, restart to check errors");
	}

	/**
	 * Disconnect the database
	 */
	public void disconnectDb(){
		if(dbDriver!=null){
			dbDriver.disconnect();
		}
	}
		
}
