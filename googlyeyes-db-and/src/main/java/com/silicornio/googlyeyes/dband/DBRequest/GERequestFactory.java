package com.silicornio.googlyeyes.dband.dbrequest;

import android.util.Pair;

import com.google.gson.Gson;
import com.silicornio.googlyeyes.dband.GEDBController;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dband.general.GEReflectionUtils;
import com.silicornio.googlyeyes.dband.model.GEModelFactory;
import com.silicornio.googlyeyes.dband.model.GEModelObject;
import com.silicornio.googlyeyes.dband.model.GEModelObjectAttribute;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GERequestFactory {


    /**
     * Get a list of object for attributes received
     * @param modelName String name of the model
     * @param attrNames String[] attributes
     * @param attrValues String[] values
     * @param objects GEModelObject[] array of objects
     * @return GERequest request to execute with GEDBController
     */
    public static GERequest getObjects(String modelName, String[] attrNames, String[] attrValues, GEModelObject[] objects){

        if(modelName==null || attrNames==null || attrValues==null || objects==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        GEModelObject modelObject = GEModelFactory.findObject(modelName, objects);
        if(modelObject==null){
            GEL.e("Model '" + modelName + "' not found in the list of models");
            return null;
        }

        //generate the request
        GERequest request = new GERequest(GERequest.TYPE_GET, modelName);
        addOperatorsToRequest(request, attrNames, attrValues);

        //execute and get the response
        return request;
    }

    /**
     * Update a list of object for attributes received
     * @param modelName String name of the model
     * @param object Object to use for Object conversion and to read values
     * @param attrNames String[] attributes
     * @param attrValues String[] values
     * @param objects GEModelObject[] array of objects
     * @return GERequest request to execute with GEDBController
     */
    public static GERequest updateObjects(String modelName, Object object, String[] attrNames, String[] attrValues, GEModelObject[] objects){

        if(modelName==null || object==null || attrNames==null || attrValues==null || objects==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        GEModelObject modelObject = GEModelFactory.findObject(modelName, objects);
        if(modelObject==null){
            GEL.e("Model '" + modelName + "' not found in the list of models");
            return null;
        }

        //generate the request
        GERequest request = new GERequest(GERequest.TYPE_UPDATE, modelName);

        //add all attributes values from the model to the request (not if it is autoincrement)
        request.value = getObjectMap(GERequest.TYPE_UPDATE, object, modelObject, objects);

        //add operators
        addOperatorsToRequest(request, attrNames, attrValues);

        //execute and get the response
        return request;
    }

    /**
     * Delete a list of object for attributes received
     * @param modelName String name of the model
     * @param attrNames String[] attributes
     * @param attrValues String[] values
     * @param objects GEModelObject[] array of objects
     * @return GERequest request to execute with GEDBController
     */
    public static GERequest deleteObjects(String modelName, String[] attrNames, String[] attrValues, GEModelObject[] objects){

        if( modelName==null || attrNames==null || attrValues==null || objects==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        GEModelObject modelObject = GEModelFactory.findObject(modelName, objects);
        if(modelObject==null){
            GEL.e("Model '" + modelName + "' not found in the list of models");
            return null;
        }

        //generate the request
        GERequest request = new GERequest(GERequest.TYPE_DELETE, modelName);
        addOperatorsToRequest(request, attrNames, attrValues);

        //execute and get the response
        return request;
    }

    /**
     * Add an object into the database
     * @param modelName String name of the model
     * @param object Object with information to add
     * @param objects GEModelObject[] array of objects
     * @return GERequest request to execute with GEDBController
     */
    public static GERequest addObject(String modelName, Object object, GEModelObject[] objects){

        if(modelName==null || object==null || objects==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        GEModelObject modelObject = GEModelFactory.findObject(modelName, objects);
        if(modelObject==null){
            GEL.e("Model '" + modelName + "' not found in the list of models");
            return null;
        }

        //generate the request
        GERequest request = new GERequest(GERequest.TYPE_ADD, modelName);

        //add all attributes values from the model to the request (not if it is autoincrement)
        request.value = getObjectMap(GERequest.TYPE_ADD, object, modelObject, objects);

        //execute and get the response
        return request;
    }

    //----------- UTILS -----------

    /**
     * Add all the operators received in the request
     * @param request DBRequest where to add operators
     * @param attrNames String[] array of names of operators
     * @param attrValues String[] values of operators
     */
    private static void addOperatorsToRequest(GERequest request, String[] attrNames, String[] attrValues){
        for(int i=0; i<attrNames.length && i<attrValues.length; i++) {

            //attribute
            String name = attrNames[i];
            String value = attrValues[i];

            //add the operator
            request.operators.add(new GERequestOperator(name, "=", value));
        }
    }

    /**
     * Get an object as a map using the model of objects
     * Only values in the model will be added to the map
     * @param requestType String type of request to generate the object
     * @param object Object from where get the values
     * @param modelObject ModelObject connected with object
     * @param modelObjects ModelObject[] array of models where to get nested objects
     * @return Map<String, Object> generated
     */
    private static Map<String, Object> getObjectMap(String requestType, Object object, GEModelObject modelObject, GEModelObject[] modelObjects){

        //return null if the object is null
        if(object==null){
            return null;
        }

        //Prepare Gson to write and read JSON
        Gson gson = new Gson();

        //create a map
        Map<String, Object> map = new HashMap<>();

        //for each object attribute of the model we add the value
        for(GEModelObjectAttribute moa : modelObject.attributes){
            if(!moa.autoincrement && !(GERequest.TYPE_UPDATE.equalsIgnoreCase(requestType) && moa.id)){
                if(!moa.objectJson) {
                    if(moa.isObjectType()){

                        //get the object model reference
                        GEModelObject moRef = GEModelFactory.findObject(moa.type, modelObjects);
                        if(moRef!=null) {
                            if(GERequest.TYPE_ADD.equalsIgnoreCase(requestType)) {
                                //add nested objects
                                map.put(moa.name, getObjectMap(requestType, GEReflectionUtils.getReflectionValue(object, moa.name), moRef, modelObjects));
                            }else{
                                //add the identifier of the nested object. Search for the attribute with the identifier
                                GEModelObjectAttribute moaRef = GEModelFactory.findAttributeId(moRef);
                                if(moaRef!=null){
                                    Object objectRef = GEReflectionUtils.getReflectionValue(object, moa.name);
                                    if(objectRef!=null){
                                        map.put(moa.name, GEReflectionUtils.getReflectionValue(objectRef, moaRef.name));
                                    }
                                }else{
                                    GEL.e("The identifier of the model '" + moRef.name + "' not exists and is nested object, add one");
                                }
                            }
                        }else{
                            GEL.e("Reference in attribute '" + moa.name + "' of model '" + modelObject.name + "' is not right, that model not exists. That value is not being converted");
                        }


                    }else if(moa.type.equalsIgnoreCase(GEModelObjectAttribute.TYPE_DATE)) {
                        String sDate = getStringDate(GEReflectionUtils.getReflectionValue(object, moa.name), moa.format);
                        if(sDate!=null){
                            map.put(moa.name, sDate);
                        }
                    }else{
                        map.put(moa.name, GEReflectionUtils.getReflectionValue(object, moa.name));
                    }
                }else{
                    //save the object as JSON in this attribute
                    map.put(moa.name, gson.toJson(object));
                }
            }
        }

        //return the map
        return map;
    }

    /**
     * Return the text of a Date or Calendar object
     * @param objectDate Object Calendar or Date
     * @param format String to apply
     * @return String
     */
    private static String getStringDate(Object objectDate, String format){

        //check object and format are not null
        if(objectDate==null || format==null){
            return null;
        }

        Date d;
        if(objectDate instanceof Date){
            d = (Date)objectDate;
        }else if(objectDate instanceof Calendar){
            d = ((Calendar) objectDate).getTime();
        }else{
            return null;
        }

        //return the result of apply format
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(d);
        }catch(Exception e){
            GEL.e("Exception converting date with format '" + format + "': " + e.toString());
        }
        return null;
    }

    /**
     * Get the model and its attribute as identifier
     * @param dbController DbController to read objects of the model
     * @param objectClass Class of the object to find in the model (same name)
     * @return Pair<ModelObject, ModelObjectAttribute> if it is found everything, else null
     */
    public static Pair<GEModelObject, GEModelObjectAttribute> getModelAndId(GEDBController dbController, Class objectClass){

        if(dbController==null || objectClass==null){
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        //get the model object
        GEModelObject modelObject = GEModelFactory.findObject(objectClass.getSimpleName(), dbController.getModelConf().objects);
        if(modelObject==null){
            GEL.e("Model '" + objectClass.getSimpleName() + "' not found in the list of models");
            return null;
        }

        //get the Id attribute of the object
        GEModelObjectAttribute modelObjectAttribute = GEModelFactory.findAttributeId(modelObject);
        if(modelObjectAttribute==null){
            GEL.e("Model '" + modelObject.name + "' has not got an attribute as ID");
            return null;
        }

        //return model and attribute found
        return new Pair<>(modelObject, modelObjectAttribute);
    }


}
