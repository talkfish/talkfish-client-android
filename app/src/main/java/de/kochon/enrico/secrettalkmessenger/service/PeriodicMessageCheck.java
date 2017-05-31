package de.kochon.enrico.secrettalkmessenger.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.kochon.enrico.secrettalkmessenger.TFApp;


public class PeriodicMessageCheck extends BroadcastReceiver {    
   public final static int DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS = 19;
   public final static String PERIODICMESSAGECHECK_KEEPALIVE = "pm_alive";


   @Override
   public void onReceive(Context context, Intent intent) {
      ((TFApp) context.getApplicationContext()).addToApplicationLog("received messagecheckalarm");
      setAlarm(context);

      long lag = ((TFApp) context.getApplicationContext()).getLagToLastKeepAliveMarkerInMillis(KeepAliveCheck.KEEPALIVECHECK_KEEPALIVE);

      if (lag > 2*KeepAliveCheck.DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS * 1000 ) {
         ((TFApp) context.getApplicationContext()).addToApplicationLog(
                 String.format("*** PeriodicMessageCheck registered not working keepalivecheckalarm (last run before %d s), resetting it! ***",
                         lag / 1000));
         KeepAliveCheck.setAlarm(context);
      }

      Intent i = new Intent(context, CheckNewMessages.class);
      context.startService(i);
   }
     
   public static void setAlarm(Context context) {
    	Intent i = new Intent(context, PeriodicMessageCheck.class);
      PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
      AlarmManager am= (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
      am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS, pi);

      ((TFApp) context.getApplicationContext()).setKeepAliveMarker(PERIODICMESSAGECHECK_KEEPALIVE);
   }
     
   public static void cancelAlarm(Context context) {
    	Intent i = new Intent(context, PeriodicMessageCheck.class);
    	PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
   }
}
