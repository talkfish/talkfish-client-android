package de.kochon.enrico.secrettalkmessenger.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;
import de.kochon.enrico.secrettalkmessenger.model.EncryptedMessage;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

import android.text.util.Linkify;


public class ChatActivity extends Activity {
	
    private EditText chatMessage;
    private TextView conversationNick;
    private ScrollView chatscroll;
	
    private Conversation conversation;
    private Channel receivingChan;
    private Channel sendingChan;
    private CountedMessage currentMessageToBeSent;


   public final static String SHOW_CONVERSATION_ID_KEY = "SHOW_CONVERSATION_ID_KEY";


   protected class SendMessageTask extends AsyncTask<String, Void, Boolean> {

      public boolean pushMessageToServer(String url, String message) {
         boolean success = false;
         try {
            Log.d(TFApp.LOGKEY, "pushMessageToServer");
            String sendtarget = String.format("%s?m=%s", url, message);
			   Log.d(TFApp.LOGKEY, String.format("calling %s", sendtarget));
				URL website = new URL(sendtarget);
		      URLConnection connection = website.openConnection();
		      BufferedReader in = new BufferedReader(
		                                new InputStreamReader(
		                                    connection.getInputStream()));
		      String inputLine;
		      while ((inputLine = in.readLine()) != null)  {
               // do nothing
		      }
		        
		      in.close();
            success = true;
			} catch (Exception ex) {
            success = false;
            TFApp.logException(ex);
			}
         return success;
      }


      @Override
      protected Boolean doInBackground(String... url_message) {
         boolean success = false;
         try {
            if ( (url_message != null) && (url_message.length == 2) ) {
               success = pushMessageToServer(url_message[0], url_message[1]);
            }
            
			} catch (Exception ex) {
            success = false;
            TFApp.logException(ex);
			}
         return success;
      }


      @Override
      protected void onPreExecute() {
         ChatActivity.this.setProgressBarIndeterminateVisibility(true);
      }


      @Override
      protected void onPostExecute(Boolean success) {
         if (success) {
             chatMessage.setText("");
             currentMessageToBeSent = conversation.addSentMessage(currentMessageToBeSent);
             ((TFApp)(ChatActivity.this.getApplication())).getDAH().addNewMessage(currentMessageToBeSent);
             ((TFApp)(ChatActivity.this.getApplication())).getDAH().updateConversation(conversation);

         } else {
             currentMessageToBeSent = null;
             Toast.makeText(ChatActivity.this, "Die Nachricht konnte leider nicht gesendet werden!", Toast.LENGTH_LONG).show();
         }
         ChatActivity.this.fullReload();
         ChatActivity.this.setProgressBarIndeterminateVisibility(false);
         ChatActivity.this.scrollDown();
         chatscroll.requestFocus(); // close softkeyboard
      }
   }

   private void init() {
      Intent data = getIntent();
      if (data.hasExtra(SHOW_CONVERSATION_ID_KEY)) {
         Long conversationID = data.getLongExtra(SHOW_CONVERSATION_ID_KEY, -1);
         conversation = ((TFApp)(this.getApplication())).getDAH().loadConversation(conversationID);
         receivingChan = conversation.getChannelForReceiving();
         sendingChan = conversation.getChannelForSending();
      }
      if (null!=conversationNick) {
         conversationNick.setText(String.format("Unterhaltung mit %s", conversation.getNick()));
      }
      if (null != chatMessage) {
         if (!conversation.hasAtLeastOneKeyForSending()) {
            chatMessage.setText("Schlüssel fehlen!");
            chatMessage.setEnabled(false);
         }
      }
   }

   // http://stackoverflow.com/questions/2667471/android-scroll-down
   private void scrollDown() {
      new Handler().postDelayed(new Runnable() {
         @Override
         public void run() {
            ScrollView mainChatScrollView = (ScrollView)findViewById(R.id.mainChatScrollView);
            if (mainChatScrollView != null) {
               mainChatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
         }
      },1000);

   }

   private boolean focuslock = false;

   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_chat);
		
      chatMessage = (EditText)findViewById(R.id.editNewMessageActivityChat);
      chatscroll = (ScrollView)findViewById(R.id.mainChatScrollView);
      conversationNick = (TextView)findViewById(R.id.textViewContactNickActivityChat);
      if ( (null != chatMessage) && (null != chatscroll)  ) {
         //chatscroll.setFocusable(true);
         //chatscroll.setFocusableInTouchMode(true);
         //chatscroll.requestFocus();
         chatMessage.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
               if (!focuslock) {
                  focuslock = true;
                  if (v==chatMessage) { 
                     if (hasFocus) {
                         ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(chatMessage, InputMethodManager.SHOW_FORCED);
                         scrollDown();
                         chatMessage.requestFocus();
                     } else {
                         ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(chatMessage.getWindowToken(), 0);
                     }
                  }
                  focuslock = false;
               }
            }
         });

         chatMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
               boolean handled = false;
               if (actionId == EditorInfo.IME_ACTION_SEND) {
                  ChatActivity.this.sendUserMessage();
                  handled = true;
                  ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(chatMessage.getWindowToken(), 0);
               }
               return handled;
            }
         });
      }


      init();

      fullReload();

      scrollDown();

      ((TFApp)(this.getApplication())).checkBackgroundService(this);
	}


   @Override
   public void onResume() {
      super.onResume();  // Always call the superclass method first

      init();

      fullReload();

   }


   public void fullReload() {
      Log.d(TFApp.LOGKEY, "fullReload");
      if (receivingChan != null) {
         clearChat();

         if (conversation != null) {
            for(CountedMessage cm: conversation.getMessages()) {
               StringBuilder sb = new StringBuilder();
               if (cm.getIsReceived()) {
                  sb.append(conversation.getNick());
               } else {
                  sb.append(((TFApp)(this.getApplication())).configHelper.getName());
               }
               sb.append(": ");
               sb.append(cm.getMessagebody());
               addChatMessage(sb.toString());
            }
         } else {
            Log.d(TFApp.LOGKEY, "fullReload: error conversation is null!");
         }
      } else {
         Toast.makeText(this, "FEHLER: Empfangskanal nicht vollständig geladen, Nachrichten können nicht aktualisiert werden.", Toast.LENGTH_LONG).show();
      }
   }


   public void clearChat() {
      Log.d(TFApp.LOGKEY, "clearChat");
		LinearLayout mainChatArea = (LinearLayout)findViewById(R.id.mainChatArea);
      if (mainChatArea != null) {
         mainChatArea.removeAllViews();
      }
   }


   public void sendUserMessage() {
      if (sendingChan != null && conversation != null && conversation.getSendKeyAmount()!=0) {
         SimpleDateFormat daytimeformat = new SimpleDateFormat("dd.MM./HH:mm");
         String message = String.format("%s>%s", daytimeformat.format(new Date()), ChatActivity.this.chatMessage.getText().toString().trim());

         byte fullmessagebytes[] = message.getBytes();
         String limitedmessage = message;
         if (fullmessagebytes.length > Messagekey.KEYBODY_LENGTH) {
            final byte limitedmessagebytes[] = Arrays.copyOfRange(fullmessagebytes, 0, Messagekey.KEYBODY_LENGTH);
            limitedmessage = new String(limitedmessagebytes, 0, Messagekey.KEYBODY_LENGTH);
         }
		   Log.d(TFApp.LOGKEY, String.format("trying to encrypt and send this message: >%s<", limitedmessage));

         currentMessageToBeSent = new CountedMessage(false, CountedMessage.NOTADDEDNUMBER, -1, limitedmessage, new Date() );

         Messagekey k = conversation.getKeyForSending();

         if (null != k) {
            EncryptedMessage cryptogram = EncryptedMessage.encrypt(currentMessageToBeSent, k);
            k.setIsUsed(true);
            ((TFApp)(this.getApplication())).getDAH().updateKey(k);

            SendMessageTask task = new SendMessageTask();

            String websafe = cryptogram.getWebsafeSerialization();
			   Log.d(TFApp.LOGKEY, String.format("trying to send %s", websafe));
            task.execute(sendingChan.endpoint, websafe);
         } else {
             currentMessageToBeSent = null;
             Toast.makeText(this, "Fehler: Kein zulässiger Schlüssel vorhanden!", Toast.LENGTH_LONG).show();
             Log.d(TFApp.LOGKEY, "ERROR: Could not obtain key!");
         }
      } else {
          currentMessageToBeSent = null;
          Toast.makeText(this, "ERROR: Chan for sending not properly loaded or keys for sending are missing, could not send message.", Toast.LENGTH_LONG).show();
          Log.d(TFApp.LOGKEY, "ERROR: Chan for sending not properly loaded or keys for sending are missing, could not send message.");
          if (sendingChan == null) {
		      Log.d(TFApp.LOGKEY, "ERROR: Chan for sending not properly loaded.");
          }
          if (conversation == null) {
		      Log.d(TFApp.LOGKEY, "ERROR: Conversation not properly loaded.");
         }
         if (conversation != null && conversation.getSendKeyAmount()==0) {
		      Log.d(TFApp.LOGKEY, "ERROR: Keys for sending are missing.");
         }
      }
   }

	
	public void addChatMessage(String message) {
       Log.d(TFApp.LOGKEY, String.format("addChatMessage(%s)", message));
       TextView newEntry = new TextView(ChatActivity.this);
       newEntry.setTextColor(getResources().getColor(R.color.app_foreground));
       newEntry.setText(String.format("%s", message));
       Linkify.addLinks(newEntry, Linkify.WEB_URLS);
       LinearLayout mainChatArea = (LinearLayout)findViewById(R.id.mainChatArea);
       if (mainChatArea != null) {
          mainChatArea.addView(newEntry);
       }
	}
}
