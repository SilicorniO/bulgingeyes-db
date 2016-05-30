package com.silicornio.googlyeyes.dbandexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silicornio.googlyeyes.dband.GEDBController;
import com.silicornio.googlyeyes.dband.GEDBObjectFactory;
import com.silicornio.googlyeyes.dband.GEDbConf;
import com.silicornio.googlyeyes.dband.GEModelConf;
import com.silicornio.googlyeyes.dband.GERequest;
import com.silicornio.googlyeyes.dband.GERequestOperator;
import com.silicornio.googlyeyes.dband.GEResponse;
import com.silicornio.googlyeyes.dband.general.GEDBUtils;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dbandexample.model.GEMessage;
import com.silicornio.googlyeyes.dbandexample.model.GEMessageEncrypt;
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

        GEMessageText messageTest = new GEMessageText("message");
        GEMessage message = new GEMessage("title1", null);
        message.setCheckBoolean(true);

        //add object
        GEMessage messageAdd = GEDBObjectFactory.addObject(mDbController, message);

        GEMessageEncrypt messageEncrypt = new GEMessageEncrypt("title1");
        GEDBObjectFactory.addObject(mDbController, messageEncrypt);

        //get object
        GERequest request = new GERequest(GERequest.TYPE_GET, "GEMessage");
        request.operators.add(new GERequestOperator("checkBoolean", "=", GERequestOperator.VALUE_TRUE));

        GEResponse response = mDbController.request(request);

        GEL.d("MessageAdd: " + response.toString());

        //-----

        mDbController.disconnectDb();
    }

}
