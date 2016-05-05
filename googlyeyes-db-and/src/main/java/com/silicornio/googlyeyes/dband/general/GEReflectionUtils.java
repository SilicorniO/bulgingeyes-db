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
            Field field = null;
            Class klass = object.getClass();
            do {
                try {
                    field = klass.getDeclaredField(varName);
                }catch(NoSuchFieldException nsfe){
                    klass = klass.getSuperclass();
                }
            }while(field==null && klass!=null);

            if(field!=null) {
                field.setAccessible(true);
                return field.get(object);
            }else{
                GEL.e("Object '" + object.getClass().getSimpleName() + "' hasn't got the field '" + varName + "' of the model");
            }

        } catch (Exception e) {
            GEL.e("Exception getting value from object '" + object.getClass().getSimpleName() + "' hasn't got the field '" + varName + "' of the model: " + e.toString());
        }
        return null;
    }

}
