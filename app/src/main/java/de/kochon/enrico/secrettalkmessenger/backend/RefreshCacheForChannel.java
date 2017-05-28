package de.kochon.enrico.secrettalkmessenger.backend;

import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.SecretTalkChannelCache;

import android.os.AsyncTask;
import android.util.Log;


public class RefreshCacheForChannel extends AsyncTask<String, Void, String> {


   private long idchannel;
   private DataAccessHelper dataAccessHelper;
   private ConfigHelper configHelper;
   private ChannelCacheRefreshable refreshable;
   private TFApp app;

   public RefreshCacheForChannel(TFApp app, long idchannel, DataAccessHelper dataAccessHelper, ConfigHelper configHelper, ChannelCacheRefreshable refreshable) {
      super();
      this.app = app;
      this.idchannel = idchannel;
      this.dataAccessHelper = dataAccessHelper;
      this.configHelper = configHelper;
      this.refreshable = refreshable;

   }


   @Override
   protected String doInBackground(String... urls) {
      String message = "";
      try {
         if ( (urls != null) && (urls.length >= 1) ) {
            String baseurl = urls[0] + "/get/";

            int persistedOffset = dataAccessHelper.getCurrentOffsetForChannel(idchannel);
            if (-1 == persistedOffset) persistedOffset = 0; // fix for scenario of fresh installed apps and server where m_000.txt does not yet exist
            String currentTarget = baseurl+"current.txt?r="+NetworkIO.getRandomSuffixForAvoidingCachedRefreshs();
            int mc = NetworkIO.getCurrentMessageOffsetOnServer(currentTarget);
            app.addToApplicationLog(String.format("by useraction, got current offset on server for channel with endpoint %s : %d", currentTarget, mc));

            if ( configHelper.isFirstRun()) {
               app.addToApplicationLog(String.format("first run, skipping old entries"));
               dataAccessHelper.setCurrentOffsetForChannel(idchannel, mc);
               persistedOffset = mc;
               configHelper.setFirstRunDone();
            }

            if (mc != -1 && persistedOffset != mc) {
               int messagelimit = mc; // default in case persistedOffset < mc
               if (persistedOffset > mc) { // server wrap occured
                  messagelimit = mc+SecretTalkChannelCache.CACHE_SIZE;
               }
               for (int i=persistedOffset+1; i<=messagelimit; i++) {
                  int i_representant = i%SecretTalkChannelCache.CACHE_SIZE;
                  String targetfile = String.format("%sm_%07d.txt", baseurl, i_representant);
                  Log.d(TFApp.LOGKEY, String.format("trying to download %s", targetfile));
                  String currentMessage = NetworkIO.loadFileFromServer(targetfile);

                  dataAccessHelper.setCacheForCacheMetaIDAndKey(idchannel, i_representant, currentMessage);
               }
               dataAccessHelper.setCurrentOffsetForChannel(idchannel, mc);
            }
            message = String.format("persisted: %d - on server: %d", persistedOffset, mc);
         }
         
      } catch (Exception ex) {
         app.logException(ex);
         message = ex.toString();
      }
      return message;
   }


   @Override
   protected void onPreExecute() {
      refreshable.indicateRefresh();
   }


   @Override
   protected void onPostExecute(String result) {
      refreshable.refreshConversationsFromCache(idchannel);
      refreshable.stopRefreshIndication();
   }

}
