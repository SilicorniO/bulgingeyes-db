package com.silicornio.googlyeyes.dbandexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silicornio.googlyeyes.dband.GEDBController;
import com.silicornio.googlyeyes.dband.GEDBObjectFactory;
import com.silicornio.googlyeyes.dband.GEDbConf;
import com.silicornio.googlyeyes.dband.general.GEDBUtils;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dband.GEModelConf;
import com.silicornio.googlyeyes.dbandexample.model.GEMessage;
import com.silicornio.googlyeyes.dbandexample.model.GEMessageText;
import com.silicornio.quepotranslator.general.QPL;

public class MainActivity extends AppCompatActivity {

    private GEDBController mDbController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        GEL.showLogs = true;
        QPL.showLogs = true;

        GEDbConf dbConf = GEDBUtils.readConfObjectFromAssets(this, "jedb/db.conf", GEDbConf.class);
        GEModelConf modelConf = GEDBUtils.readConfObjectFromAssets(this, "jedb/model.conf", GEModelConf.class);

        deleteDatabase(dbConf.name);

        mDbController = new GEDBController(dbConf, modelConf);
        mDbController.connectDb(this);

        //-----

        GEMessageText messageTest = new GEMessageText("message");
        GEMessage message = new GEMessage("title1", messageTest);

        //add object
        GEMessage messageAdd = GEDBObjectFactory.addObject(mDbController, message);

        GEL.d("MessageAdd: " + messageAdd.getText().getText());


        //-----

        mDbController.disconnectDb();
    }

}
