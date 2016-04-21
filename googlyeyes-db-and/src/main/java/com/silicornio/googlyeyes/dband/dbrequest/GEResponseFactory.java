package com.silicornio.googlyeyes.dband.dbrequest;

import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dband.model.GEModelFactory;
import com.silicornio.googlyeyes.dband.model.GEModelObject;
import com.silicornio.googlyeyes.dband.model.GEModelObjectAttribute;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SilicorniO
 */
public class GEResponseFactory {

    /**
     * Convert all the fields that should be a date
     * @param request GERequest to know the type of object requested
     * @param response GEResponse with the map or maps to to check
     * @param models GEModelObject[] array with all model objects
     */
    public static void convertStringDatesToObject(GERequest request, GEResponse response, GEModelObject[] models){

        //check there are results
        if(response.numResults==0){
            return;
        }

        //get the object requested
        GEModelObject modelObject = GEModelFactory.findObject(request.modelObject, models);
        if(modelObject==null){
            return;
        }

        //prepare the list of maps to check
        List<Map<String, Object>> maps = response.results;
        if(response.numResults==1){
            maps = new ArrayList<>();
            maps.add(response.result);
        }

        //check each map
        for(Map<String, Object> map : maps){

            //check all values
            convertStringDatesToObject(modelObject, map, models);
        }
    }

    /**
     * Convert all the fields that should be a date
     * @param model GEModelObject used in request
     * @param map Map<String, Object> with values
     * @param models GEModelObject[] array with all model objects
     */
    public static void convertStringDatesToObject(GEModelObject model, Map<String, Object> map, GEModelObject[] models){

        //check each map
        for(GEModelObjectAttribute modAttr : model.attributes){

            //check the value is not null
            Object value = map.get(modAttr.name);
            if(value!=null) {

                //check if it is a date or another model
                if (GEModelObjectAttribute.TYPE_DATE.equalsIgnoreCase(modAttr.type)) {

                    //check if value of the map has a value
                    if (value instanceof String) {
                        //convert value to date with the format received
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(modAttr.format);
                            map.put(modAttr.name, sdf.parse((String) value));
                        } catch (Exception e) {
                            GEL.e("Exception converting date of attribute '" + modAttr.name + "' with format '" + modAttr.format + "' from String: " + e.toString());
                        }
                    }

                } else if (modAttr.isObjectType() && value instanceof Map) {

                    //get the model and try to convert the map
                    GEModelObject modelRef = GEModelFactory.findObject(modAttr.type, models);

                    if(modelRef != null){
                        convertStringDatesToObject(modelRef, (Map<String, Object>)value, models);
                    }

                }
            }

        }
    }
}
