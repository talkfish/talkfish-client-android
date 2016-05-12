package de.kochon.enrico.secrettalkmessenger.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.CountedImageMessage;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;
import de.kochon.enrico.secrettalkmessenger.model.EncryptedMessage;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;
import de.kochon.enrico.secrettalkmessenger.model.StructuredMessageBody;

import android.text.util.Linkify;


public class ChatActivity extends Activity {

    private EditText chatMessage;
    private TextView conversationNick;
    private ScrollView chatscroll;
    private Button buttonSend;
    private Button buttonTakePhoto;


    private Conversation conversation;
    private Channel receivingChan;
    private Channel sendingChan;

    private CountedMessage currentTextMessageToBeSent;
    private int currentTextMessagePartAmount;
    private int currentTextMessagePartCounter;

    private CountedImageMessage currentImageMessageToBeSent;
    private int currentImageMessagePartAmount;
    private int currentImageMessagePartCounter;


    public final static String SHOW_CONVERSATION_ID_KEY = "SHOW_CONVERSATION_ID_KEY";

    public final int MAX_IMAGE_SIZE = 1024 * 7;

    private final int INTENT_REQUEST_IMAGE_CAPTURE = 1;


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
                while ((inputLine = in.readLine()) != null) {
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
        protected Boolean doInBackground(String... url_messageweb) {
            boolean success = false;
            try {
                if ((url_messageweb != null) && (url_messageweb.length == 2)) {
                    success = pushMessageToServer(url_messageweb[0], url_messageweb[1]);
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
                currentTextMessagePartCounter += 1;
                if (currentTextMessagePartCounter == currentTextMessagePartAmount) {
                    chatMessage.setText("");
                    CountedMessage m = conversation.addSentMessage(currentTextMessageToBeSent);
                    ((TFApp) (ChatActivity.this.getApplication())).getDAH().addNewMessage(m);
                    ((TFApp) (ChatActivity.this.getApplication())).getDAH().updateConversation(conversation);
                }

            } else {
                currentTextMessageToBeSent = null;
                Toast.makeText(ChatActivity.this, "Die Nachricht konnte leider nicht gesendet werden!", Toast.LENGTH_LONG).show();
            }
            ChatActivity.this.fullReload();
            ChatActivity.this.setProgressBarIndeterminateVisibility(false);
            ChatActivity.this.scrollDown();
            chatscroll.requestFocus(); // close softkeyboard
        }
    }

    protected class SendImageMessageTask extends SendMessageTask {
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                currentImageMessagePartCounter += 1;
                if (currentImageMessagePartCounter == currentImageMessagePartAmount) {
                    CountedMessage m = conversation.addSentMessage(currentImageMessageToBeSent);
                    ((TFApp) (ChatActivity.this.getApplication())).getDAH().addNewMessage(m);
                    ((TFApp) (ChatActivity.this.getApplication())).getDAH().updateConversation(conversation);
                }

            } else {
                currentImageMessageToBeSent = null;
                Toast.makeText(ChatActivity.this, "Das Photo konnte leider nicht gesendet werden!", Toast.LENGTH_LONG).show();
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
            conversation = ((TFApp) (this.getApplication())).getDAH().loadConversation(conversationID);
            receivingChan = conversation.getChannelForReceiving();
            sendingChan = conversation.getChannelForSending();
        }
        if (null != conversationNick) {
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
                ScrollView mainChatScrollView = (ScrollView) findViewById(R.id.mainChatScrollView);
                if (mainChatScrollView != null) {
                    mainChatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
        }, 1000);

    }

    private boolean focuslock = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_chat);

        chatMessage = (EditText) findViewById(R.id.editNewMessageActivityChat);
        chatscroll = (ScrollView) findViewById(R.id.mainChatScrollView);
        conversationNick = (TextView) findViewById(R.id.textViewContactNickActivityChat);

        if ((null != chatMessage) && (null != chatscroll)) {
            //chatscroll.setFocusable(true);
            //chatscroll.setFocusableInTouchMode(true);
            //chatscroll.requestFocus();
            chatMessage.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!focuslock) {
                        focuslock = true;
                        if (v == chatMessage) {
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

            buttonSend = (Button) findViewById(R.id.sendNewMessageActivityChat);
            if (null != buttonSend) {
                buttonSend.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      ChatActivity.this.sendUserMessage();
                                                      ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(chatMessage.getWindowToken(), 0);
                                                  }
                                              }
                );
            }
            buttonTakePhoto = (Button) findViewById(R.id.takePhotoActivityChat);
            if (null != buttonTakePhoto) {
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    buttonTakePhoto.setEnabled(false);
                }
                buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Intent takePictureIntent = new Intent(ChatActivity.this, CapturePhotoActivity.class);
                        //startActivityForResult(takePictureIntent, 0);

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, INTENT_REQUEST_IMAGE_CAPTURE);
                        }

                    }
                });
            }
        }


        init();

        fullReload();

        scrollDown();

        ((TFApp) (this.getApplication())).checkBackgroundService(this);
    }

    private ImageView mImageView;
    private Bitmap imageBitmap;
    private Bitmap resultBitmap;
    private ImageView resultView;

    private int countPNG;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (imageBitmap.compress(Bitmap.CompressFormat.JPEG, 15, stream)) {

                byte[] byteArray = stream.toByteArray();
                countPNG = byteArray.length;
                if (countPNG < MAX_IMAGE_SIZE) {
                    sendImage(byteArray);
                }
            } else {
                resultBitmap = null;
            }
        }
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
                for (CountedMessage cm : conversation.getMessages()) {
                    if (cm instanceof CountedImageMessage) {
                        addImageChatMessage((CountedImageMessage)cm);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        if (cm.getIsReceived()) {
                            sb.append(conversation.getNick());
                        } else {
                            sb.append(((TFApp) (this.getApplication())).configHelper.getName());
                        }
                        sb.append(": ");
                        sb.append(cm.getMessagebody());
                        addChatMessage(sb.toString(), !cm.getIsReceived());
                    }
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
        LinearLayout mainChatArea = (LinearLayout) findViewById(R.id.mainChatArea);
        if (mainChatArea != null) {
            mainChatArea.removeAllViews();
        }
    }

    public void sendUserMessage() {
        currentTextMessagePartCounter = 0;
        final int MAX_MULTIMESSAGE_COUNT = 100;

        SimpleDateFormat daytimeformat = new SimpleDateFormat("dd.MM./HH:mm");
        String dateannotatedmessage = String.format("%s:\n%s", daytimeformat.format(new Date()), ChatActivity.this.chatMessage.getText().toString().trim());
        currentTextMessageToBeSent = new CountedMessage(false, CountedMessage.NOTADDEDNUMBER, -1, dateannotatedmessage, new Date());
        byte fullmessagebytes[] = dateannotatedmessage.getBytes();
        currentTextMessagePartAmount = fullmessagebytes.length / StructuredMessageBody.PAYLOAD_LENGTH + 1;

        if (sendingChan != null && conversation != null
                && conversation.getSendKeyAmount() >= currentTextMessagePartAmount
                && currentTextMessagePartAmount <= MAX_MULTIMESSAGE_COUNT) {
            for (int i = 0; i < currentTextMessagePartAmount; i++) {
                int remainingByteCount = fullmessagebytes.length - i * StructuredMessageBody.PAYLOAD_LENGTH;
                int remainingBytesInCurrentMessagePart = Math.min(StructuredMessageBody.PAYLOAD_LENGTH, remainingByteCount);
                final byte limitedmessagebytes[] = Arrays.copyOfRange(fullmessagebytes,
                        i * StructuredMessageBody.PAYLOAD_LENGTH,
                        i * StructuredMessageBody.PAYLOAD_LENGTH + remainingBytesInCurrentMessagePart);
                StructuredMessageBody structuredPart = new StructuredMessageBody(limitedmessagebytes, false,
                        i, currentTextMessagePartAmount,
                        conversation.getCurrentNumberOfSentMessages() + 1);
                // TODO: not robust enough if sending of a huge message partially fails, the next try will reuse the same number -> garbage will be received
                Messagekey k = conversation.getKeyForSending();
                if (null != k) {
                    EncryptedMessage cryptogram = EncryptedMessage.encrypt(structuredPart, k);
                    k.setIsUsed(true);
                    ((TFApp) (this.getApplication())).getDAH().updateKey(k);

                    SendMessageTask task = new SendMessageTask();

                    String websafe = cryptogram.getWebsafeSerialization();
                    Log.d(TFApp.LOGKEY, String.format("trying to send %s", websafe));
                    task.execute(sendingChan.endpoint, websafe);
                } else {
                    currentTextMessageToBeSent = null;
                    Toast.makeText(this, "Fehler: Kein zulässiger Schlüssel vorhanden!", Toast.LENGTH_LONG).show();
                    Log.d(TFApp.LOGKEY, "ERROR: Could not obtain key!");
                }
            }
        } else {
            currentTextMessageToBeSent = null;
            Toast.makeText(this, "ERROR: Chan for sending not properly loaded or keys for sending are missing, could not send message.", Toast.LENGTH_LONG).show();
            Log.d(TFApp.LOGKEY, "ERROR: Chan for sending not properly loaded or keys for sending are missing, could not send message.");
            if (sendingChan == null) {
                Log.d(TFApp.LOGKEY, "ERROR: Chan for sending not properly loaded.");
            }
            if (conversation == null) {
                Log.d(TFApp.LOGKEY, "ERROR: Conversation not properly loaded.");
            }
            if (conversation != null && conversation.getSendKeyAmount() == 0) {
                Log.d(TFApp.LOGKEY, "ERROR: Keys for sending are missing.");
            }
        }
    }

    public void sendImage(byte[] imagebytes) {
        currentImageMessagePartCounter = 0;
        final int MAX_MULTIMESSAGE_COUNT = MAX_IMAGE_SIZE / StructuredMessageBody.PAYLOAD_LENGTH;

        currentImageMessageToBeSent = new CountedImageMessage(false, CountedMessage.NOTADDEDNUMBER, -1, imagebytes, new Date());
        currentImageMessagePartAmount = imagebytes.length / StructuredMessageBody.PAYLOAD_LENGTH + 1;

        if (sendingChan != null && conversation != null
                && conversation.getSendKeyAmount() >= currentImageMessagePartAmount
                && currentImageMessagePartAmount <= MAX_MULTIMESSAGE_COUNT) {
            for (int i = 0; i < currentImageMessagePartAmount; i++) {
                int remainingByteCount = imagebytes.length - i * StructuredMessageBody.PAYLOAD_LENGTH;
                int remainingBytesInCurrentMessagePart = Math.min(StructuredMessageBody.PAYLOAD_LENGTH, remainingByteCount);
                final byte limitedmessagebytes[] = Arrays.copyOfRange(imagebytes,
                        i * StructuredMessageBody.PAYLOAD_LENGTH,
                        i * StructuredMessageBody.PAYLOAD_LENGTH + remainingBytesInCurrentMessagePart);
                StructuredMessageBody structuredPart = new StructuredMessageBody(limitedmessagebytes, true,
                        i, currentImageMessagePartAmount,
                        conversation.getCurrentNumberOfSentMessages() + 1);
                // TODO: not robust enough if sending of a huge message partially fails, the next try will reuse the same number -> garbage will be received
                Messagekey k = conversation.getKeyForSending();
                if (null != k) {
                    EncryptedMessage cryptogram = EncryptedMessage.encrypt(structuredPart, k);
                    k.setIsUsed(true);
                    ((TFApp) (this.getApplication())).getDAH().updateKey(k);

                    SendMessageTask task = new SendImageMessageTask();

                    String websafe = cryptogram.getWebsafeSerialization();
                    Log.d(TFApp.LOGKEY, String.format("trying to send %s", websafe));
                    task.execute(sendingChan.endpoint, websafe);
                } else {
                    currentImageMessageToBeSent = null;
                    Toast.makeText(this, "Fehler: Kein zulässiger Schlüssel vorhanden!", Toast.LENGTH_LONG).show();
                    Log.d(TFApp.LOGKEY, "ERROR: Could not obtain key!");
                }
            }
        } else {
            currentImageMessageToBeSent = null;
            Toast.makeText(this, "ERROR: Chan for sending not properly loaded or keys for sending are missing, could not send message.", Toast.LENGTH_LONG).show();
            Log.d(TFApp.LOGKEY, "ERROR: Chan for sending not properly loaded or keys for sending are missing, could not send message.");
            if (sendingChan == null) {
                Log.d(TFApp.LOGKEY, "ERROR: Chan for sending not properly loaded.");
            }
            if (conversation == null) {
                Log.d(TFApp.LOGKEY, "ERROR: Conversation not properly loaded.");
            }
            if (conversation != null && conversation.getSendKeyAmount() == 0) {
                Log.d(TFApp.LOGKEY, "ERROR: Keys for sending are missing.");
            }
        }
    }

    protected void setLayout(View v, boolean isMoi) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (isMoi) {
            v.setBackgroundColor(0x70aaaaee);
            params.setMargins(16, 16, 64, 16);
            params.gravity = Gravity.LEFT;
        } else {
            v.setBackgroundColor(0x70aaeeaa);
            params.setMargins(64, 16, 16, 16);
            params.gravity = Gravity.RIGHT;
        }

        v.setLayoutParams(params);
    }


    protected void addToChat(View v) {
        LinearLayout mainChatArea = (LinearLayout) findViewById(R.id.mainChatArea);
        if (mainChatArea != null) {
            mainChatArea.addView(v);
        }
    }


    public void addChatMessage(String message, boolean isMoi) {
        Log.d(TFApp.LOGKEY, String.format("addChatMessage(%s)", message));
        TextView newEntry = new TextView(ChatActivity.this);
        newEntry.setTextColor(getResources().getColor(R.color.app_foreground));
        newEntry.setText(String.format("%s", message));
        Linkify.addLinks(newEntry, Linkify.WEB_URLS);
        setLayout(newEntry, isMoi);

        addToChat(newEntry);
    }


    public void addImageChatMessage(CountedImageMessage cim) {
        Log.d(TFApp.LOGKEY, "addImageChatMessage()");

        ByteArrayInputStream is = new ByteArrayInputStream(cim.getImagedata());
        Bitmap imageBitmap = BitmapFactory.decodeStream(is);

        ImageView imageBitmapView = new ImageView(ChatActivity.this);
        imageBitmapView.setImageBitmap(imageBitmap);
        imageBitmapView.setScaleX(1.0f);
        imageBitmapView.setScaleY(1.0f);
        setLayout(imageBitmapView, !cim.getIsReceived());

        addToChat(imageBitmapView);
    }
}
