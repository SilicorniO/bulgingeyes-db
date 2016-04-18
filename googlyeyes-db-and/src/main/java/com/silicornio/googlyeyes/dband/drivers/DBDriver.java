package com.silicornio.googlyeyes.dband.drivers;


import android.content.Context;

import com.silicornio.googlyeyes.dband.dbrequest.GERequest;
import com.silicornio.googlyeyes.dband.dbrequest.GEResponse;
import com.silicornio.googlyeyes.dband.db.GEDbConf;
import com.silicornio.googlyeyes.dband.model.GEModelObject;


public interface DBDriver {
	
	/**
	 * Initialize the object getting all information necessary for future calls
	 * @param allModels ModelObject[] list of all models to read information for attributes
	 */
	void init(GEModelObject[] allModels);
	
	/**
	 * Connect to the database
	 * @param context Context to connect
	 * @param dbConf DbConf with all the information to connect
	 * @return boolean TRUE if database was connected
	 */
	boolean connect(Context context, GEDbConf dbConf);
	
	/**
	 * Disconnect the database
	 */
	void disconnect();
	
	/**
	 * Check if the model exists in the database
	 * @param model ModelObject Model to check if exists into the database
	 * @return boolean TRUE if exists, FALSE if not
	 */
	boolean modelExists(GEModelObject model);
	
	/**
	 * Create the model received
	 * @param model ModelObject to create
	 */
	void createModel(GEModelObject model);
	
	/**
	 * Update the model received
	 * @param model ModelObject to update
	 * @return boolean TRUE if was updated, FALSE if there was an error
	 */
	boolean updateModel(GEModelObject model);
	
	/**
	 * Delete the model received
	 * @param model ModelObject to delete
	 */
	void deleteModel(GEModelObject model);
	
	/**
	 * Execute a request
	 * @param request DBRequest received
	 */
	GEResponse request(GERequest request);
}
