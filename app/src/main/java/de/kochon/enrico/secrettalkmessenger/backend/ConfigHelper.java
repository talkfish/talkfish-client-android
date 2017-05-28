package de.kochon.enrico.secrettalkmessenger.backend;

import java.util.ArrayList;
import de.kochon.enrico.secrettalkmessenger.TFApp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ConfigHelper {

   
   public static final String CONFIG_KEY_NAME = "name";
   public static final String CONFIG_KEY_NAME_DEFAULT = "anonymous";
   
   public static final String CONFIG_KEY_BACKGROUND = "background";
   public static final String CONFIG_KEY_BACKGROUND_OPTION_MOBILE = "3G+WIFI";
   public static final String CONFIG_KEY_BACKGROUND_OPTION_WIFI = "WIFI";
   public static final String CONFIG_KEY_BACKGROUND_DEFAULT = CONFIG_KEY_BACKGROUND_OPTION_WIFI;

   public static final String CONFIG_KEY_FIRSTRUN = "firstrun";
   public static final String CONFIG_KEY_FIRSTRUN_DONE = "done";
   public static final String CONFIG_KEY_FIRSTRUN_DEFAULT = "notyet";

   public static final String CONFIG_KEY_KEEP_ALIVE = "keepalive";
   public static final String CONFIG_KEY_PERIODIC_MESSAGE_CHECK = "periodicmessagecheck";

   public static final String CONFIG_KEY_SERVER_BASE_URL = "server_base_url";
   public static final String CONFIG_KEY_SERVER_BASE_URL_DEFAULT = "http://fishnode1.de/p2";

   
   private SqlOpenHelper dbhelper;
   
   
   public ConfigHelper(SqlOpenHelper dbhelper) {
      this.dbhelper = dbhelper;
   }
   

   public ArrayList<String> getAllConfigValues() {
      SQLiteDatabase database = dbhelper.getReadableDatabase();
        
        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_CONFIG, 
              new String[] { SqlOpenHelper.CONFIG_COLUMN_KEY, SqlOpenHelper.CONFIG_COLUMN_VALUE }, 
              null, null, null, null, 
              SqlOpenHelper.CONFIG_COLUMN_KEY);
        ArrayList<String> entries = new ArrayList<String>();

        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
           do {
              entries.add(listCursor.getString(0) + " - " + listCursor.getString(1));
           } while (listCursor.moveToNext());      
        }
        listCursor.close();
        database.close();
        
        return entries;
   }
   
   private boolean hasKey(String key) {
      SQLiteDatabase database = dbhelper.getReadableDatabase();
        
        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_CONFIG, 
              new String[] { SqlOpenHelper.CONFIG_COLUMN_KEY, SqlOpenHelper.CONFIG_COLUMN_VALUE }, 
              null, null, null, null, 
              SqlOpenHelper.CONFIG_COLUMN_KEY);
        boolean hasKeyInDB = false;
        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
           do {
              if (key.equals(listCursor.getString(0))) {
                 hasKeyInDB = true;
              }
           } while (!hasKeyInDB && listCursor.moveToNext());      
        }
        listCursor.close();
        database.close();
        return hasKeyInDB;
   }
   
   
   private String getValueForKey(String key, String defaultValue) {
      SQLiteDatabase database = dbhelper.getReadableDatabase();
        
        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_CONFIG,
                new String[]{SqlOpenHelper.CONFIG_COLUMN_KEY, SqlOpenHelper.CONFIG_COLUMN_VALUE},
                null, null, null, null,
                SqlOpenHelper.CONFIG_COLUMN_KEY);
        String value = defaultValue;
        boolean hasKey = false;
        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
           do {
              //Log.d("sql", "configsearch, current key/val: " +  listCursor.getString(0) + "/" + listCursor.getString(1) );
              if (key.equals(listCursor.getString(0))) {
                 value = listCursor.getString(1);
                 hasKey = true;
              }
           } while (!hasKey && listCursor.moveToNext());      
        }
        listCursor.close();
        database.close();

        return value;
   }
   
   
   public int setConfig(String key, String value) {
      Log.d(TFApp.LOGKEY, String.format("setting key/val: %s/%s", key, value));
      
      ContentValues configValues = new ContentValues();
      
      boolean keyAlreadyInsideConfig = hasKey(key);
      if (keyAlreadyInsideConfig) {
         Log.d(TFApp.LOGKEY, "key already inside.");
      } else {
         Log.d(TFApp.LOGKEY, "key not yet inside.");
      }
      int affectedRows = 0;
      SQLiteDatabase db = dbhelper.getWritableDatabase();
      
      if (keyAlreadyInsideConfig) {
         configValues.put(SqlOpenHelper.CONFIG_COLUMN_VALUE, value);
         affectedRows = db.update(SqlOpenHelper.TABLE_NAME_CONFIG, configValues, SqlOpenHelper.CONFIG_COLUMN_KEY+"=?", new String[] {key});
         Log.d(TFApp.LOGKEY, String.format("updated %d rows", affectedRows));
      } else {
         configValues.put(SqlOpenHelper.CONFIG_COLUMN_KEY, key);
         configValues.put(SqlOpenHelper.CONFIG_COLUMN_VALUE, value);
         if (-1 != db.insert(SqlOpenHelper.TABLE_NAME_CONFIG, null, configValues)) {
            affectedRows = 1;
            Log.d(TFApp.LOGKEY, "added one row");
         }
      }
      
      db.close();
      
      return affectedRows;
   }
   
   
   public String getName() {
      return getValueForKey(CONFIG_KEY_NAME, CONFIG_KEY_NAME_DEFAULT);
   }
   
   
   public void setName(String newName) {
      setConfig(CONFIG_KEY_NAME, newName);
   }


   public String getBackground() {
      return getValueForKey(CONFIG_KEY_BACKGROUND, CONFIG_KEY_BACKGROUND_DEFAULT);
   }

   public void setBackgroundMobile() {
      setConfig(CONFIG_KEY_BACKGROUND, CONFIG_KEY_BACKGROUND_OPTION_MOBILE);
   }

   public void setBackgroundWifi() {
      setConfig(CONFIG_KEY_BACKGROUND, CONFIG_KEY_BACKGROUND_OPTION_WIFI);
   }

   public boolean isFirstRun() {
      return CONFIG_KEY_FIRSTRUN_DEFAULT.equals(getValueForKey(CONFIG_KEY_FIRSTRUN, CONFIG_KEY_FIRSTRUN_DEFAULT));
   }

   public void setFirstRunDone() {
      setConfig(CONFIG_KEY_FIRSTRUN, CONFIG_KEY_FIRSTRUN_DONE);
   }

   public String getKeepAlive() {
      return getValueForKey(CONFIG_KEY_KEEP_ALIVE, "");
   }

   public void setKeepAlive(String timeInfo) {
      setConfig(CONFIG_KEY_KEEP_ALIVE, timeInfo);
   }

   public String getPeriodicMessageCheck() {
      return getValueForKey(CONFIG_KEY_PERIODIC_MESSAGE_CHECK, "");
   }

   public void setPeriodicMessageCheck(String timeInfo) {
      setConfig(CONFIG_KEY_PERIODIC_MESSAGE_CHECK, timeInfo);
   }

   public String getServerBaseURL() {
      return getValueForKey(CONFIG_KEY_SERVER_BASE_URL, CONFIG_KEY_SERVER_BASE_URL_DEFAULT);
   }

   public void setServerBaseURL(String serverBaseURL) {
      setConfig(CONFIG_KEY_SERVER_BASE_URL, serverBaseURL);
   }
}
