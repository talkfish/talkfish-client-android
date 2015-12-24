package de.kochon.enrico.secrettalkmessenger.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;


public class PeriodicMessageCheck extends BroadcastReceiver {    
   public final static int DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS = 137;
   public final static String PERIODICMESSAGECHECK_KEEPALIVE = "pm_alive";


   @Override
   public void onReceive(Context context, Intent intent) {   
      SecretTalkMessengerApplication.addToApplicationLog("received messagecheckalarm");
      setAlarm(context);

      long lag = SecretTalkMessengerApplication.getLagToLastKeepAliveMarkerInMillis(KeepAliveCheck.KEEPALIVECHECK_KEEPALIVE);

      if (lag > 2*KeepAliveCheck.DEFAULT_RECURRENCE_INTERVAL_IN_SECONDS * 1000 ) {
         SecretTalkMessengerApplication.addToApplicationLog(
            String.format("*** PeriodicMessageCheck registered not working keepalivecheckalarm (last run before %d ms), resetting it! ***",
               lag));
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

      SecretTalkMessengerApplication.setKeepAliveMarker(PERIODICMESSAGECHECK_KEEPALIVE);
   }
     
   public static void cancelAlarm(Context context) {
    	Intent i = new Intent(context, PeriodicMessageCheck.class);
    	PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
   }
}
