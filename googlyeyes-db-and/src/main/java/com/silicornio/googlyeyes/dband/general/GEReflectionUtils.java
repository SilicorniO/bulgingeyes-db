package com.silicornio.googlyeyes.dband.general;


import java.lang.reflect.Field;

public class GEReflectionUtils {

    /**
     * Return the value of a variable in an object
     * @param object Object to read
     * @param varName String name of the variable
     * @return Object or null if not found
     */
    public static Object getReflectionValue(Object object, String varName){

        try {
            Field field = object.getClass().getDeclaredField(varName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            GEL.e("Object '" + object.getClass().getSimpleName() + "' hasn't got the field '" + varName + "' of the model: " + e.toString());
        }
        return null;
    }
}
