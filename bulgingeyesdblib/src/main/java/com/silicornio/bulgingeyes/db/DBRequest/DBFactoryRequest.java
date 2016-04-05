package com.silicornio.bulgingeyes.db.DBRequest;

import android.util.Pair;

import com.google.gson.Gson;
import com.silicornio.bulgingeyes.db.DbController;
import com.silicornio.bulgingeyes.db.general.JEL;
import com.silicornio.bulgingeyes.db.model.ModelFactory;
import com.silicornio.bulgingeyes.db.model.ModelObject;
import com.silicornio.bulgingeyes.db.model.ModelObjectAttribute;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBFactoryRequest {

    //--------------- OBJECTS ----------
    //----- GET -----

    /**
     * Get the object with the identifier received
     * @param dbController DbController to read model and execute queries
     * @param objectClass  Class of the object to find in the model (same name)
     * @param sId String identifier
     * @return boolean TRUE if deleted one or more elements, FALSE if error or not deleted any element
     */
    public static <T>T getOneObject(DbController dbController, Class<T> objectClass, String sId){

        Pair<ModelObject, ModelObjectAttribute> modelAndId = getModelAndId(dbController, objectClass);
        if(modelAndId!=null) {
            DBResponse response = getObjects(dbController, modelAndId.first.name, objectClass, new String[]{modelAndId.second.name}, new String[]{sId});
            return getOneObjectResponse(response, modelAndId.first, objectClass);
        }else{
            return null;
        }

    }

    /**
     * Get objects with the request received
     * @param dbController DbController to read model and execute queries
     * @param dbRequest DBRequest to execute
     * @param objectClass  Class of the object to find in the model (same name)
     * @return boolean TRUE if deleted one or more elements, FALSE if error or not deleted any element
     */
    public static <T>List<T> executeRequestListResponse(DbController dbController, DBRequest dbRequest, Class<T> objectClass){

        if(dbController==null || dbRequest==null || objectClass==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model that will be used to convert the response to an object
        ModelObject modelObject = ModelFactory.findObject(dbRequest.modelObject, dbController.getModelConf().objects);
        if(modelObject==null){
            JEL.e("Model '" + dbRequest.modelObject + "' not found in the list of models");
            return null;
        }

        //execute the request
        DBResponse response = dbController.request(dbRequest);
        return getObjectsResponse(response, modelObject, objectClass);
    }

    /**
     * Get objects with the request received
     * @param dbController DbController to read model and execute queries
     * @param dbRequest DBRequest to execute
     * @param objectClass  Class of the object to find in the model (same name)
     * @return boolean TRUE if deleted one or more elements, FALSE if error or not deleted any element
     */
    public static <T>T executeRequestOneResponse(DbController dbController, DBRequest dbRequest, Class<T> objectClass){

        if(dbController==null || dbRequest==null || objectClass==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model that will be used to convert the response to an object
        ModelObject modelObject = ModelFactory.findObject(dbRequest.modelObject, dbController.getModelConf().objects);
        if(modelObject==null){
            JEL.e("Model '" + dbRequest.modelObject + "' not found in the list of models");
            return null;
        }

        //execute the request
        DBResponse response = dbController.request(dbRequest);
        return getOneObjectResponse(response, modelObject, objectClass);
    }


    //----- ADD -----

    /**
     * Add an object
     * @param dbController DbController to read model and execute queries
     * @param object Object to add
     * @param <T>
     * @return Object inserted or null if there was an error
     */
    public static <T>T addObject(DbController dbController, T object){

        if(object==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        DBResponse response = addObject(dbController, object.getClass().getSimpleName(), object);
        return getOneObjectResponse(response, ModelFactory.findObject(object.getClass().getSimpleName(), dbController.getModelConf().objects), (Class<T>)object.getClass());
    }

    //----- UPDATE -----

    /**
     * Update the object with the identifier received
     * @param dbController DbController to read model and execute queries
     * @param object  Object to update
     * @return boolean TRUE if updated one or more elements, FALSE if error or not deleted any element
     */
    public static <T>T updateObject(DbController dbController, T object){

        Pair<ModelObject, ModelObjectAttribute> modelAndId = getModelAndId(dbController, object.getClass());
        if(modelAndId!=null) {
            Object value = getReflectionValue(object, modelAndId.second.name);
            if(value!=null) {
                DBResponse response = updateObjects(dbController, modelAndId.first.name, object, new String[]{modelAndId.second.name}, new String[]{String.valueOf(value)});
                return getOneObjectResponse(response, modelAndId.first, (Class<T>) object.getClass());
            }else{
                return null;
            }
        }else{
            return null;
        }

    }


    //----- DELETE -----

    /**
     * Delete the object with the identifier received
     * @param dbController DbController to read model and execute queries
     * @param objectClass  Class of the object to find in the model (same name)
     * @param sId String identifier
     * @return boolean TRUE if deleted one or more elements, FALSE if error or not deleted any element
     */
    public static boolean deleteObject(DbController dbController, Class objectClass, String sId){

        Pair<ModelObject, ModelObjectAttribute> modelAndId = getModelAndId(dbController, objectClass);
        if(modelAndId!=null) {
            DBResponse response = deleteObjects(dbController, modelAndId.first.name, objectClass, new String[]{modelAndId.second.name}, new String[]{sId});
            return response.numResults>0;
        }else{
            return false;
        }

    }

    //----------- DBRESPONSES -----------

    public static DBResponse getObjects(DbController dbController, String modelName, Class objectClass, String[] attrNames, String[] attrValues){

        if(dbController==null || modelName==null || objectClass==null || attrNames==null || attrValues==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        ModelObject modelObject = ModelFactory.findObject(modelName, dbController.getModelConf().objects);
        if(modelObject==null){
            JEL.e("Model '" + modelName + "' not found in the list of models");
            return null;
        }

        //generate the request
        DBRequest request = new DBRequest(DBRequest.TYPE_GET, modelName);
        addOperatorsToRequest(request, attrNames, attrValues);

        //execute and get the response
        return dbController.request(request);
    }

    public static DBResponse updateObjects(DbController dbController, String modelName, Object object, String[] attrNames, String[] attrValues){

        if(dbController==null || modelName==null || object==null || attrNames==null || attrValues==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        ModelObject modelObject = ModelFactory.findObject(modelName, dbController.getModelConf().objects);
        if(modelObject==null){
            JEL.e("Model '" + modelName + "' not found in the list of models");
            return null;
        }

        //generate the request
        DBRequest request = new DBRequest(DBRequest.TYPE_UPDATE, modelName);

        //add all attributes values from the model to the request (not if it is autoincrement)
        addValuesToRequest(request, modelObject, object);

        //add operators
        addOperatorsToRequest(request, attrNames, attrValues);

        //execute and get the response
        return dbController.request(request);
    }

    public static DBResponse deleteObjects(DbController dbController, String modelName, Class objectClass, String[] attrNames, String[] attrValues){

        if(dbController==null || modelName==null || objectClass==null || attrNames==null || attrValues==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        ModelObject modelObject = ModelFactory.findObject(modelName, dbController.getModelConf().objects);
        if(modelObject==null){
            JEL.e("Model '" + modelName + "' not found in the list of models");
            return null;
        }

        //generate the request
        DBRequest request = new DBRequest(DBRequest.TYPE_DELETE, modelName);
        addOperatorsToRequest(request, attrNames, attrValues);

        //execute and get the response
        return dbController.request(request);
    }

    public static DBResponse addObject(DbController dbController, String modelName, Object object){

        if(dbController==null || modelName==null || object==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        ModelObject modelObject = ModelFactory.findObject(modelName, dbController.getModelConf().objects);
        if(modelObject==null){
            JEL.e("Model '" + modelName + "' not found in the list of models");
            return null;
        }

        //generate the request
        DBRequest request = new DBRequest(DBRequest.TYPE_ADD, modelName);

        //add all attributes values from the model to the request (not if it is autoincrement)
        addValuesToRequest(request, modelObject, object);

        //execute and get the response
        return dbController.request(request);
    }

    //----------- UTILS -----------

    /**
     * Add all the operators received in the request
     * @param request DBRequest where to add operators
     * @param attrNames String[] array of names of operators
     * @param attrValues String[] values of operators
     */
    private static void addOperatorsToRequest(DBRequest request, String[] attrNames, String[] attrValues){
        for(int i=0; i<attrNames.length && i<attrValues.length; i++) {

            //attribute
            String name = attrNames[i];
            String value = attrValues[i];

            //add the operator
            request.operators.add(new DBRequestOperator(name, "=", value));
        }
    }

    /**
     * Add all values of the object in the request. All attributes read from model
     * @param request DBRequest where to add
     * @param modelObject ModelObject to use to get attributes
     * @param object Object where read values
     */
    private static void addValuesToRequest(DBRequest request, ModelObject modelObject, Object object){

        //Prepare Gson to write and read JSON
        Gson gson = new Gson();

        //add all attributes values from the model to the request (not if it is autoincrement)
        for(ModelObjectAttribute moa : modelObject.attributes){
            if(!moa.autoincrement){
                if(!moa.objectJson) {
                    Object value = getReflectionValue(object, moa.name);
                    if(value!=null){
                        request.value.put(moa.name, value);
                    }
                }else{
                    //save the object as JSON in this attribute
                    request.value.put(moa.name, gson.toJson(object));
                }
            }
        }
    }

    /**
     * Get the model and its attribute as identifier
     * @param dbController DbController to read objects of the model
     * @param objectClass Class of the object to find in the model (same name)
     * @return Pair<ModelObject, ModelObjectAttribute> if it is found everything, else null
     */
    private static Pair<ModelObject, ModelObjectAttribute> getModelAndId(DbController dbController, Class objectClass){

        if(dbController==null || objectClass==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        ModelObject modelObject = ModelFactory.findObject(objectClass.getSimpleName(), dbController.getModelConf().objects);
        if(modelObject==null){
            JEL.e("Model '" + objectClass.getSimpleName() + "' not found in the list of models");
            return null;
        }

        //get the Id attribute of the object
        ModelObjectAttribute modelObjectAttribute = ModelFactory.findAttributeId(modelObject);
        if(modelObjectAttribute==null){
            JEL.e("Model '" + modelObject.name + "' has not got an attribute as ID");
            return null;
        }

        //return model and attribute found
        return new Pair<>(modelObject, modelObjectAttribute);
    }

    /**
     * Convert the response received in an object
     * @param response DBResponse to read
     * @param modelObject ModelObject reference
     * @param objectClass Class to use for convert to an object
     * @param <T>
     * @return Object translated or null if there was an error
     */
    public static <T>T getOneObjectResponse(DBResponse response, ModelObject modelObject, Class<T> objectClass){

        //Prepare Gson to write and read JSON
        Gson gson = new Gson();

        //convert the response to an object
        Map<String, Object> result;
        if(response.numResults==1) {
            result = response.result;
        }else if(response.numResults>1) {
            result = response.results.get(0);
        }else {
            return null;
        }

        //check if the model has a JSON attribute to return it
        ModelObjectAttribute attrObjectJson = ModelFactory.findAttributeObjectJson(modelObject);
        if(attrObjectJson!=null){
            return gson.fromJson((String)result.get(attrObjectJson.name), objectClass);
        }else {
            return gson.fromJson(gson.toJson(result), objectClass);
        }
    }

    /**
     * Convert the response received in a list of objects
     * @param response DBResponse to read
     * @param modelObject ModelObject reference
     * @param objectClass Class to use for convert to an object
     * @param <T>
     * @return List<T> List of objects or null if there was an error
     */
    public static <T>List<T> getObjectsResponse(DBResponse response, ModelObject modelObject, Class<T> objectClass){

        //Prepare Gson to write and read JSON
        Gson gson = new Gson();

        //convert the response to an object
        List<Map<String, Object>> results = new ArrayList<>();
        if(response.numResults==1) {
            results.add(response.result);
        }else if(response.numResults>1) {
            results.addAll(response.results);
        }else {
            return null;
        }

        //check if the model has a JSON attribute to return it
        ModelObjectAttribute attrObjectJson = ModelFactory.findAttributeObjectJson(modelObject);

        //generate the list of objects
        List<T> listResults = new ArrayList<>();
        for(Map<String, Object> result : results){
            if(attrObjectJson!=null){
                listResults.add(gson.fromJson((String)result.get(attrObjectJson.name), objectClass));
            }else{
                listResults.add(gson.fromJson(gson.toJson(result), objectClass));
            }
        }
        return listResults;
    }

    /**
     * Return the value of a variable in an object
     * @param object Object to read
     * @param varName String name of the variable
     * @return Object or null if not found
     */
    private static Object getReflectionValue(Object object, String varName){
        try {
            Field field = object.getClass().getDeclaredField(varName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            JEL.e("Object '" + object.getClass().getSimpleName() + "' hasn't got the field '" + varName + "' of the model: " + e.toString());
        }
        return null;
    }
}
