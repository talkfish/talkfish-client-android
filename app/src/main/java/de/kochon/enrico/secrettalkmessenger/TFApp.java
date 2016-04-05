package de.kochon.enrico.secrettalkmessenger;

import de.kochon.enrico.secrettalkmessenger.backend.DataAccessHelper;
import de.kochon.enrico.secrettalkmessenger.backend.ConfigHelper;
import de.kochon.enrico.secrettalkmessenger.backend.SqlOpenHelper;
import de.kochon.enrico.secrettalkmessenger.service.KeepAliveCheck;
import de.kochon.enrico.secrettalkmessenger.service.PeriodicMessageCheck;
import android.util.Log;
import android.app.Application;
import android.app.Activity;
import android.content.Context;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import android.os.Environment;



// http://stackoverflow.com/questions/19171596/sqliteopenhelper-multiples-tables-and-contentprovider

public class TFApp extends Application {
    public SqlOpenHelper databaseHelper;
    public Context applicationContext;
    public ConfigHelper configHelper;
    private DataAccessHelper dataAccessHelper;

    public static final String LOGKEY = "talkfish";
    public static final String DOWNLOADLOCATION = "http://www.talkfish.de/download/talkfish.apk";
    public static final String APPLICATIONLOGPATH = "talkfish/";


    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new SqlOpenHelper(this);
        applicationContext = getApplicationContext();
        configHelper = new ConfigHelper(databaseHelper);
        dataAccessHelper = new DataAccessHelper(databaseHelper);
    }

    @Override
    public void onTerminate() {
        databaseHelper.close();
        super.onTerminate();
    }

   // look at http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
   public synchronized DataAccessHelper getDAH() {
      return dataAccessHelper;
   }

   public static void logException(Exception e) {
      StringBuilder exception = new StringBuilder();
      exception.append(String.format("WARNING: exception of type %s occured!\n", e.getClass().getCanonicalName()));
      exception.append(e.getMessage()+"\n");
      StringWriter sw = new StringWriter(); // http://stackoverflow.com/questions/1149703/stacktrace-to-string-in-java
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      exception.append(sw.toString()); // stack trace as a string
      Log.d(LOGKEY, exception.toString());
      addToApplicationLog(exception.toString());
   }


   public static String ensureLogDir() {
      String dirname = String.format("%s/%s", Environment.getExternalStorageDirectory().getPath(), APPLICATIONLOGPATH);
      File directory = new File(dirname); 
      if (!directory.exists()) {
         try { directory.mkdir(); } 
         catch (SecurityException e) { Log.d(LOGKEY, e.toString()); }
      }
      return dirname;
   }


   public static void addToApplicationLog(String message) {
      String dirname = ensureLogDir();
      DateFormat filedateformat = new SimpleDateFormat("yyyy_MM_dd");
      String filename = String.format("%s%s.log", dirname, filedateformat.format(new java.util.Date()));
      File file = new File(filename); 
      if (!file.exists()) {
         try { file.createNewFile(); } 
         catch (IOException e) { Log.d(LOGKEY, e.toString()); }
      }
      try {
         BufferedWriter buffer = new BufferedWriter(new FileWriter(file, true)); 
         DateFormat df = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
         String lineToWrite = String.format("%s: %s",df.format(new java.util.Date()), message);
         buffer.append(lineToWrite);
         buffer.newLine();
         buffer.close();

			Log.d(TFApp.LOGKEY, "appending to AppLog: " + message);
      } catch (IOException e) { Log.d(LOGKEY, e.toString()); } 
   }


   public static void setKeepAliveMarker(String tag) {
      String dirname = ensureLogDir();
      String filename = String.format("%s%s.log", dirname, tag);
      File file = new File(filename); 
      if (!file.exists()) {
         try { file.createNewFile(); } 
         catch (IOException e) { Log.d(LOGKEY, e.toString()); }
      }
      try {
         BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false)); 
         DateFormat df = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
         java.util.Date d = new java.util.Date();
         String lineToWrite = String.format("%s:%d",df.format(d), d.getTime());
         buffer.append(lineToWrite);
         buffer.close();
      } catch (IOException e) { Log.d(LOGKEY, e.toString()); } 
   }


   public static long getLagToLastKeepAliveMarkerInMillis(String tag) {
      String dirname = ensureLogDir();
      String filename = String.format("%s%s.log", dirname, tag);
      File file = new File(filename); 
      if (!file.exists()) {
         Log.d(TFApp.LOGKEY, "getLagToLastKeepAliveMarkerInMillis: file does not exist");
         return -1;
      }
      try {
         BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file))); 
         String lastKeepAlive = buffer.readLine();
         buffer.close();
         buffer = null;
         String[] splitResult = lastKeepAlive.split(":");
         if (splitResult.length == 2) {
            try {
               long last = new Long(splitResult[1]);
               java.util.Date d = new java.util.Date();
               long now = d.getTime();
               return now - last;

            } catch (NumberFormatException nfe) {
               Log.d(TFApp.LOGKEY, String.format("getLagToLastKeepAliveMarkerInMillis: number format exception, while converting %s", splitResult[1]));
               return -1;
            }
         } else {
            Log.d(TFApp.LOGKEY, String.format("getLagToLastKeepAliveMarkerInMillis: could not obtain millis when splitting <%s>", lastKeepAlive));
            return -1;
         }
      } catch (IOException e) { 
         Log.d(LOGKEY, e.toString()); 
         return -1;
      } 
   }


   public static void checkBackgroundService(Activity activity) {
      String currentmode = ((TFApp)(activity.getApplication())).configHelper.getBackground();
      if (currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_WIFI) || currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_MOBILE)) {
         long lag_KeepAlive = TFApp.getLagToLastKeepAliveMarkerInMillis(KeepAliveCheck.KEEPALIVECHECK_KEEPALIVE);

         if (lag_KeepAlive > 2*KeepAliveCheck.DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS * 1000 ) {
            TFApp.addToApplicationLog(
                    String.format("*** ChatActivity registered not working keepalivecheckalarm (last run before %d ms), resetting it! ***",
                            lag_KeepAlive));
            KeepAliveCheck.setAlarm(activity);
         }
         long lag_PeriodicMessageCheck = TFApp.getLagToLastKeepAliveMarkerInMillis(PeriodicMessageCheck.PERIODICMESSAGECHECK_KEEPALIVE);

         if (lag_PeriodicMessageCheck  > 3*PeriodicMessageCheck.DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS * 1000) {
            TFApp.addToApplicationLog(
                    String.format(
                            "*** ChatActivity registered not working messagecheckalarm (last run before %d ms), resetting it! ***",
                            lag_PeriodicMessageCheck));
            PeriodicMessageCheck.setAlarm(activity);
         }
      }
   }

	

}
