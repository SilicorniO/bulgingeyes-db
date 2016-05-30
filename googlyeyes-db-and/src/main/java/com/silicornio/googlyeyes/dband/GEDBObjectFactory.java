package com.silicornio.googlyeyes.dband;

import android.util.Pair;

import com.google.gson.Gson;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dband.general.GEReflectionUtils;
import com.silicornio.quepotranslator.QPCustomTranslation;
import com.silicornio.quepotranslator.QPTransManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Silicornio
 * Layer to work directly with objects.
 * NOTE: Model names have to be setted with the same name than the object
 */
public class GEDBObjectFactory {

    //----- GET -----

    /**
     * Get the unique object of a table (if there is more than one, the first one)
     * @param dbController DbController to read model and execute queries
     * @param objectClass  Class of the object to find in the model (same name)
     * @return T Object gotten from database or null if there was an error
     */
    public static <T>T getUniqueObject(GEDBController dbController, Class<T> objectClass){

        GEModelObject modelObject = GEModelFactory.findObject(objectClass.getSimpleName(), dbController.getModelConf().objects);
        if(modelObject!=null) {
            GERequest request = new GERequest(GERequest.TYPE_GET, modelObject.name);
            request.operators.add(new GERequestOperator(null, GERequestOperator.SYMBOL_LIMIT, "1"));
            request.nestedObjects = true;
            GEResponse response = dbController.request(request);
            return getOneObjectResponse(response, modelObject, objectClass, dbController);
        }else{
            return null;
        }

    }

    /**
     * Get the object with the identifier received
     * @param dbController DbController to read model and execute queries
     * @param objectClass  Class of the object to find in the model (same name)
     * @param sId String identifier
     * @return T Object gotten from database or null if there was an error
     */
    public static <T>T getOneObject(GEDBController dbController, Class<T> objectClass, String sId){

        if(dbController==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        Pair<GEModelObject, GEModelObjectAttribute> modelAndId = GERequestFactory.getModelAndId(dbController, objectClass);
        if(modelAndId!=null) {
            GERequest request = GERequestFactory.getObjects(
                    modelAndId.first.name,
                    new String[]{modelAndId.second.name},
                    new String[]{sId},
                    dbController.getModelConf().objects);
            request.nestedObjects = true;
            GEResponse response = dbController.request(request);
            return getOneObjectResponse(response, modelAndId.first, objectClass, dbController);
        }else{
            return null;
        }

    }

    /**
     * Get the object with the identifier received
     * @param dbController DbController to read model and execute queries
     * @param request GERequest to execute
     * @param classModel Class of the model for the casting
     * @return T Object gotten from database or null if there was an error
     */
    public static <T>T getOneObject(GEDBController dbController, GERequest request, Class<T> classModel){

        GEModelObject modelObject = GEModelFactory.findObject(request.modelObject, dbController.getModelConf().getObjects());
        return GEDBObjectFactory.getOneObjectResponse(dbController.request(request), modelObject, classModel, dbController);
    }

    /**
     * Get all the objects of the type received
     * @param dbController DbController to read model and execute queries
     * @param objectClass  Class of the object to find in the model (same name)
     * @return List<T> list of objects found or empty array
     */
    public static <T>List<T> getAllObjects(GEDBController dbController, Class<T> objectClass){
        return getObjects(dbController, objectClass, new String[]{}, new String[]{});
    }

    /**
     * Get the list of objects with the values given
     * @param dbController DbController to read model and execute queries
     * @param objectClass  Class of the object to find in the model (same name)
     * @param attrNames String[] with array of names of parameters to compare
     * @param attrValues String[] with array of values for parameters given
     * @return List<T> list of objects found or empty array
     */
    public static <T>List<T> getObjects(GEDBController dbController, Class<T> objectClass, String[] attrNames, String[] attrValues){

        if(dbController==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        GEModelObject modelObject = GEModelFactory.findObject(objectClass.getSimpleName(), dbController.getModelConf().objects);
        if(modelObject!=null) {
            GERequest request = GERequestFactory.getObjects(
                    modelObject.name,
                    attrNames,
                    attrValues,
                    dbController.getModelConf().objects);
            request.nestedObjects = true;
            GEResponse response = dbController.request(request);
            return getObjectsResponse(response, modelObject, objectClass, dbController);
        }else{
            return null;
        }
    }

    /**
     * Get the object with the identifier received
     * @param dbController DbController to read model and execute queries
     * @param request GERequest to execute
     * @param classModel Class of the model for the casting
     * @return T Object gotten from database or null if there was an error
     */
    public static <T>List<T> getObjects(GEDBController dbController, GERequest request, Class<T> classModel){

        GEModelObject modelObject = GEModelFactory.findObject(request.modelObject, dbController.getModelConf().getObjects());
        return GEDBObjectFactory.getObjectsResponse(dbController.request(request), modelObject, classModel, dbController);
    }

    //----- ADD -----

    /**
     * Add an object
     * @param dbController DbController to read model and execute queries
     * @param object Object to add
     * @return Object inserted or null if there was an error
     */
    public static <T>T addObject(GEDBController dbController, T object){

        if(dbController==null || object==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        GERequest request = GERequestFactory.addObject(object.getClass().getSimpleName(), object, dbController.getModelConf().objects);
        request.nestedObjects = true;
        GEResponse response = dbController.request(request);
        return getOneObjectResponse(response, GEModelFactory.findObject(object.getClass().getSimpleName(), dbController.getModelConf().objects), (Class<T>) object.getClass(), dbController);
    }

    //----- UPDATE -----

    /**
     * Add unique object. Try to update the object and create it if not exist
     * @param dbController DbController to read model and execute queries
     * @param object Object to add
     * @return Object inserted or null if there was an error
     */
    public static <T>T updateUniqueObject(GEDBController dbController, T object){

        if(dbController==null || object==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        GEResponse response = dbController.request(GERequestFactory.updateObjects(
                object.getClass().getSimpleName(),
                object,
                new String[]{},
                new String[]{},
                dbController.getModelConf().objects));
        if(response.numResults==0) {
            response = dbController.request(GERequestFactory.addObject(object.getClass().getSimpleName(), object, dbController.getModelConf().objects));
        }
        return getOneObjectResponse(response, GEModelFactory.findObject(object.getClass().getSimpleName(), dbController.getModelConf().objects), (Class<T>) object.getClass(), dbController);
    }

    /**
     * Update the object with the identifier received
     * @param dbController DbController to read model and execute queries
     * @param object  Object to update
     * @return boolean TRUE if updated one or more elements, FALSE if error or not deleted any element
     */
    public static <T>T updateObject(GEDBController dbController, T object){

        if(dbController==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        Pair<GEModelObject, GEModelObjectAttribute> modelAndId = GERequestFactory.getModelAndId(dbController, object.getClass());
        if(modelAndId!=null) {
            Object value = GEReflectionUtils.getReflectionValue(object, modelAndId.second.name);
            if(value!=null) {
                GEResponse response = dbController.request(GERequestFactory.updateObjects(
                        modelAndId.first.name,
                        object,
                        new String[]{modelAndId.second.name},
                        new String[]{String.valueOf(value)},
                        dbController.getModelConf().objects));
                return getOneObjectResponse(response, modelAndId.first, (Class<T>) object.getClass(), dbController);
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
     * @return int number of rows affected
     */
    public static int deleteObject(GEDBController dbController, Class objectClass, String sId){

        if(dbController==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        Pair<GEModelObject, GEModelObjectAttribute> modelAndId = GERequestFactory.getModelAndId(dbController, objectClass);
        if(modelAndId!=null) {
            GEResponse response = dbController.request(
                    GERequestFactory.deleteObjects(modelAndId.first.name,
                            new String[]{modelAndId.second.name},
                            new String[]{sId},
                            dbController.getModelConf().objects));
            return response.numResults;
        }else{
            return 0;
        }

    }

    /**
     * Delete the object with the identifier received
     * @param dbController DbController to read model and execute queries
     * @param objectClass  Class of the object to find in the model (same name)
     * @param attrNames String[] with array of names of parameters to compare
     * @param attrValues String[] with array of values for parameters given
     * @return boolean TRUE if deleted one or more elements, FALSE if error or not deleted any element
     */
    public static int deleteObjects(GEDBController dbController, Class objectClass, String[] attrNames, String[] attrValues){

        if(dbController==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        GEModelObject modelObject = GEModelFactory.findObject(objectClass.getSimpleName(), dbController.getModelConf().objects);
        if(modelObject!=null) {
            GEResponse response = dbController.request(GERequestFactory.deleteObjects(modelObject.name, attrNames, attrValues, dbController.getModelConf().objects));
            return response.numResults;
        }else{
            return 0;
        }

    }

    //----- REQUESTS -----

    /**
     * Get objects with the request received
     * @param dbController DbController to read model and execute queries
     * @param dbRequest DBRequest to execute
     * @param objectClass  Class of the object to find in the model (same name)
     * @return boolean TRUE if deleted one or more elements, FALSE if error or not deleted any element
     */
    public static <T>List<T> executeRequestListResponse(GEDBController dbController, GERequest dbRequest, Class<T> objectClass){

        if(dbController==null || dbRequest==null || objectClass==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model that will be used to convert the response to an object
        GEModelObject modelObject = GEModelFactory.findObject(dbRequest.modelObject, dbController.getModelConf().objects);
        if(modelObject==null){
            GEL.e("Model '" + dbRequest.modelObject + "' not found in the list of models");
            return null;
        }

        //execute the request
        GEResponse response = dbController.request(dbRequest);
        return getObjectsResponse(response, modelObject, objectClass, dbController);
    }

    /**
     * Get objects with the request received
     * @param dbController DbController to read model and execute queries
     * @param dbRequest DBRequest to execute
     * @param objectClass  Class of the object to find in the model (same name)
     * @return boolean TRUE if deleted one or more elements, FALSE if error or not deleted any element
     */
    public static <T>T executeRequestOneResponse(GEDBController dbController, GERequest dbRequest, Class<T> objectClass){

        if(dbController==null || dbRequest==null || objectClass==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model that will be used to convert the response to an object
        GEModelObject modelObject = GEModelFactory.findObject(dbRequest.modelObject, dbController.getModelConf().objects);
        if(modelObject==null){
            GEL.e("Model '" + dbRequest.modelObject + "' not found in the list of models");
            return null;
        }

        //execute the request
        GEResponse response = dbController.request(dbRequest);
        return getOneObjectResponse(response, modelObject, objectClass, dbController);
    }

    //----- UTILS -----

    /**
     * Convert the response received in an object
     * @param response DBResponse to read
     * @param modelObject ModelObject reference
     * @param objectClass Class to use for convert to an object
     * @param dbController GEDBController to get the list of objects of the model
     * @return Object translated or null if there was an error
     */
    public static <T>T getOneObjectResponse(GEResponse response, GEModelObject modelObject, Class<T> objectClass, GEDBController dbController){

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
        GEModelObjectAttribute attrObjectJson = GEModelFactory.findAttributeObjectJson(modelObject);
        if(attrObjectJson!=null){
            try {
                return gson.fromJson((String) result.get(attrObjectJson.name), objectClass);
            }catch(Exception e){
                GEL.e("Someone touched this registry in the database: Exception converting from JSON: " + e.toString());
                return null;
            }

        }else {

            //convert response date values to a Date object
            GEResponseFactory.convertStringDatesToObject(modelObject, result, dbController.getModelConf().objects);

            QPTransManager manager = new QPTransManager(null);
            manager.addCustomTranslation(mCustomTranslationDate);
            return manager.translate(result, objectClass);
        }
    }

    /**
     * Convert the response received in a list of objects
     * @param response DBResponse to read
     * @param modelObject ModelObject reference
     * @param objectClass Class to use for convert to an object
     * @param dbController GEDBController to get the list of objects of the model
     * @return List<T> List of objects or null if there was an error
     */
    public static <T>List<T> getObjectsResponse(GEResponse response, GEModelObject modelObject, Class<T> objectClass, GEDBController dbController){

        //Prepare Gson to write and read JSON
        Gson gson = new Gson();

        //list of objects to return
        List<T> listResults = new ArrayList<>();

        //convert the response to an object
        List<Map<String, Object>> results = new ArrayList<>();
        if(response.numResults==1) {
            results.add(response.result);
        }else if(response.numResults>1) {
            results.addAll(response.results);
        }else {
            return listResults;
        }

        //check if the model has a JSON attribute to return it
        GEModelObjectAttribute attrObjectJson = GEModelFactory.findAttributeObjectJson(modelObject);

        //generate the list of objects
        QPTransManager manager = new QPTransManager(null);
        for(Map<String, Object> result : results){

            //convert response date values to a Date object
            GEResponseFactory.convertStringDatesToObject(modelObject, result, dbController.getModelConf().objects);

            if(attrObjectJson!=null){
                listResults.add(gson.fromJson((String)result.get(attrObjectJson.name), objectClass));
            }else{
                listResults.add(manager.translate(result, objectClass));
            }
        }
        return listResults;
    }


    private static QPCustomTranslation<Calendar, Date> mCustomTranslationDate = new QPCustomTranslation<Calendar, Date>() {
        @Override
        public Date onTranslation(Calendar c) {
            return c.getTime();
        }

        @Override
        public Calendar onTranslationInverse(Date d) {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c;
        }
    };

}
