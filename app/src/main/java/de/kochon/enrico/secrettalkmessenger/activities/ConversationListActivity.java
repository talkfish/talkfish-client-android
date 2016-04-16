package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.SecretTalkChannelCache;
import de.kochon.enrico.secrettalkmessenger.model.EncryptedMessage;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

import de.kochon.enrico.secrettalkmessenger.backend.ChannelCacheRefreshable;
import de.kochon.enrico.secrettalkmessenger.backend.RefreshCacheForChannel;
import de.kochon.enrico.secrettalkmessenger.model.StructuredMessageBody;

import android.app.ListActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ConversationListActivity extends ListActivity implements ChannelCacheRefreshable {

   private ArrayAdapter<Conversation> aa;

   private HashMap<Channel,SecretTalkChannelCache> channelCacheMap;

   

   public final static int REQUEST_CODE_NEW_CONVERSATION = 1;
   public final static int REQUEST_CODE_EDIT_OR_DELETE_CONVERSATION = 2;
   public final static int REQUEST_CODE_SHOW_CONVERSATION = 3;


   protected void initArrayAdapter() {
       Log.d(TFApp.LOGKEY, "ConversationListActivity: initArrayAdapter");
       List<Conversation> conversations = ((TFApp)(this.getApplication())).getDAH().loadAllConversations();
      Collections.sort(conversations);
      Collections.reverse(conversations);
      aa = new ArrayAdapter<Conversation>(this, R.layout.rowlayout_big, R.id.label, conversations);
      setListAdapter(aa);
   }


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_chats);

      initArrayAdapter();

      Button btnCreate = (Button) findViewById(R.id.buttonChatsAdd);
      if (btnCreate != null) {
         btnCreate.setOnClickListener(new OnClickListener() { 
               public void onClick(View v) { 
                  Intent intentCreateNewConversation = new Intent(ConversationListActivity.this, CreateNewConversationActivity.class);
                  startActivityForResult(intentCreateNewConversation, REQUEST_CODE_NEW_CONVERSATION);
               } 
         });
      }

      Button btnRefresh = (Button) findViewById(R.id.buttonChatsRefresh);
      if (btnRefresh != null) {
         btnRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

               Toast.makeText(ConversationListActivity.this, "Aktualisiere alle Unterhaltungen ...", Toast.LENGTH_SHORT).show();
               channelCacheMap = new HashMap<Channel, SecretTalkChannelCache>();

               for (int index_for_conversations = 0; index_for_conversations < aa.getCount(); index_for_conversations++) {
                  Conversation c = aa.getItem(index_for_conversations);
                  Channel r = c.getChannelForReceiving();
                  if (!channelCacheMap.containsKey(r)) {
                     Log.d(TFApp.LOGKEY, String.format("adding cache for channel %s", r.toString()));
                     channelCacheMap.put(r, new SecretTalkChannelCache(r.endpoint));
                  }
               }
               for (Channel channel : channelCacheMap.keySet()) {
                  Log.d(TFApp.LOGKEY, String.format("starting async task for loading from server for channel %s", channel.toString()));
                  RefreshCacheForChannel task = new RefreshCacheForChannel(channel.id,
                          ((TFApp) (ConversationListActivity.this.getApplication())).getDAH(),
                          ConversationListActivity.this);
                  task.execute(channel.endpoint);
               }
            }
         });
      }
   }


   public void refreshConversationsFromCache(int idchannel) {
      boolean gotNewInChan = false;
      boolean gotNewInConv = false;
      for (int index_for_conversations=0; index_for_conversations<aa.getCount(); index_for_conversations++) {
         gotNewInConv = false;
         Conversation c = aa.getItem(index_for_conversations);
         List<Messagekey> keys = c.getReceivingKeys();
         Channel r = c.getChannelForReceiving();
         StringBuffer messageBuilder = new StringBuffer();

         if (r.id == idchannel && channelCacheMap.containsKey(r)) {
            SecretTalkChannelCache cache = channelCacheMap.get(r);
            if (!cache.isInitialized()) {
               cache.initCache(((TFApp)(ConversationListActivity.this.getApplication())).getDAH().
                                                         loadCacheForChannel(r.id, SecretTalkChannelCache.CACHE_SIZE));
            }

             // TODO: the following code is more model or backend logic than ui -> refactor + REDUNDANT look at checkNewMessages.java
            for(int i=0;i<SecretTalkChannelCache.CACHE_SIZE;i++) {
               String currentMessage = cache.getValue(i);
               if (EncryptedMessage.isEncryptedMessage(currentMessage)) {
                  EncryptedMessage e = new EncryptedMessage(currentMessage);
                  Messagekey k = e.findMatchingKey(keys);
                  if (k!=null) { 
                     if (!k.getIsUsed()) {
                        try {
                           StructuredMessageBody rawMessagePart = e.decrypt(k);
                           messageBuilder.append(rawMessagePart.getPayload());
                           if (rawMessagePart.getTotal() == rawMessagePart.getCurrentPart()+1) {
                              CountedMessage currentMessageToRetrieve = new CountedMessage(k.getIsForReceiving(), CountedMessage.NOTADDEDNUMBER, 1, messageBuilder.toString(), new Date());
                              messageBuilder = new StringBuffer();
                              CountedMessage added = c.addReceivedMessage(currentMessageToRetrieve);
                              c.setHasNewMessages(true);
                              ((TFApp)(ConversationListActivity.this.getApplication())).getDAH().addNewMessage(added);
                              ((TFApp)(ConversationListActivity.this.getApplication())).getDAH().updateConversation(c);
                              aa.sort(Conversation.conversationReverseComparator);
                              aa.notifyDataSetChanged();
                              ConversationListActivity.this.getListView().invalidateViews();
                              gotNewInChan = true;
                              gotNewInConv = true;
                           }
                        } catch (IllegalArgumentException iae) {
                           TFApp.logException(iae);
                           TFApp.addToApplicationLog(String.format("probably old messageformat observered in communication with %s", c.getNick()));
                        } finally {
                           k.setIsUsed(true);
                           ((TFApp)(ConversationListActivity.this.getApplication())).getDAH().updateKey(k);
                        }
                     }
                  }
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


   public void indicateRefresh() {
      this.setProgressBarIndeterminateVisibility(true);
   }
   
   public void stopRefreshIndication() {
      this.setProgressBarIndeterminateVisibility(false);
   }


   // taken from http://androidforbeginners.blogspot.de/2010/03/clicking-buttons-in-listview-row.html
   public void clickAddKeys(View v) {
      LinearLayout vwParentRow = (LinearLayout)v.getParent();
      int position = getListView().getPositionForView(vwParentRow);
      Conversation c = ConversationListActivity.this.aa.getItem(position);
      if (null != c) {
         Intent intentShowConversationProperties = new Intent(ConversationListActivity.this, AddKeysActivity.class);
         Bundle state = new Bundle();
         state.putLong(AddKeysActivity.SHOW_CONVERSATION_ID_KEY, c.getID());
         intentShowConversationProperties.putExtras(state); 
         startActivityForResult(intentShowConversationProperties, REQUEST_CODE_EDIT_OR_DELETE_CONVERSATION);
      }
   }


   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (REQUEST_CODE_EDIT_OR_DELETE_CONVERSATION == requestCode) {
         initArrayAdapter();
      }
      if ( REQUEST_CODE_SHOW_CONVERSATION == requestCode) {
         initArrayAdapter();
      }
      if (REQUEST_CODE_NEW_CONVERSATION == requestCode) {
         switch (resultCode) {
            case Activity.RESULT_OK:
               if (data.hasExtra(CreateNewConversationActivity.NEW_CONVERSATION_ID_KEY)) {
                  Long conversationID = data.getLongExtra(CreateNewConversationActivity.NEW_CONVERSATION_ID_KEY, -1);
                  Conversation c = ((TFApp)(this.getApplication())).getDAH().loadConversation(conversationID);
                  if (null != c) {
                     Toast.makeText(this, "Neue Unterhaltung mit " + c.getNick() + " angelegt.", Toast.LENGTH_SHORT).show();
                     aa.add(c);
                  }
                  else {
                     Toast.makeText(this, String.format("Technischer Fehler. Unterhaltung mit ID %d kann nicht angezeigt werden.", conversationID),
                     Toast.LENGTH_SHORT).show();
                  }
               }
               break;
            case Activity.RESULT_CANCELED:
               break;
            default:
         }
      }
   }
   
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l,v,position,id);
      Conversation c = aa.getItem(position);
      if (null!=c) {
         c.setHasNewMessages(false);
         aa.notifyDataSetChanged();
         ConversationListActivity.this.getListView().invalidateViews();
         Intent intentOpenConversation = new Intent(ConversationListActivity.this, ChatActivity.class);
         Bundle state = new Bundle();
         state.putLong(ChatActivity.SHOW_CONVERSATION_ID_KEY, c.getID()); // Todo: refactor
         intentOpenConversation.putExtras(state); 
         startActivityForResult(intentOpenConversation, REQUEST_CODE_SHOW_CONVERSATION);
      }
   }
}
