package com.silicornio.googlyeyes.dbandexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silicornio.googlyeyes.dband.DBRequest.DBFactoryRequest;
import com.silicornio.googlyeyes.dband.DbController;
import com.silicornio.googlyeyes.dband.DbFactory;
import com.silicornio.googlyeyes.dband.Setting;
import com.silicornio.googlyeyes.dband.db.DbConf;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dband.model.ModelConf;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){

        GEL.showLogs = true;

        DbConf dbConf = DbFactory.readConfObjectFromAssets(this, "jedb/db.conf", DbConf.class);
        ModelConf modelConf = DbFactory.readConfObjectFromAssets(this, "jedb/model.conf", ModelConf.class);

        DbController dbController = new DbController(dbConf, modelConf);
        dbController.connectDb(this);

        Setting settingAdd = new Setting();
        settingAdd.setName("name8");
        settingAdd.setValue("value8_1");
        settingAdd.setValue2("value8_1_2");
        DBFactoryRequest.addObject(dbController, settingAdd);

        Setting setting = DBFactoryRequest.getOneObject(dbController, Setting.class, "name8");
        if(setting!=null) {
            GEL.i(setting.toString());

            DBFactoryRequest.deleteObject(dbController, Setting.class, "name8");

        }else{
            GEL.i("no setting");
        }

        dbController.disonnectDb();
    }
}
