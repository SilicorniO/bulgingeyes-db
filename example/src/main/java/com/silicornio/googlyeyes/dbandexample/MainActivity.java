package com.silicornio.googlyeyes.dbandexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silicornio.googlyeyes.dband.GEDBController;
import com.silicornio.googlyeyes.dband.GEDBObjectFactory;
import com.silicornio.googlyeyes.dband.GEDbConf;
import com.silicornio.googlyeyes.dband.GEModelConf;
import com.silicornio.googlyeyes.dband.general.GEDBUtils;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dbandexample.model.GEMessageData;
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
        dbConf.encryptKey = "secretpass2";
        GEModelConf modelConf = GEDBUtils.readConfObjectFromAssets(this, "jedb/model.conf", GEModelConf.class);

        deleteDatabase(dbConf.name);

        mDbController = new GEDBController(dbConf, modelConf);
        mDbController.connectDb(this);

        //-----

        GEMessageData messageData = new GEMessageData("msg1");
        messageData.setTag("tag1");
        messageData.setState(1);

        GEMessageText messageData2 = new GEMessageText("msg2");
        messageData.setData(messageData2);

//        GEMessageText messageData3 = new GEMessageData("msg3");
//        messageData2.setData(messageData3);

        //add object
        GEMessageData messageDataAdd = GEDBObjectFactory.addObject(mDbController, messageData);
        GEL.d("MessageAdd: " + messageDataAdd.toString());

        messageData.setState(2);

        GEMessageData messageDataUpdate = GEDBObjectFactory.updateObject(mDbController, messageData);
        GEL.d("MessageUpdate: " + messageDataUpdate.toString());


        //-----

        mDbController.disconnectDb();
    }

}
