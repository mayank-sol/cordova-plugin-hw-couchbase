package com.medicalrecords;

import android.content.Context;

import com.couchbase.lite.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class MedicalRecords extends CordovaPlugin {

    private Database database;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Context context = cordova.getContext();
        com.medicalrecords.DatabaseManager.getSharedInstance(context);
        this.database = com.medicalrecords.DatabaseManager.getDatabase();
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        if ("queryMedicalRecords".equals(action)) {
            List<Result> resultList = queryMedicalRecords();
            ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
            assert resultList != null;
            for (Result result : resultList) {
                Map<String, Object> map = result.toMap();
                arrayList.add(map);
            }
            JSONArray jsonArray = new JSONArray(arrayList);
            callbackContext.success(jsonArray);
            return true;
        }

        if ("getRecord".equals(action)) {
            try {
                Document doc = getRecord(args.getString(0));

                if (doc != null) {
                    JSONObject jsonObject = new JSONObject(doc.toMap());
                    callbackContext.success(jsonObject);
                } else callbackContext.error("null");

            } catch (final Exception e) {
                callbackContext.error(e.getMessage());
            }

        }

        if ("addRecord".equals(action)) {
            try {
                String message = addRecord(args.getString(0), args.getString(1));

                callbackContext.success(message);
            } catch (final Exception e) {
                callbackContext.error(e.getMessage());
            }
        }

        return false;
    }

    private List<Result> queryMedicalRecords() {

        Query query = QueryBuilder
                .select(
                        SelectResult.all()
                )
                .from(DataSource.database(database));

        try {
            ResultSet resultSet = query.execute();
            return resultSet.allResults();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Document getRecord(String id) {
        try {
            return database.getDocument(id);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String addRecord(String id, String jsonString) {
        try {

            JSONObject jsonObject = new JSONObject(jsonString);
            Document document = database.getDocument(id);

            MutableDictionary dictionary = new MutableDictionary();
            dictionary = convertFromJsonObj(dictionary, jsonObject);

            if (document == null) {

                MutableDocument mutableDocument = new MutableDocument(id);

                for (String key:dictionary.getKeys()) {

                    if (dictionary.getValue(key) !=null) {
                        // do something with jsonObject here
                        Map<String, Object> temp = new HashMap<String, Object>();
                        temp.put(key,dictionary.getValue(key));
                        mutableDocument.setData(temp);
                    }
                }

                database.save(mutableDocument);

                return "Added record successfully";

            } else {
                MutableDocument mutableDocument = document.toMutable();

                mutableDocument.setData(dictionary.toMap());

                database.save(mutableDocument);

                return "Updated record successfully";
            }

        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MutableDictionary convertFromJsonObj(MutableDictionary m, JSONObject obj){
        try {
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String key = keys.next();

                // do something with jsonObject here
                if ((obj.get(key) instanceof JSONObject)) {

                    MutableDictionary temp = new MutableDictionary();
                    temp = convertFromJsonObj(temp,((JSONObject)obj.get(key)));
                    m.setValue(key,temp);

                } else if ((obj.get(key) instanceof JSONArray)) {

//          MutableDictionary arrayDictionary = new MutableDictionary();
                    MutableArray array = new MutableArray();

                    JSONArray jsonArray = obj.getJSONArray(key);

                    for(int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject object = jsonArray.getJSONObject(i);
                        MutableDictionary temp = new MutableDictionary();
                        temp = convertFromJsonObj(temp, object);

                        array.addDictionary(temp);
                    }

                    m.setValue(key, array);

                } else if (obj.isNull(key)) {
                    System.out.println(key);
                    m.setValue(key, null);
                } else {
                    m.setValue(key, obj.get(key));
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return m;
    }

}
