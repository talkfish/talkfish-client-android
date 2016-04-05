package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Channel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;



public class EditChannelActivity extends Activity {

   protected Button btnOk;
   protected TextView idText;
   protected TextView nameEdit;
   protected TextView protocolText;
   protected EditText endpointEdit;

   private Channel chan;

   public final static String EDIT_CHANNEL_ID_KEY = "EDIT_CHANNEL_ID_KEY";


   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_editchannel);

      btnOk = (Button) findViewById(R.id.buttonEditChannelOk);
      if (btnOk != null) {
         btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               int affectedRows = 0;
               if (null != chan && null != endpointEdit && 
                   endpointEdit.getText() != null &&
                   endpointEdit.getText().toString().trim().length() != 0 &&
                   endpointEdit.getText().toString().trim() != chan.endpoint) {
                   chan.endpoint = endpointEdit.getText().toString().trim();
                   affectedRows = ((TFApp)(EditChannelActivity.this.getApplication()))
                     .getDAH().updateChannel(chan);
               }
               if (1 == affectedRows) {
                  Intent reply = new Intent();
                  Bundle result = new Bundle();
                  setResult(RESULT_OK, reply);
                  finish();
               } else {
                  Intent reply = new Intent();
                  Bundle result = new Bundle();
                  setResult(RESULT_CANCELED, reply);
                  finish();
               }
            }
         });
      }

      Intent data = getIntent();
      if (data.hasExtra(EDIT_CHANNEL_ID_KEY)) {
         int channelID = data.getIntExtra(EDIT_CHANNEL_ID_KEY, -1);
         chan = ((TFApp)(this.getApplication())).getDAH().loadChannel(channelID);
         idText = (TextView) findViewById(R.id.viewChannelID);
         nameEdit = (TextView) findViewById(R.id.viewChannelName);
         protocolText = (TextView) findViewById(R.id.viewChannelProtocol);
         endpointEdit = (EditText) findViewById(R.id.editChannelEndpoint);
         if (null != chan && 
             null != idText && 
             null != nameEdit &&
             null != protocolText &&
             null != endpointEdit) 
         {
            idText.setText(String.format("%d", chan.id));
            nameEdit.setText(chan.name);
            protocolText.setText(chan.protocol);
            endpointEdit.setText(chan.endpoint);
         }
         else {
            Toast.makeText(this, String.format("Technischer Fehler. Kanal mit ID %d kann nicht angezeigt werden.", channelID),
            Toast.LENGTH_LONG).show();
         }
      }
      
   }

}
