package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.backend.NetworkIO;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.CountedImageMessage;
import de.kochon.enrico.secrettalkmessenger.model.SecretTalkChannelCache;
import de.kochon.enrico.secrettalkmessenger.model.EncryptedMessage;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

import de.kochon.enrico.secrettalkmessenger.backend.ChannelCacheRefreshable;
import de.kochon.enrico.secrettalkmessenger.backend.RefreshCacheForChannel;
import de.kochon.enrico.secrettalkmessenger.model.StructuredMessageBody;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.util.Log;
import android.app.ListFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;



public class ConversationListActivity extends Activity implements ChannelCacheRefreshable, Clickable {

    public final static int REQUEST_CODE_NEW_CONVERSATION = 1;
    public final static int REQUEST_CODE_EDIT_OR_DELETE_CONVERSATION = 2;
    public final static int REQUEST_CODE_SHOW_CONVERSATION = 3;

    private MyListFragment fragment;

    private HashMap<Channel, SecretTalkChannelCache> channelCacheMap;




    private ArrayAdapter<Conversation> aa;

    protected void initArrayAdapter() {
        Log.d(TFApp.LOGKEY, "ConversationListActivity: initArrayAdapter");
        Application app = this.getApplication();
        List<Conversation> conversations = ((TFApp) (app)).getDAH().loadAllConversations();
        Collections.sort(conversations);
        Collections.reverse(conversations);
        aa = new ArrayAdapter<Conversation>(this, R.layout.rowlayout_big, R.id.label, conversations);
        fragment.setListAdapter(aa);
    }

    protected void add(Conversation c) {
        aa.add(c);
    }

    protected Conversation getItem(int i) {
        return aa.getItem(i);
    }

    protected int getCount() {
        return aa.getCount();
    }

    protected void refreshSorting() {
        aa.sort(Conversation.conversationReverseComparator);
        aa.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.allchatsmenu, menu);

        return true;
    }


    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

       setContentView(R.layout.activity_chats);
       FragmentManager fm = getFragmentManager();
       fragment = (MyListFragment) fm.findFragmentById(R.id.listFragment);

       initArrayAdapter();


        ((TFApp)(this.getApplication())).checkBackgroundService(this);

    }


    public void click(int position) {
        Conversation c = aa.getItem(position);
        if (null != c) {
            c.setHasNewMessages(false);
            aa.notifyDataSetChanged();
            Intent intentOpenConversation = new Intent(this, ChatActivity.class);
            Bundle state = new Bundle();
            state.putLong(ChatActivity.SHOW_CONVERSATION_ID_KEY, c.getID()); // Todo: refactor
            intentOpenConversation.putExtras(state);
            startActivityForResult(intentOpenConversation, REQUEST_CODE_SHOW_CONVERSATION);
        }
    }


    private void createNewChat() {
        Intent intentCreateNewConversation = new Intent(ConversationListActivity.this, CreateNewConversationActivity.class);
        startActivityForResult(intentCreateNewConversation, REQUEST_CODE_NEW_CONVERSATION);
    }


    private void reload() {

        String networkState = NetworkIO.getNetworkStatus(ConversationListActivity.this);

        if (networkState.equals("wifi") || networkState.equals("mobile")) {
            Toaster.show(this, "Aktualisiere alle Unterhaltungen ...");
            channelCacheMap = new HashMap<Channel, SecretTalkChannelCache>();

            for (int index_for_conversations = 0; index_for_conversations < getCount(); index_for_conversations++) {
                Conversation c = getItem(index_for_conversations);
                Channel r = c.getChannelForReceiving();
                if (!channelCacheMap.containsKey(r)) {
                    Log.d(TFApp.LOGKEY, String.format("adding cache for channel %s", r.toString()));
                    channelCacheMap.put(r, new SecretTalkChannelCache(r.endpoint));
                }
            }
            for (Channel channel : channelCacheMap.keySet()) {
                Log.d(TFApp.LOGKEY, String.format("starting async task for loading from server for channel %s", channel.toString()));
                RefreshCacheForChannel task = new RefreshCacheForChannel(
                        ((TFApp) (ConversationListActivity.this.getApplication())),
                        channel.id,
                        ((TFApp) (ConversationListActivity.this.getApplication())).getDAH(),
                        ((TFApp) (ConversationListActivity.this.getApplication())).configHelper,
                        ConversationListActivity.this);
                task.execute(channel.endpoint);
            }
        } else {
            Toaster.show(ConversationListActivity.this, "Keine Datenverbindung vorhanden, keine Aktualisierung möglich.");
        }
    }

    private void goToSettings() {
        Intent intentSettings = new Intent(ConversationListActivity.this, SettingsActivity.class);
        startActivityForResult(intentSettings, 0);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_addchat):
                createNewChat();
                return true;

            case (R.id.action_reload):
                reload();
                return true;

            case (R.id.action_settings):
                goToSettings();
                return true;

            default: {
                break;
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_EDIT_OR_DELETE_CONVERSATION == requestCode) {
            initArrayAdapter();
        }
        if (REQUEST_CODE_SHOW_CONVERSATION == requestCode) {
            initArrayAdapter();
        }
        if (REQUEST_CODE_NEW_CONVERSATION == requestCode) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data.hasExtra(CreateNewConversationActivity.NEW_CONVERSATION_ID_KEY)) {
                        Long conversationID = data.getLongExtra(CreateNewConversationActivity.NEW_CONVERSATION_ID_KEY, -1);
                        Conversation c = ((TFApp) (this.getApplication())).getDAH().loadConversation(conversationID);
                        if (null != c) {
                            Toaster.show(this, "Neue Unterhaltung mit " + c.getNick() + " angelegt.");
                            add(c);
                        } else {
                            Toaster.show(this, String.format("Technischer Fehler. Unterhaltung mit ID %d kann nicht angezeigt werden.", conversationID));
                        }
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
                default:
            }
        }
    }


    public void indicateRefresh() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void stopRefreshIndication() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.INVISIBLE);

    }


    public void refreshConversationsFromCache(int idchannel) {
        boolean gotNewInChan = false;
        boolean gotNewInConv = false;
        for (int index_for_conversations = 0; index_for_conversations < getCount(); index_for_conversations++) {
            gotNewInConv = false;
            Conversation c = getItem(index_for_conversations);
            List<Messagekey> keys = c.getReceivingKeys();
            Channel r = c.getChannelForReceiving();
            StringBuffer messageBuilder = new StringBuffer();
            ByteArrayOutputStream imageBuilder = new ByteArrayOutputStream();

            if (r.id == idchannel && channelCacheMap.containsKey(r)) {
                SecretTalkChannelCache cache = channelCacheMap.get(r);
                if (!cache.isInitialized()) {
                    cache.initCache(((TFApp) (ConversationListActivity.this.getApplication())).getDAH().
                            loadCacheForChannel(r.id, SecretTalkChannelCache.CACHE_SIZE));
                }

                // TODO: the following code is more model or backend logic than ui -> refactor + REDUNDANT look at checkNewMessages.java
                for (int i = 0; i < SecretTalkChannelCache.CACHE_SIZE; i++) {
                    try {
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
                                        if (rawMessagePart.getTotal() == rawMessagePart.getCurrentPart() + 1) {
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
                                            ((TFApp) (ConversationListActivity.this.getApplication())).getDAH().addNewMessage(((TFApp) (ConversationListActivity.this.getApplication())), added);
                                            ((TFApp) (ConversationListActivity.this.getApplication())).getDAH().updateConversation(c);
                                            refreshSorting();
                                            fragment.getListView().invalidateViews();
                                            gotNewInChan = true;
                                            gotNewInConv = true;
                                        }
                                    } catch (IllegalArgumentException iae) {
                                       ((TFApp) (ConversationListActivity.this.getApplication())).logException(iae);
                                       ((TFApp) (ConversationListActivity.this.getApplication())).addToApplicationLog(String.format("probably old messageformat observered in communication with %s", c.getNick()));
                                    } finally {
                                        k.setIsUsed(true);
                                        ((TFApp) (ConversationListActivity.this.getApplication())).getDAH().updateKey(k);
                                    }
                                }
                            }
                        }
                    } catch (IOException ioe) {
                       ((TFApp) (ConversationListActivity.this.getApplication())).logException(ioe);
                    }
                }
                if (gotNewInConv) {
                    Toast.makeText(this, String.format(getString(R.string.messageNewMessagesInConv), c.getNick()), Toast.LENGTH_LONG).show();
                }
            }
        }
        if (!gotNewInChan) {
            Toast.makeText(this, String.format(getString(R.string.messageNoNewMessagesInChan), idchannel), Toast.LENGTH_LONG).show();
        }

    }



    public static class MyListFragment extends ListFragment  {

        private Clickable clickReceiver;


       // https://code.google.com/p/android/issues/detail?id=183358
       @TargetApi(23)
       @Override
       public void onAttach(Context context) {
          super.onAttach(context);
          onAttachToContext(context);
       }

       @SuppressWarnings("deprecation")
       @Override
       public void onAttach(Activity activity) {
          super.onAttach(activity);
          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
             onAttachToContext(activity);
          }
       }

       protected void onAttachToContext(Context context) {
          //do what you want / cast fragment listener
          try {
             clickReceiver = (Clickable) context;
          } catch (ClassCastException e) {
             throw new ClassCastException(context.toString()
                     + " must implement Clickable");
          }
       }

        public void onListItemClick(ListView listView, View view, int position, long id) {
            super.onListItemClick(listView, view, position, id);

           if (clickReceiver != null) {
              clickReceiver.click(position);
           }
           listView.invalidateViews();
        }
    }
}
