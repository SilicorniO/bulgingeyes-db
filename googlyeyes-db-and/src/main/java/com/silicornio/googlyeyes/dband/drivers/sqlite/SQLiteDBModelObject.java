package com.silicornio.googlyeyes.dband.drivers.sqlite;

import com.silicornio.googlyeyes.dband.GEModelObject;
import com.silicornio.googlyeyes.dband.GEModelObjectAttribute;

public class SQLiteDBModelObject extends GEModelObject {

    public SQLiteDBModelObject(GEModelObject mo){
        this.name = mo.name;
        this.dbAction = mo.dbAction;
        this.attributes = mo.attributes;
    }

    /** List of attributes as object, this is for calculations **/
    public GEModelObjectAttribute[] attributesObject;

}
