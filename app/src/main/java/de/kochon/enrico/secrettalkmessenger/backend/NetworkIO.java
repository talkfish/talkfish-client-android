package de.kochon.enrico.secrettalkmessenger.backend;

import de.kochon.enrico.secrettalkmessenger.TFApp;

import android.util.Log;

import java.net.URL;
import java.net.URLConnection;
import android.net.ConnectivityManager;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

public class NetworkIO {

   public final static int MAX_TIME_OUT = 5000;


   // http://stackoverflow.com/questions/17148371/check-whether-mobile-is-using-wifi-or-data-3g
   public static String getNetworkStatus(Context contextobject) {
      final ConnectivityManager connMgr = (ConnectivityManager)
      contextobject.getSystemService(Context.CONNECTIVITY_SERVICE);
      final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
      if (wifi.isConnectedOrConnecting ()) {
         return "wifi";
      } else if (mobile.isConnectedOrConnecting ()) {
         return "mobile";
      } else {
         return "offline";
      }
   }


   public static String loadFileFromServer(String urlForFile) throws SocketTimeoutException, IOException, NumberFormatException {
			
			StringBuilder loadedServerFile = new StringBuilder();
	        
         Log.d(TFApp.LOGKEY, "loadFileFromServer");
         URL website = new URL(urlForFile);
         URLConnection connection = website.openConnection();
         connection.setConnectTimeout(MAX_TIME_OUT);
         connection.setReadTimeout(MAX_TIME_OUT);
         BufferedReader in = new BufferedReader(
                                   new InputStreamReader(
                                       connection.getInputStream()));
         String inputLine;
         while ((inputLine = in.readLine()) != null)  {
            loadedServerFile.append(inputLine);
         }
           
         in.close();
		        
	      return loadedServerFile.toString();
   }


   public static int getCurrentMessageOffsetOnServer(String urlForCurrentOffsetFile) throws SocketTimeoutException, IOException, NumberFormatException {
      int currentcount = -1;

      Log.d(TFApp.LOGKEY, "getCurrentMessageOffsetOnServer");
      String countFileContent = NetworkIO.loadFileFromServer(urlForCurrentOffsetFile);
      currentcount = Integer.parseInt(countFileContent.toString().trim(), 10);

      Log.d(TFApp.LOGKEY, String.format("messagefileoffset on server is %d", currentcount));
      return currentcount;
   }
}
