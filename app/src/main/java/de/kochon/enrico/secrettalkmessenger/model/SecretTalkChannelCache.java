package de.kochon.enrico.secrettalkmessenger.model;

import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;

import android.util.Log;

public class SecretTalkChannelCache {


   private String endpoint;

   private boolean initialized;

   public final static int CACHE_SIZE = 1930000;

   // TODO Rundenzähler 
   // ACHTUNG: ohne Abbildung der aktuellen Runde,
   //          ist nicht feststellbar, ob Nachrichten
   //          übersprungen wurden, falls
   //          die Anzahl neuer Nachrichten
   //          größer als CACHE_SIZE ist
   //
   // Prio B, da bei hinreichend großer Cachesize nicht nötig
   // und im Falle der Notwendigkeit nicht ausreichend zur
   // Wiederherstellung von Nachrichten
   // einziger reeller Nutzen ist dann Indikatorfunktion

   private String[] cachedValues = new String[CACHE_SIZE];


   public SecretTalkChannelCache(String endpoint) {
      this.endpoint = endpoint;
      this.initialized = false;
   }


   public void initCache(String[] initValues) {
      if (initValues.length != cachedValues.length) {
         String errormessage = String.format("ERROR in initCachedValues: size does not fit - length of given array is %d, but should be %d!",
                                             initValues.length, cachedValues.length);
         Log.d(SecretTalkMessengerApplication.LOGKEY, errormessage);
         throw new IllegalArgumentException(errormessage);
      }

      for (int i=0; i<cachedValues.length; i++) {
         cachedValues[i] = initValues[i];
      }

      this.initialized = true;
   }


   public boolean isInitialized() {
      return this.initialized;
   }

   
   public String getValue(int index) {
      if (index < 0 || index >= CACHE_SIZE) throw new IllegalArgumentException("ERROR: post fence error, index outside cache borders.");
      return cachedValues[index];
   }
}
