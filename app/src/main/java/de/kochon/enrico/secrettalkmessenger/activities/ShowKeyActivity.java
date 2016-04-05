package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;
import de.kochon.enrico.secrettalkmessenger.views.CharView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


public class ShowKeyActivity extends Activity {

   protected Button btnSend;
   protected Button btnDelete;
   protected Button btnBack;
   protected TextView txtSerializedKey;

   protected Messagekey key;
   

   public final static String MESSAGEKEY_ID_KEY = "MESSAGEKEY_ID_KEY";


   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_showkey);

      Intent data = getIntent();
      if (data.hasExtra(MESSAGEKEY_ID_KEY)) {
         long keyID = data.getLongExtra(MESSAGEKEY_ID_KEY, -1);
         key = ((TFApp)(this.getApplication())).getDAH().loadMessagekey(keyID);
		   CharView codeView = (CharView) findViewById(R.id.keyRepresentationGraphicalView);
         txtSerializedKey = (TextView) findViewById(R.id.textviewSerializedKey);
         if (null != key && 
             null != codeView &&
             null != txtSerializedKey) 
         {
            codeView.setKey(key.getHeaderID(), key.getKeybody());
            String serializedKey = key.getWebsafeSerialization();
            txtSerializedKey.setText(serializedKey);
         }
         else {
            Toast.makeText(this, String.format("Technischer Fehler. Messagekey mit ID %d kann nicht angezeigt werden.", keyID),
            Toast.LENGTH_LONG).show();
         }
      }

      btnSend = (Button) findViewById(R.id.buttonShowKeyActivitySendOverAir);
      if (btnSend != null) {
         if ((key!=null) && (!key.getIsExchanged())) {
            btnSend.setEnabled(true);
         } else {
            btnSend.setEnabled(false);
         }
         btnSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               Intent intentSendKeyByBluetooth = new Intent(ShowKeyActivity.this, SendSingleKeyByBluetoothActivity.class);
               Bundle state = new Bundle();
               state.putLong(ShowKeyActivity.MESSAGEKEY_ID_KEY, key.getID());
               intentSendKeyByBluetooth.putExtras(state); 
               startActivityForResult(intentSendKeyByBluetooth,0);
            }
         });
      }

      btnDelete = (Button) findViewById(R.id.buttonShowKeyActivityDelete);
      if (btnDelete != null) {
         btnDelete.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               if (ShowKeyActivity.this.key != null) {
                  int affectedRows = ((TFApp)(ShowKeyActivity.this.getApplication())).getDAH().deleteMessagekey(ShowKeyActivity.this.key.getID());
                  if (1 == affectedRows) {
                     Intent reply = new Intent();
                     Bundle result = new Bundle();
                     setResult(RESULT_OK, reply);
                     finish();
                  } else {
                     if (0 == affectedRows) {
                        Toast.makeText(ShowKeyActivity.this, 
                           String.format("ERROR: deleting of key %d was not successful",ShowKeyActivity.this.key.getID()), 
                           Toast.LENGTH_LONG).show();
                     } else {
                        Toast.makeText(ShowKeyActivity.this, 
                           String.format("ERROR: deletion of key %d yielded a deletion of %d rows",ShowKeyActivity.this.key.getID(), affectedRows), 
                           Toast.LENGTH_LONG).show();
                     }
                        
                  }
               }
            }
         });
      }

      btnBack = (Button) findViewById(R.id.buttonShowKeyActivityBack);
      if (btnBack != null) {
         btnBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               // build reply
               Intent reply = new Intent();
               Bundle result = new Bundle();
               setResult(RESULT_OK, reply);
               finish();
            }
         });
      }

   }
}
