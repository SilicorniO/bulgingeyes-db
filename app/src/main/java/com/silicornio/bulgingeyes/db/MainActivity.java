package com.silicornio.bulgingeyes.db;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silicornio.bulgingeyes.db.DBRequest.DBFactoryRequest;
import com.silicornio.bulgingeyes.db.db.DbConf;
import com.silicornio.bulgingeyes.db.general.JEL;
import com.silicornio.bulgingeyes.db.model.ModelConf;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){

        JEL.showLogs = true;

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
            JEL.i(setting.toString());

            DBFactoryRequest.deleteObject(dbController, Setting.class, "name8");

        }else{
            JEL.i("no setting");
        }

        dbController.disonnectDb();
    }
}
