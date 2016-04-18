package com.silicornio.googlyeyes.dbandexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silicornio.googlyeyes.dband.GEDBController;
import com.silicornio.googlyeyes.dband.GEDBObjectFactory;
import com.silicornio.googlyeyes.dband.db.GEDbConf;
import com.silicornio.googlyeyes.dband.general.GEDBUtils;
import com.silicornio.googlyeyes.dband.general.GEL;
import com.silicornio.googlyeyes.dband.model.GEModelConf;
import com.silicornio.googlyeyes.dbandexample.model.GEMessage;
import com.silicornio.googlyeyes.dbandexample.model.GEMessageText;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){

        GEL.showLogs = true;

        GEDbConf dbConf = GEDBUtils.readConfObjectFromAssets(this, "jedb/db.conf", GEDbConf.class);
        GEModelConf modelConf = GEDBUtils.readConfObjectFromAssets(this, "jedb/model.conf", GEModelConf.class);

        deleteDatabase(dbConf.name);

        GEDBController dbController = new GEDBController(dbConf, modelConf);
        dbController.connectDb(this);

        //generate objects to use in tests
        GEMessageText messageText = new GEMessageText("text of title1");
        GEMessage message = new GEMessage("title1", messageText);

        //add object
        GEMessage messageAdd = GEDBObjectFactory.addObject(dbController, message);
        GEL.i("ADD: " + messageAdd.toString());

        //get the object
        GEMessage messageGet = GEDBObjectFactory.getOneObject(dbController, GEMessage.class, "1");
        if(messageGet!=null) {

            //show the
            GEL.i("GET: " + messageGet.toString());

            //remove the object
            GEDBObjectFactory.deleteObject(dbController, GEMessage.class, "title1");

        }else{
            GEL.i("No message");
        }

        dbController.disonnectDb();
    }
}
