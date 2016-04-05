package com.silicornio.bulgingeyes.db.drivers;


import android.content.Context;

import com.silicornio.bulgingeyes.db.DBRequest.DBRequest;
import com.silicornio.bulgingeyes.db.DBRequest.DBResponse;
import com.silicornio.bulgingeyes.db.db.DbConf;
import com.silicornio.bulgingeyes.db.model.ModelObject;

public interface DBDriver {
	
	/**
	 * Initialize the object getting all information necessary for future calls
	 * @param allModels ModelObject[] list of all models to read information for attributes
	 */
	void init(ModelObject[] allModels);
	
	/**
	 * Connect to the database
	 * @param context Context to connect
	 * @param dbConf DbConf with all the information to connect
	 * @return boolean TRUE if database was connected
	 */
	boolean connect(Context context, DbConf dbConf);
	
	/**
	 * Disconnect the database
	 */
	void disconnect();
	
	/**
	 * Check if the model exists in the database
	 * @param model ModelObject Model to check if exists into the database
	 * @return boolean TRUE if exists, FALSE if not
	 */
	boolean modelExists(ModelObject model);
	
	/**
	 * Create the model received
	 * @param model ModelObject to create
	 */
	void createModel(ModelObject model);
	
	/**
	 * Update the model received
	 * @param model ModelObject to update
	 * @return boolean TRUE if was updated, FALSE if there was an error
	 */
	boolean updateModel(ModelObject model);
	
	/**
	 * Delete the model received
	 * @param model ModelObject to delete
	 */
	void deleteModel(ModelObject model);
	
	/**
	 * Execute a request
	 * @param request DBRequest received
	 */
	DBResponse request(DBRequest request);
}
