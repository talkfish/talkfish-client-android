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
import android.os.StrictMode;



// http://stackoverflow.com/questions/19171596/sqliteopenhelper-multiples-tables-and-contentprovider

public class TFApp extends Application {
    public SqlOpenHelper databaseHelper;
    public Context applicationContext;
    public ConfigHelper configHelper;
    private DataAccessHelper dataAccessHelper;

    public static final String LOGKEY = "talkfish";
    public static final String WEBSITELOCATION = "http://www.talkfish.de";

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new SqlOpenHelper(this);
        applicationContext = getApplicationContext();
        configHelper = new ConfigHelper(databaseHelper);
        dataAccessHelper = new DataAccessHelper(databaseHelper);

       if (BuildConfig.DEBUG) {
          StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                  .detectDiskReads()
                  .detectDiskWrites()
                  .detectNetwork()   // or .detectAll() for all detectable problems
                  .penaltyLog()
                  .build());
          StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                  .detectLeakedSqlLiteObjects()
                  .detectLeakedClosableObjects()
                  .penaltyLog()
                  .penaltyDeath()
                  .build());
       }

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

   public void logException(Exception e) {
      StringBuilder exception = new StringBuilder();
      exception.append(String.format("WARNING: exception of type %s occured!\n", e.getClass().getCanonicalName()));
      exception.append(e.getMessage()+"\n");
      StringWriter sw = new StringWriter(); // http://stackoverflow.com/questions/1149703/stacktrace-to-string-in-java
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      exception.append(sw.toString()); // stack trace as a string
      addToApplicationLog(exception.toString(),10);
   }


   public void addToApplicationLog(String message) {
      addToApplicationLog(message,0);
   }

   public void addToApplicationLog(String message, int loglevel) {
      Log.d(LOGKEY, String.format("[level: %d] %s", loglevel, message));
      getDAH().appendLogMessage(message, loglevel);
   }


   public void setKeepAliveMarker(String tag) {
      DateFormat df = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
      java.util.Date d = new java.util.Date();
      String timeInfo = String.format("%s:%d",df.format(d), d.getTime());
      if (tag.equals(KeepAliveCheck.KEEPALIVECHECK_KEEPALIVE)) {
         configHelper.setKeepAlive(timeInfo);
      }
      if (tag.equals(PeriodicMessageCheck.PERIODICMESSAGECHECK_KEEPALIVE)) {
         configHelper.setPeriodicMessageCheck(timeInfo);
      }
   }


   public long getLagToLastKeepAliveMarkerInMillis(String tag) {
      String timeInfo = "";
      if (tag.equals(KeepAliveCheck.KEEPALIVECHECK_KEEPALIVE)) {
         timeInfo = configHelper.getKeepAlive();
      }
      if (tag.equals(PeriodicMessageCheck.PERIODICMESSAGECHECK_KEEPALIVE)) {
         timeInfo = configHelper.getPeriodicMessageCheck();
      }

      if (timeInfo == null || timeInfo.trim().equals("")) {
         Log.d(TFApp.LOGKEY, "getLagToLastKeepAliveMarkerInMillis: timeinfo does not exist");
         return -1;
      }
      String[] splitResult = timeInfo.split(":");
      if (splitResult.length == 2) {
         try {
            long last = Long.valueOf(splitResult[1]);
            java.util.Date d = new java.util.Date();
            long now = d.getTime();
            return now - last;
         } catch (NumberFormatException nfe) {
            Log.d(TFApp.LOGKEY, String.format("getLagToLastKeepAliveMarkerInMillis: number format exception, while converting %s", splitResult[1]));
            return -1;
         }
      } else {
         Log.d(TFApp.LOGKEY, String.format("getLagToLastKeepAliveMarkerInMillis: could not obtain millis when splitting <%s>", timeInfo));
         return -1;
      }
   }


   public void checkBackgroundService(Activity activity) {
      String currentmode = configHelper.getBackground();
      if (currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_WIFI) || currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_MOBILE)) {
         long lag_KeepAlive = getLagToLastKeepAliveMarkerInMillis(KeepAliveCheck.KEEPALIVECHECK_KEEPALIVE);

         if (-1 == lag_KeepAlive || lag_KeepAlive > 2*KeepAliveCheck.DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS * 1000 ) {
            addToApplicationLog(
                    String.format("*** App registered not working keepalivecheckalarm (last run before %d s), resetting it! ***",
                            lag_KeepAlive/1000), 5);
            KeepAliveCheck.setAlarm(activity);
         }
         long lag_PeriodicMessageCheck = getLagToLastKeepAliveMarkerInMillis(PeriodicMessageCheck.PERIODICMESSAGECHECK_KEEPALIVE);

         if (-1 == lag_PeriodicMessageCheck || lag_PeriodicMessageCheck > 3*PeriodicMessageCheck.DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS * 1000) {
            addToApplicationLog(
                    String.format(
                            "*** App registered not working messagecheckalarm (last run before %d s), resetting it! ***",
                            lag_PeriodicMessageCheck/1000), 5);
            PeriodicMessageCheck.setAlarm(activity);
         }
      }
   }

	

}
