package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class AddKeysActivity extends AppCompatActivity {

    protected Button btnRenameConversation;
    protected Button btnDeleteMessages;
    protected Button btnDeleteConversation;
    protected Button btnAddAndSend;
    protected Button btnReceive;
    protected Button btnKeys;
    protected TextView nameText;
    protected TextView conversationDetails;
    protected TextView keyDetails;
    protected Conversation conversation;
    protected long conversationID;

    public final static String SHOW_CONVERSATION_ID_KEY = "SHOW_CONVERSATION_ID_KEY";

    public final static int REQUESTCODE_CHANGE_CONVERSATION_NAME = 1;

    public final static int RESULT_CONVERSATION_DELETED = 23;


    protected void initConversation() {
        conversation = ((TFApp) (this.getApplication())).getDAH().loadConversation(conversationID);
        nameText = (TextView) findViewById(R.id.viewConversationName);
        conversationDetails = (TextView) findViewById(R.id.viewConversationMessageDetails);
        keyDetails = (TextView) findViewById(R.id.viewConversationKeyDetails);
        if (null != conversation && null != nameText && null != conversationDetails && null != keyDetails) {
            nameText.setText(conversation.getNick());

            conversationDetails.setSingleLine(false);
            SimpleDateFormat simpleGermanDate = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat simpleTime = new SimpleDateFormat("HH:mm:ss");
            String dateFormatted = simpleGermanDate.format(conversation.getLastMessageTime());
            String timeFormatted = simpleTime.format(conversation.getLastMessageTime());
            conversationDetails.setText(String.format("Letzte Nachricht am %s um %s erhalten.\n\n",
                    dateFormatted, timeFormatted));

            keyDetails.setSingleLine(false);
            keyDetails.setText(String.format("Sendeschlüssel: %d\nEmpfangsschlüssel: %d",
                    conversation.countActiveSendKeys(), conversation.countActiveReceiveKeys()));
        } else {
            Toast.makeText(this, String.format("Technischer Fehler. Unterhaltung mit ID %d kann nicht angezeigt werden.", conversationID),
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settingsmenu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                finish();
                return true;
            case (R.id.action_about):
                Intent intentWelcome = new Intent(AddKeysActivity.this, WelcomeActivity.class);
                startActivityForResult(intentWelcome, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addkeys);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar_for_addKeys);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent data = getIntent();
        if (data.hasExtra(SHOW_CONVERSATION_ID_KEY)) {
            conversationID = data.getLongExtra(SHOW_CONVERSATION_ID_KEY, -1);
            initConversation();
        }


        btnRenameConversation = (Button) findViewById(R.id.buttonShowConversationPropertiesActivityRenameConversation);
        if (btnRenameConversation != null) {
            btnRenameConversation.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (null != AddKeysActivity.this.conversation && -1 != AddKeysActivity.this.conversation.getID()) {
                        ArrayList<String> bundleParam = new ArrayList<String>();
                        String caption = "Name des Kontaktes ändern.";
                        String currentName = AddKeysActivity.this.conversation.getNick();
                        bundleParam.add(caption);
                        bundleParam.add(currentName);
                        Intent intentRename = new Intent(AddKeysActivity.this, RenameActivity.class);
                        Bundle state = new Bundle();
                        state.putStringArrayList(RenameActivity.STRINGPARAM_KEY, bundleParam);
                        intentRename.putExtras(state);
                        startActivityForResult(intentRename, REQUESTCODE_CHANGE_CONVERSATION_NAME);
                    } else {
                        Toast.makeText(AddKeysActivity.this, "Technischer Fehler. Kontakt kann nicht umbenannt werden.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        btnDeleteMessages = (Button) findViewById(R.id.buttonShowConversationPropertiesActivityDeleteMessages);
        if (btnDeleteMessages != null) {
            btnDeleteMessages.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (null != AddKeysActivity.this.conversation && -1 != AddKeysActivity.this.conversation.getID()) {
                        int rows = ((TFApp) (AddKeysActivity.this.getApplication())).
                                getDAH().deleteMessagesAndUsedKeys(
                                AddKeysActivity.this.conversation.getID());
                        if (rows > 0) {
                            Intent reply = new Intent();
                            Bundle result = new Bundle();
                            setResult(RESULT_OK, reply);
                            finish();
                        } else {
                            Toast.makeText(AddKeysActivity.this,
                                    String.format("Technischer Fehler. Nachrichten aus Unterhaltung mit ID %d können nicht gelöscht werden.", AddKeysActivity.this.conversation.getID()),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AddKeysActivity.this, "Technischer Fehler. Nachrichten können nicht gelöscht werden.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        btnDeleteConversation = (Button) findViewById(R.id.buttonShowConversationPropertiesActivityDeleteConversation);
        if (btnDeleteConversation != null) {
            btnDeleteConversation.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (null != AddKeysActivity.this.conversation && -1 != AddKeysActivity.this.conversation.getID()) {
                        int rows = ((TFApp) (AddKeysActivity.this.getApplication())).
                                getDAH().deleteConversation(
                                AddKeysActivity.this.conversation.getID());
                        if (rows > 0) {
                            Intent reply = new Intent();
                            Bundle result = new Bundle();
                            setResult(RESULT_CONVERSATION_DELETED, reply);
                            finish();
                        } else {
                            Toast.makeText(AddKeysActivity.this,
                                    String.format("Technischer Fehler. Unterhaltung mit ID %d kann nicht gelöscht werden.", AddKeysActivity.this.conversation.getID()),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AddKeysActivity.this, "Technischer Fehler. Unterhaltung kann nicht gelöscht werden.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        btnAddAndSend = (Button) findViewById(R.id.buttonKeysAddAndSend);
        if (btnAddAndSend != null) {
            btnAddAndSend.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (AddKeysActivity.this.conversation != null) {
                        Intent intentAddAndSendKeys = new Intent(AddKeysActivity.this, SendKeyBatchByBluetoothActivity.class);
                        Bundle state = new Bundle();
                        state.putLong(SendKeyBatchByBluetoothActivity.SHOW_CONVERSATION_ID_KEY, AddKeysActivity.this.conversation.getID());
                        intentAddAndSendKeys.putExtras(state);
                        startActivityForResult(intentAddAndSendKeys, 0);
                    } else {
                        Toast.makeText(AddKeysActivity.this, "Technischer Fehler. Die Unterhaltung wurde nicht geladen.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        btnReceive = (Button) findViewById(R.id.buttonKeysReceive);
        if (btnReceive != null) {
            btnReceive.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intentReceiveKeyByBluetooth = new Intent(AddKeysActivity.this, ReceiveKeyByBluetoothActivity.class);
                    Bundle state = new Bundle();
                    state.putLong(ReceiveKeyByBluetoothActivity.SHOW_CONVERSATION_ID_KEY, AddKeysActivity.this.conversation.getID());
                    intentReceiveKeyByBluetooth.putExtras(state);
                    startActivityForResult(intentReceiveKeyByBluetooth, 0);
                }
            });
        }

        btnKeys = (Button) findViewById(R.id.buttonShowConversationPropertiesActivityKeys);
        if (btnKeys != null && conversation != null) {
            btnKeys.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intentOpenKeymanagement = new Intent(AddKeysActivity.this, KeyListActivity.class);
                    Bundle state = new Bundle();
                    state.putLong(KeyListActivity.CONVERSATION_ID_KEY, conversation.getID());
                    intentOpenKeymanagement.putExtras(state);
                    startActivityForResult(intentOpenKeymanagement, 0);
                }
            });
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                if ((REQUESTCODE_CHANGE_CONVERSATION_NAME == requestCode)
                        && (data.hasExtra(RenameActivity.STRINGRESULT_KEY))) {
                    String newConversationName = data.getStringExtra(RenameActivity.STRINGRESULT_KEY);
                    if (null != newConversationName) {
                        conversation.setNick(newConversationName);
                        if (1 == ((TFApp) (this.getApplication())).getDAH().updateConversation(conversation)) {
                            initConversation();
                            Toast.makeText(this, "Kontaktname geändert.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Fehler beim Speichern der Änderung.", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    initConversation(); // refresh KeyInfo
                }
                break;
            case Activity.RESULT_CANCELED:
                break;
            default:
        }
    }
}
