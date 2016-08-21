package de.kochon.enrico.secrettalkmessenger.service;

import de.kochon.enrico.secrettalkmessenger.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.os.Binder;
import android.os.IBinder;

import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.activities.ConversationListActivity;
import de.kochon.enrico.secrettalkmessenger.model.CountedImageMessage;
import de.kochon.enrico.secrettalkmessenger.model.SecretTalkChannelCache;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.EncryptedMessage;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

import de.kochon.enrico.secrettalkmessenger.backend.NetworkIO;
import de.kochon.enrico.secrettalkmessenger.backend.ConfigHelper;
import de.kochon.enrico.secrettalkmessenger.model.StructuredMessageBody;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Random;


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
        Intent goToSecretTalk = new Intent(this, ConversationListActivity.class);
        goToSecretTalk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending = PendingIntent.getActivity(this, (int) c.getID(), goToSecretTalk,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setContentIntent(pending)
                .setContentText(notificationmessage)
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
            if (((TFApp) (CheckNewMessages.this.getApplication())).getDAH().isDuringUpdate(1000 * MAX_LOCK_SECONDS))
                return false;
            iterationCount++;
            ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().setDuringUpdate(true);
        }
        if (false == lock) {
            ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().setDuringUpdate(false);
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
                       ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - service is being executed: successful got the lock, checking ..", myIter));
                        String networkState = NetworkIO.getNetworkStatus(CheckNewMessages.this);
                        String currentmode = ((TFApp) (CheckNewMessages.this.getApplication())).configHelper.getBackground();
                        if (networkState.equals("wifi") &&
                                (currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_WIFI) || currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_MOBILE))
                                || networkState.equals("mobile") && currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_MOBILE)) {
                            List<Conversation> conversations = ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().loadAllConversations();
                            HashMap<Channel, SecretTalkChannelCache> channelCacheMap = new HashMap<Channel, SecretTalkChannelCache>();

                            for (int index_for_conversations = 0; index_for_conversations < conversations.size(); index_for_conversations++) {
                                Conversation c = conversations.get(index_for_conversations);
                                Channel r = c.getChannelForReceiving();
                                if (!channelCacheMap.containsKey(r)) {
                                    channelCacheMap.put(r, new SecretTalkChannelCache(r.endpoint));
                                }
                            }

                            for (Channel channel : channelCacheMap.keySet()) {
                                int idchannel = channel.id;
                                String endpoint = channel.endpoint;

                                String baseurl = endpoint + "/get/";

                                int persistedOffset = ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().getCurrentOffsetForChannel(idchannel);
                                if (-1 == persistedOffset)
                                    persistedOffset = 0; // fix for scenario of fresh installed apps and server where m_000.txt does not yet exist
                                String currentTarget = baseurl + "current.txt?r="+NetworkIO.getRandomSuffixForAvoidingCachedRefreshs();
                                int mc = NetworkIO.getCurrentMessageOffsetOnServer(currentTarget);
                               ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - got current offset on server for channel with endpoint %s : %d", myIter, currentTarget, mc));

                                if ( ((TFApp) (CheckNewMessages.this.getApplication())).configHelper.isFirstRun()) {
                                   ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - first run, skipping old entries", myIter));
                                    ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().setCurrentOffsetForChannel(idchannel, mc);
                                    persistedOffset = mc;
                                    ((TFApp) (CheckNewMessages.this.getApplication())).configHelper.setFirstRunDone();
                                }

                                if (mc != -1 && persistedOffset != mc) {
                                    int messagelimit = mc; // default in case persistedOffset < mc
                                    if (persistedOffset > mc) { // server wrap occured
                                        messagelimit = mc + SecretTalkChannelCache.CACHE_SIZE;
                                    }
                                   ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - loading %d files from server", myIter, (messagelimit - persistedOffset)));
                                    for (int i = persistedOffset + 1; i <= messagelimit; i++) {
                                        int i_mod_cache_size = i % SecretTalkChannelCache.CACHE_SIZE;
                                        String targetfile = String.format("%sm_%07d.txt", baseurl, i_mod_cache_size);
                                       ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - trying to download %s", myIter, targetfile));
                                        String currentMessage = NetworkIO.loadFileFromServer(targetfile);
                                       ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - successfully downloaded %s", myIter, targetfile));

                                       ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - adding file to cache", myIter));
                                        ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().setCacheForCacheMetaIDAndKey(idchannel, i_mod_cache_size, currentMessage);
                                    }
                                   ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - updating current offset for channel %d to %d", myIter, idchannel, mc));
                                    ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().setCurrentOffsetForChannel(idchannel, mc);
                                   ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - finished loading server for channel with endpoint %s", myIter, endpoint));

                                   ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - processing conversations and cache for channel with endpoint %s", myIter, endpoint));
                                    for (int index_for_conversations = 0; index_for_conversations < conversations.size(); index_for_conversations++) {
                                        Conversation c = conversations.get(index_for_conversations);
                                        List<Messagekey> keys = c.getReceivingKeys();
                                        Channel r = c.getChannelForReceiving();
                                        StringBuffer messageBuilder = new StringBuffer();
                                        ByteArrayOutputStream imageBuilder = new ByteArrayOutputStream();

                                        if (r.id == idchannel && channelCacheMap.containsKey(r)) {
                                            SecretTalkChannelCache cache = channelCacheMap.get(r);
                                            if (!cache.isInitialized()) {
                                                cache.initCache(((TFApp) (CheckNewMessages.this.getApplication())).getDAH().loadCacheForChannel(r.id, SecretTalkChannelCache.CACHE_SIZE));
                                            }

                                            for (int i = 0; i < SecretTalkChannelCache.CACHE_SIZE; i++) {
                                                String currentMessage = cache.getValue(i);
                                                if (EncryptedMessage.isEncryptedMessage(currentMessage)) {
                                                    EncryptedMessage e = new EncryptedMessage(currentMessage);
                                                    Messagekey k = e.findMatchingKey(keys);
                                                    if (k != null) {
                                                        if (!k.getIsUsed()) {
                                                            try {
                                                                StructuredMessageBody rawMessagePart = e.decrypt(k);
                                                                if (rawMessagePart.isImage()) {
                                                                    imageBuilder.write(rawMessagePart.getRawPayload());
                                                                } else {
                                                                    messageBuilder.append(rawMessagePart.getPayload());
                                                                }

                                                                if (rawMessagePart.getTotal() == rawMessagePart.getCurrentPart()+1) {
                                                                    CountedMessage currentMessageToRetrieve = null;
                                                                    if (rawMessagePart.isImage()) {
                                                                        currentMessageToRetrieve = new CountedImageMessage(k.getIsForReceiving(), CountedMessage.NOTADDEDNUMBER, 1, imageBuilder.toByteArray(), new Date());
                                                                    } else {
                                                                        currentMessageToRetrieve = new CountedMessage(k.getIsForReceiving(), CountedMessage.NOTADDEDNUMBER, 1, messageBuilder.toString(), new Date());
                                                                    }
                                                                    imageBuilder = new ByteArrayOutputStream();
                                                                    messageBuilder = new StringBuffer();
                                                                    CountedMessage added = c.addReceivedMessage(currentMessageToRetrieve);
                                                                    c.setHasNewMessages(true);
                                                                    ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().addNewMessage(((TFApp) (CheckNewMessages.this.getApplication())), added);
                                                                    ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().updateConversation(c);

                                                                    Notification n = getNotification(c);
                                                                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                                    notificationManager.notify(NOTIFICATION_ID + ((int) c.getID()), n);

                                                                }
                                                            } catch (IllegalArgumentException iae) {
                                                               ((TFApp) (CheckNewMessages.this.getApplication())).logException(iae);
                                                               ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("probably old messageformat observered in communication with %s", c.getNick()));
                                                            } finally {
                                                                k.setIsUsed(true);
                                                                ((TFApp) (CheckNewMessages.this.getApplication())).getDAH().updateKey(k);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                   ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - finished processing conversations and cache", myIter));

                                } else {
                                   ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - nothing to download", myIter));
                                }

                            }
                        } else {
                           ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog(String.format("%d - networkstate: %s and backgroundconfig: %s -> skipping ..", myIter, networkState, currentmode));
                        }

                    } catch (Exception e) {
                       ((TFApp) (CheckNewMessages.this.getApplication())).logException(e);
                    } finally {
                        setLock(false);
                    }
                } else {
                   ((TFApp) (CheckNewMessages.this.getApplication())).addToApplicationLog("*** service is being executed: lock was set, skipping ..");
                }
                stopSelf();

            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
           ((TFApp) (CheckNewMessages.this.getApplication())).logException(e);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startID) {
        Log.d(TFApp.LOGKEY, "on start");
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
