package com.silicornio.googlyeyes.dbandexample;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.silicornio.googlyeyes.dband.GEDBController;
import com.silicornio.googlyeyes.dband.GEDBObjectFactory;
import com.silicornio.googlyeyes.dband.GEDbConf;
import com.silicornio.googlyeyes.dband.GEModelConf;
import com.silicornio.googlyeyes.dband.GEModelFactory;
import com.silicornio.googlyeyes.dband.GERequest;
import com.silicornio.googlyeyes.dband.GERequestOperator;
import com.silicornio.googlyeyes.dband.GEResponse;
import com.silicornio.googlyeyes.dband.general.GEDBUtils;
import com.silicornio.googlyeyes.dbandexample.model.GEMessage;
import com.silicornio.googlyeyes.dbandexample.model.GEMessageData;
import com.silicornio.googlyeyes.dbandexample.model.GEMessageEncrypt;
import com.silicornio.googlyeyes.dbandexample.model.GEMessageText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by SilicorniO
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class GooglyEyesTests {

    Context mMockContext;

    GEDBController mDbController;

    @Before
    public void setUp() {

        mMockContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");

        GEDbConf dbConf = GEDBUtils.readConfObjectFromAssets(mMockContext, "jedb/db.conf", GEDbConf.class);
        GEModelConf modelConf = GEDBUtils.readConfObjectFromAssets(mMockContext, "jedb/model.conf", GEModelConf.class);
        mDbController = new GEDBController(dbConf, modelConf);
        mDbController.connectDb(mMockContext);

    }

    //----- OBJECTS TESTS -----

    @Test
    public void test001AddObject(){

        //generate objects to use in tests
        GEMessage message = new GEMessage("title1", null);

        //add object
        GEMessage messageAdd = GEDBObjectFactory.addObject(mDbController, message);

        //compare object received with its identifier (title)
        assertEquals(message.getTitle(), messageAdd.getTitle());
    }

    @Test
    public void test002GetObject(){

        //generate objects to use in tests
        GEMessage message = new GEMessage("title1", null);

        //add object
        GEDBObjectFactory.addObject(mDbController, message);

        //get object
        GEMessage messageGet = GEDBObjectFactory.getOneObject(mDbController, GEMessage.class, "title1");

        //compare identifiers of objects (title)
        assertEquals(message.getTitle(), messageGet.getTitle());
    }

    @Test
    public void test003UpdateObject(){

        //generate objects to use in tests including the field to modify
        GEMessage message = new GEMessage("title1", null);
        message.setTag("tag1");

        //add object
        GEDBObjectFactory.addObject(mDbController, message);

        //update object
        GEDBObjectFactory.updateObject(mDbController, message);

        //get the object
        GEMessage messageGet = GEDBObjectFactory.getOneObject(mDbController, GEMessage.class, "title1");

        //compare modified field (tag)
        assertEquals(message.getTag(), messageGet.getTag());
    }

    @Test
    public void test004DeleteObject(){

        //generate objects to use in tests
        GEMessage message = new GEMessage("title1", null);

        //add object
        GEDBObjectFactory.addObject(mDbController, message);

        //delete the object
        assertTrue(GEDBObjectFactory.deleteObject(mDbController, GEMessage.class, "title1")>0);

        //get the object and should be null
        assertNull(GEDBObjectFactory.getOneObject(mDbController, GEMessage.class, "title1"));
    }

    @Test
    public void test005InsertAndSelectBoolean(){

        //generate objects to use in tests
        GEMessage message = new GEMessage("title1", null);
        message.setCheckBoolean(true);

        //add object
        GEMessage messageDb = GEDBObjectFactory.addObject(mDbController, message);

        //check the boolean
        assertTrue(messageDb.isCheckBoolean());
    }

    @Test
    public void test006SelectBoolean(){

        //generate objects to use in tests
        GEMessage message = new GEMessage("title1", null);
        message.setCheckBoolean(true);

        //add object
        GEDBObjectFactory.addObject(mDbController, message);

        //get the object
        GERequest request = new GERequest(GERequest.TYPE_GET, "GEMessage");
        request.operators.add(new GERequestOperator("checkBoolean", "=", GERequestOperator.VALUE_TRUE));
        GEResponse response = mDbController.request(request);

        //get the object and should be null
        assertTrue(((Boolean)response.result.get("checkBoolean")).booleanValue());
    }

    @Test
    public void test007Encrypt(){

        //generate objects to use in tests
        GEMessageEncrypt message = new GEMessageEncrypt("title007");

        //add object
        GEDBObjectFactory.addObject(mDbController, message);

        //get the object
        GERequest request = new GERequest(GERequest.TYPE_GET, "GEMessageEncrypt");
        request.operators.add(new GERequestOperator("title", "=", "title007"));
        GEResponse response = mDbController.request(request);

        //get the object and should be null
        assertEquals(response.result.get("title"), "title007");
    }

    @Test
    public void test008JsonDataUpdateId(){

        GEMessageData messageData = new GEMessageData("msg1");
        messageData.setTag("tag1");

        //add object
        GEMessageData messageDataAdd = GEDBObjectFactory.addObject(mDbController, messageData);

        //change identifier
        GERequest request = new GERequest(GERequest.TYPE_UPDATE, GEMessageData.class.getSimpleName());
        request.operators.add(new GERequestOperator("title", "=", "msg1"));
        request.value.put("title", "msg2");
        mDbController.request(request);

        //get the same data with the new identifier
        GEMessageData messageDataGet2 = GEDBObjectFactory.getOneObject(mDbController, GEMessageData.class, "msg2");

        //get the object and should be null
        assertEquals(messageDataGet2.getTag(), "tag1");
    }

    //----- END OBJECTS TESTS -----

    //----- NESTED OBJECTS TESTS -----

    @Test
    public void test101AddNestedObject(){

        //generate objects to use in tests
        GEMessageText messageTest = new GEMessageText("message");
        GEMessage message = new GEMessage("title1", messageTest);

        //add object
        GEMessage messageAdd = GEDBObjectFactory.addObject(mDbController, message);

        //compare a value from nested object
        assertEquals(message.getText().getText(), messageAdd.getText().getText());
    }

    @Test
    public void test102GetNestedObject(){

        //generate objects to use in tests
        GEMessageText messageTest = new GEMessageText("message");
        GEMessage message = new GEMessage("title1", messageTest);

        //add object
        GEDBObjectFactory.addObject(mDbController, message);

        //get object
        GEMessage messageGet = GEDBObjectFactory.getOneObject(mDbController, GEMessage.class, "title1");

        //compare a value from nested object
        assertEquals(message.getText().getText(), messageGet.getText().getText());
    }

    //----- END NESTED OBJECTS TESTS -----

    //----- REQUEST TESTS -----

    @Test
    public void test201RequestOperatorsLogic(){

        //generate objects to use in tests
        GEMessage message1 = new GEMessage("title1", null);
        message1.setTag("tag");
        message1.setNumViews(1);
        GEMessage message2 = new GEMessage("title2", null);
        message2.setTag("tag");
        message2.setNumViews(2);

        //add object
        GEDBObjectFactory.addObject(mDbController, message1);
        GEDBObjectFactory.addObject(mDbController, message2);

        //generate a raw request
        GERequest getRequest = new GERequest(GERequest.TYPE_GET, GEMessage.class.getSimpleName());
        getRequest.operators.add(new GERequestOperator("tag", "=", "tag"));
        getRequest.operators.add(new GERequestOperator("numViews", "=", "1"));
        getRequest.operatorsLogic = "0 || 1";

        //execute
        GEResponse response = mDbController.request(getRequest);

        //read the response
        List<GEMessage> messageGet = GEDBObjectFactory.getObjectsResponse(response, GEModelFactory.findObject("GEMessage", mDbController.getModelConf().getObjects()), GEMessage.class, mDbController);

        //compare object received with its identifier (title)
        assertEquals(messageGet.size(), 2);
    }

    @Test
    public void test202RequestOperatorsLogic(){

        //generate objects to use in tests
        GEMessage message1 = new GEMessage("title1", null);
        message1.setTag("tag");
        message1.setNumViews(1);
        GEMessage message2 = new GEMessage("title2", null);
        message2.setTag("tag");
        message2.setNumViews(2);

        //add object
        GEDBObjectFactory.addObject(mDbController, message1);
        GEDBObjectFactory.addObject(mDbController, message2);

        //generate a raw request
        GERequest getRequest = new GERequest(GERequest.TYPE_GET, GEMessage.class.getSimpleName());
        getRequest.operators.add(new GERequestOperator("tag", "=", "tag"));
        getRequest.operators.add(new GERequestOperator("numViews", "=", "1"));
        getRequest.operatorsLogic = "numViews && tag";

        //execute
        GEResponse response = mDbController.request(getRequest);

        //read the response
        List<GEMessage> messageGet = GEDBObjectFactory.getObjectsResponse(response, GEModelFactory.findObject("GEMessage", mDbController.getModelConf().getObjects()), GEMessage.class, mDbController);

        //compare object received with its identifier (title)
        assertEquals(messageGet.size(), 1);
    }

    //----- END REQUEST TESTS -----

    //----- GENERAL TESTS -----

    @Test
    public void test500RawGetOneRequest(){

        //generate objects to use in tests
        GEMessage message = new GEMessage("title1", null);

        //add object
        GEDBObjectFactory.addObject(mDbController, message);

        //generate a raw request
        GERequest rawRequest = new GERequest(GERequest.TYPE_RAW, null);
        rawRequest.raw = "SELECT * FROM GEMessage WHERE title='title1'";

        //execute
        GEResponse response = mDbController.request(rawRequest);

        //read the response
        GEMessage messageGet = GEDBObjectFactory.getOneObjectResponse(response, GEModelFactory.findObject("GEMessage", mDbController.getModelConf().getObjects()), GEMessage.class, mDbController);

        //compare object received with its identifier (title)
        assertEquals(message.getTitle(), messageGet.getTitle());
    }

    @Test
    public void test501RawGetMultipleRequest(){

        //generate objects to use in tests
        GEMessage message1 = new GEMessage("title1", null);
        GEMessage message2 = new GEMessage("title2", null);

        //add object
        GEDBObjectFactory.addObject(mDbController, message1);
        GEDBObjectFactory.addObject(mDbController, message2);

        //generate a raw request
        GERequest rawRequest = new GERequest(GERequest.TYPE_RAW, null);
        rawRequest.raw = "SELECT * FROM GEMessage";

        //execute
        GEResponse response = mDbController.request(rawRequest);

        //read the response
        List<GEMessage> messageGet = GEDBObjectFactory.getObjectsResponse(response, GEModelFactory.findObject("GEMessage", mDbController.getModelConf().getObjects()), GEMessage.class, mDbController);

        //compare object received with its identifier (title)
        assertEquals(messageGet.size(), 2);
    }

    //----- END GENERAL TESTS -----
}
