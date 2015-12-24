package de.kochon.enrico.secrettalkmessenger.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;


public class KeepAliveCheck extends BroadcastReceiver {    
   public final static int DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS = 1200;
   public final static String KEEPALIVECHECK_KEEPALIVE = "ka_alive";

   @Override
   public void onReceive(Context context, Intent intent) {   
      SecretTalkMessengerApplication.addToApplicationLog("received keepalivecheckalarm");
      setAlarm(context);
      long lag = SecretTalkMessengerApplication.getLagToLastKeepAliveMarkerInMillis(PeriodicMessageCheck.PERIODICMESSAGECHECK_KEEPALIVE);

      if (lag > 3*PeriodicMessageCheck.DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS * 1000) {
         SecretTalkMessengerApplication.addToApplicationLog(
            String.format(
               "*** KeepAliveCheck registered not working messagecheckalarm (last run before %d ms), resetting it! ***",
               lag));
         PeriodicMessageCheck.setAlarm(context);
      }
   }
     
   public static void setAlarm(Context context) {
    	Intent i = new Intent(context, KeepAliveCheck.class);
      PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
      AlarmManager am= (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
      am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS, pi);

      SecretTalkMessengerApplication.setKeepAliveMarker(KEEPALIVECHECK_KEEPALIVE);
   }
     
   public static void cancelAlarm(Context context) {
    	Intent i = new Intent(context, KeepAliveCheck.class);
    	PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
   }
}
