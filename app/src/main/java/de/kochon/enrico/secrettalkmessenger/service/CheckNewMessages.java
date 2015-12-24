package de.kochon.enrico.secrettalkmessenger.service;

import de.kochon.enrico.secrettalkmessenger.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.os.Binder;
import android.os.IBinder;
import android.os.Bundle;

import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;
import de.kochon.enrico.secrettalkmessenger.activities.ChatActivity;
import de.kochon.enrico.secrettalkmessenger.model.SecretTalkChannelCache;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.SecretTalkChannelCache;
import de.kochon.enrico.secrettalkmessenger.model.EncryptedMessage;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

import de.kochon.enrico.secrettalkmessenger.backend.DataAccessHelper;
import de.kochon.enrico.secrettalkmessenger.backend.NetworkIO;
import de.kochon.enrico.secrettalkmessenger.backend.ConfigHelper;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class CheckNewMessages extends Service {
	
	public boolean stopThreads = false;
	public static final int NOTIFICATION_ID = 23;

   public static final int MAX_LOCK_SECONDS = 1200;
	
	public Long currentMessageCount = 0L;
	public Long newMessageCount = 0L;
	
	private Thread t;


	// DOCS at http://developer.android.com/reference/android/app/Service.html
   /**
   * Class for clients to access.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with
   * IPC.
   */
   public class LocalBinder extends Binder {
      public CheckNewMessages getService() {
            return CheckNewMessages.this;
        }
   }
    

   // This is the object that receives interactions from clients.  See
   // RemoteService for a more complete example.
   private final IBinder mBinder = new LocalBinder();
   
   @Override
   public IBinder onBind(Intent intent) {
       return mBinder;
   }
	
	
	private Notification getNotification(Conversation c) {
       String notificationmessage = String.format("Neue Nachricht von %s", c.getNick());
       Intent goToSecretTalk = new Intent(this, ChatActivity.class);
       goToSecretTalk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP);
       Bundle state = new Bundle();
       state.putLong(ChatActivity.SHOW_CONVERSATION_ID_KEY, c.getID()); // Todo: refactor
       goToSecretTalk.putExtras(state);
       PendingIntent pending = PendingIntent.getActivity(this, (int) c.getID(), goToSecretTalk,
                PendingIntent.FLAG_UPDATE_CURRENT );
		Notification notification = new NotificationCompat.Builder(this)
	   .setContentIntent(pending)
		.setContentText(notificationmessage )
		.setTicker(notificationmessage)
	   .setSmallIcon(R.drawable.internet_group_chat)
	   .setWhen(System.currentTimeMillis())
	   .setDefaults(Notification.DEFAULT_ALL)
	   .build();
      notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
      notification.ledARGB = android.graphics.Color.BLUE;
      notification.ledOnMS = 1000;
      notification.ledOffMS = 300;

		return notification;
	}
	
   private static long iterationCount = 0;

   private synchronized boolean setLock(boolean lock) {
      if (true == lock) {
         if (((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().isDuringUpdate(1000 * MAX_LOCK_SECONDS)) return false;
         iterationCount++;
         ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().setDuringUpdate(true);
      }
      if (false == lock) {
         ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().setDuringUpdate(false);
      }
      return true;
   }
	
	private void startMeOnce() {		
		t = new Thread(new Runnable() {
			@Override
			public void run() {
            if (setLock(true)) {
               try {
                  long myIter = iterationCount;
                  SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - service is being executed: successful got the lock, checking ..", myIter));
                  String networkState = NetworkIO.getNetworkStatus(CheckNewMessages.this);
                  String currentmode = ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).configHelper.getBackground();
                  if (  networkState.equals("wifi") && 
                        (currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_WIFI) || currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_MOBILE)) 
                      || networkState.equals("mobile") && currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_MOBILE)) 
                  {

                     List<Conversation> conversations = ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().loadAllConversations();
                     HashMap<Channel,SecretTalkChannelCache> channelCacheMap = new HashMap<Channel, SecretTalkChannelCache>();

                     for (int index_for_conversations=0; index_for_conversations<conversations.size(); index_for_conversations++) {
                        Conversation c = conversations.get(index_for_conversations);
                        Channel r = c.getChannelForReceiving();
                        if (!channelCacheMap.containsKey(r)) {
                           channelCacheMap.put(r, new SecretTalkChannelCache(r.endpoint));
                        }
                     }
                     int messageCount = 0;
                     for (Channel channel: channelCacheMap.keySet()) {
                        int idchannel = channel.id;
                        String endpoint = channel.endpoint;

                        String baseurl = endpoint + "/get/";

                        int persistedOffset = ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().getCurrentOffsetForChannel(idchannel);
                        if (-1 == persistedOffset) persistedOffset = 0; // fix for scenario of fresh installed apps and server where m_000.txt does not yet exist
                        SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - getting current offset on server for channel with endpoint %s", myIter, endpoint));
                        int mc = NetworkIO.getCurrentMessageOffsetOnServer(baseurl+"current.txt");
                        SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - got current offset on server for channel with endpoint %s: %d", myIter, endpoint, mc));

                        if (mc != -1 && persistedOffset != mc) { 
                           int messagelimit = mc; // default in case persistedOffset < mc
                           if (persistedOffset > mc) { // server wrap occured
                              messagelimit = mc+SecretTalkChannelCache.CACHE_SIZE;
                           }
                           SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - loading %d files from server", myIter, (messagelimit - persistedOffset)));
                           for (int i=persistedOffset+1; i<=messagelimit; i++) {
                              int i_representant = i%SecretTalkChannelCache.CACHE_SIZE;
                              String targetfile = String.format("%sm_%03d.txt", baseurl, i_representant);
                              SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - trying to download %s", myIter, targetfile));
                              String currentMessage = NetworkIO.loadFileFromServer(targetfile);
                              SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - successfully downloaded %s", myIter, targetfile));

                              SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - adding file to cache", myIter));
                              ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().setCacheForCacheMetaIDAndKey(idchannel, i_representant, currentMessage);
                           }
                           SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - updating current offset for channel %d to %d", myIter, idchannel, mc));
                           ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().setCurrentOffsetForChannel(idchannel, mc);
                           SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - finished loading server for channel with endpoint %s", myIter, endpoint));

                           SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - processing conversations and cache for channel with endpoint %s", myIter, endpoint));
                           for (int index_for_conversations=0; index_for_conversations<conversations.size(); index_for_conversations++) {
                              Conversation c = conversations.get(index_for_conversations);
                              List<Messagekey> keys = c.getReceivingKeys();
                              Channel r = c.getChannelForReceiving();

                              if (r.id == idchannel && channelCacheMap.containsKey(r)) {
                                 SecretTalkChannelCache cache = channelCacheMap.get(r);
                                 if (!cache.isInitialized()) {
                                    cache.initCache(((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().loadCacheForChannel(r.id, SecretTalkChannelCache.CACHE_SIZE));
                                 }

                                 for(int i=0;i<SecretTalkChannelCache.CACHE_SIZE;i++) {
                                    String currentMessage = cache.getValue(i);
                                    if (EncryptedMessage.isEncryptedMessage(currentMessage)) {
                                       EncryptedMessage e = new EncryptedMessage(currentMessage);
                                       Messagekey k = e.findMatchingKey(keys);
                                       if (k!=null) { 
                                          if (!k.getIsUsed()) {
                                             CountedMessage decrypted = e.decrypt(k);
                                             k.setIsUsed(true); 
                                             ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().updateKey(k);
                                             CountedMessage added = c.addReceivedMessage(decrypted);
                                             c.setHasNewMessages(true);
                                             ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().addNewMessage(added);
                                             ((SecretTalkMessengerApplication)(CheckNewMessages.this.getApplication())).getDataAccessHelper().updateConversation(c);

                                             Notification n = getNotification(c);
                                             NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                             notificationManager.notify(NOTIFICATION_ID+((int)c.getID()), n);
                                          }
                                       }
                                    }
                                 }
                              }
                           } 
                           SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - finished processing conversations and cache", myIter));

                        } else {
                           SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - nothing to download", myIter));
                        }

                     }
                  } else {
                     SecretTalkMessengerApplication.addToApplicationLog(String.format("%d - networkstate: %s and backgroundconfig: %s -> skipping ..", myIter, networkState, currentmode));
                  }

               } catch (Exception e) { 
                  SecretTalkMessengerApplication.logException(e); 
               } finally {
                  setLock(false);
               }
            } else {
			      SecretTalkMessengerApplication.addToApplicationLog("*** service is being executed: lock was set, skipping ..");
            }
            stopSelf();

			}});
		t.start();
      try {
         t.join();
      } catch (InterruptedException e) {
         SecretTalkMessengerApplication.logException(e); 
      }
	}
	
	@Override
	public int onStartCommand(final Intent intent, int flags, final int startID) {
		Log.d(SecretTalkMessengerApplication.LOGKEY, "on start");
		startMeOnce();
		return START_NOT_STICKY;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopThreads = true;
	}
}
