package de.kochon.enrico.secrettalkmessenger.activities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import android.content.Intent;

import android.view.View.OnClickListener;
import android.view.View;

import de.kochon.enrico.secrettalkmessenger.R;

import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;


public class CreateNewConversationActivity extends Activity {

   public final static String NEW_CONVERSATION_ID_KEY = "NEW_CONVERSATION_ID_KEY";

   protected Channel defaultSending;
   protected Channel defaultReceiving;
   
   protected Button btnOk;
   protected EditText convName;

   protected EditText sendingFish;
   protected EditText receivingFish;


   private String generatedSendingEndpoint;

   protected class InitConversationOnServerTask extends AsyncTask<String, Void, String> {
      protected final String ENDPOINT_INIT_CONVERSATION = "init.php";
      protected final String ENDPOINT_INIT_CONVERSATION_ERROR = "ERROR";

      public String initConversationOnServer(String url) {
         boolean success = false;
         String result = "";
         try {
            Log.d(TFApp.LOGKEY, "initConversationOnServer");
            String sendtarget = String.format("%s/%s", url, ENDPOINT_INIT_CONVERSATION);
            Log.d(TFApp.LOGKEY, String.format("calling %s", sendtarget));
            URL website = new URL(sendtarget);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine = in.readLine();
            in.close();

            if (inputLine == null || inputLine.equals("") || inputLine.equals(ENDPOINT_INIT_CONVERSATION_ERROR)) {
               success = false;
            } else {
               result = inputLine;
            }

         } catch (Exception ex) {
            success = false;
            ((TFApp) (CreateNewConversationActivity.this.getApplication())).logException(ex);
         }
         return result;
      }


      @Override
      protected String doInBackground(String... url) {
         String conversationEndpoint = "";
         try {
            if ((url != null) && (url.length == 1)) {
               conversationEndpoint = initConversationOnServer(url[0]);
            }

         } catch (Exception ex) {
            conversationEndpoint = "";
            ((TFApp) (CreateNewConversationActivity.this.getApplication())).logException(ex);
         }
         return conversationEndpoint;
      }


      @Override
      protected void onPreExecute() {
         ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_spinner_createnewconv);
         progressBar.setVisibility(View.VISIBLE);
         generatedSendingEndpoint = "";
      }


      @Override
      protected void onPostExecute(String conversationEndpoint) {
         if (!conversationEndpoint.equals("")) {
            Toast.makeText(CreateNewConversationActivity.this, conversationEndpoint, Toast.LENGTH_LONG).show();
            generatedSendingEndpoint = conversationEndpoint;
            if (sendingFish!=null) {
               sendingFish.setText(generatedSendingEndpoint);
            }
         } else {
            Toast.makeText(CreateNewConversationActivity.this, R.string.errorNewConversationOffline, Toast.LENGTH_LONG).show();
            Intent reply = new Intent();
            setResult(RESULT_CANCELED, reply);
            finish();
         }
         ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_spinner_createnewconv);
         progressBar.setVisibility(View.INVISIBLE);

      }
   }


   private void createNewConversation(String sendingEndpoint, String receivingEndpoint) {
      if (convName != null) {
         // create Conversation
         String name = convName.getText().toString();
         if (!name.equals("")) {
            String basePath = ((TFApp) (CreateNewConversationActivity.this.getApplication())).configHelper.getServerBaseURL() + "/";
            Channel newChannelReceiving = new Channel(name, basePath+receivingEndpoint, true);
            Channel newChannelSending = new Channel(name, basePath+sendingEndpoint, false);
            long receive_rowid = ((TFApp)(CreateNewConversationActivity.this.getApplication()))
                    .getDAH().addNewChannel(newChannelReceiving);
            long send_rowid = ((TFApp)(CreateNewConversationActivity.this.getApplication()))
                    .getDAH().addNewChannel(newChannelSending);
            if(-1 != receive_rowid && -1 != send_rowid) {
               Conversation newConversation = new Conversation(name, newChannelReceiving, newChannelSending);
               long rowid = ((TFApp)(CreateNewConversationActivity.this.getApplication()))
                       .getDAH().addNewConversation(newConversation);
               if (-1 != rowid)
               {
                  // build reply
                  Intent reply = new Intent();
                  Bundle result = new Bundle();
                  result.putLong(CreateNewConversationActivity.NEW_CONVERSATION_ID_KEY, rowid);
                  reply.putExtras(result);
                  setResult(RESULT_OK, reply);
                  finish();
               } else {
                  String message = String.format("Fehler: Die neue Unterhaltung mit <%s> konnte der internen Datenbank nicht hinzugefügt werden.", name);
                  Toast.makeText(CreateNewConversationActivity.this, message, Toast.LENGTH_LONG).show();
                  ((TFApp) (CreateNewConversationActivity.this.getApplication())).addToApplicationLog(message);
               }
            }

         }
      }
   }

/*
   protected boolean initDefaultChannels() {
      boolean foundReceiving = false;
      boolean foundSending = false;
      final List<Channel> channels = ((TFApp)(this.getApplication())).getDAH().loadAllChannels();
      final ArrayList<String> channelEntries = new ArrayList<String>();
      for(Channel c: channels) { 
         if (c.name.equals("default") && c.protocol.equals("secrettalk") && c.isforreceiving) 
         { foundReceiving = true; defaultReceiving = c; }
         if (c.name.equals("default") && c.protocol.equals("secrettalk") && !c.isforreceiving) 
         { foundSending = true; defaultSending = c; }
      }
      return foundReceiving && foundSending;
   }
*/

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_createnewconversationactivity);
      btnOk = (Button) findViewById(R.id.buttonCreateNewConversationActivityOk);
      convName = (EditText) findViewById(R.id.editNewConversationName);
      receivingFish = (EditText) findViewById(R.id.editNewConversationReceivingFish);

      if (btnOk != null && convName != null && receivingFish != null) {
         btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               String receivingEndpoint = receivingFish.getText().toString();
               if (generatedSendingEndpoint != null && generatedSendingEndpoint != "" && receivingEndpoint != "") {
                  Toast.makeText(CreateNewConversationActivity.this, getString(R.string.activityNewConversationInfoProcessStarted), Toast.LENGTH_LONG).show();
                  createNewConversation(generatedSendingEndpoint, receivingEndpoint);
               }
               }
         });
      }

      sendingFish = (EditText) findViewById(R.id.editNewConversationSendingFish);
      if (sendingFish != null) {
         sendingFish.setText(getString(R.string.editNewConversationSendingFishDefault));
         sendingFish.setEnabled(false);
      }

      InitConversationOnServerTask initConversationOnServerTask = new InitConversationOnServerTask();
      initConversationOnServerTask.execute(((TFApp) (CreateNewConversationActivity.this.getApplication())).configHelper.getServerBaseURL());

   }
}
