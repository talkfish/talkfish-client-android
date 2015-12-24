package de.kochon.enrico.secrettalkmessenger.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStartMessageChecker extends BroadcastReceiver
{   
   @Override
   public void onReceive(Context context, Intent intent)
   {   
      if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
      {
         PeriodicMessageCheck.setAlarm(context);
         KeepAliveCheck.setAlarm(context);
      }
   }
}
