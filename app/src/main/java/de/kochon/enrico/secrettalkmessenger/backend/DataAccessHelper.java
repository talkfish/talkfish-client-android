package de.kochon.enrico.secrettalkmessenger.backend;

import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.CountedImageMessage;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.TreeSet;
import java.text.DateFormat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;

public class DataAccessHelper {

    private SqlOpenHelper dbhelper;


    public DataAccessHelper(SqlOpenHelper dbhelper) {
        this.dbhelper = dbhelper;
    }


    public List<Channel> loadAllChannels() {
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_CHANNELS,
                new String[]{SqlOpenHelper.CHANNELS_COLUMN_ID, SqlOpenHelper.CHANNELS_COLUMN_NAME,
                        SqlOpenHelper.CHANNELS_COLUMN_PROTOCOL, SqlOpenHelper.CHANNELS_COLUMN_ENDPOINT,
                        SqlOpenHelper.CHANNELS_COLUMN_ISFORRECEIVING},
                null, null, null, null, null);
        List<Channel> channels = new ArrayList<Channel>();

        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
            do {
                channels.add(new Channel(
                                listCursor.getInt(0),
                                listCursor.getString(1),
                                listCursor.getString(2),
                                listCursor.getString(3),
                                (listCursor.getInt(4) > 0))
                );
            } while (listCursor.moveToNext());
        }
        listCursor.close();

        return channels;
    }


    public Channel loadChannel(long id) {
        SQLiteDatabase database = dbhelper.getReadableDatabase();
        return loadChannel(database, id);
    }


    public Channel loadChannel(SQLiteDatabase database, long id) {
        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_CHANNELS,
                new String[]{SqlOpenHelper.CHANNELS_COLUMN_ID, SqlOpenHelper.CHANNELS_COLUMN_NAME,
                        SqlOpenHelper.CHANNELS_COLUMN_PROTOCOL, SqlOpenHelper.CHANNELS_COLUMN_ENDPOINT,
                        SqlOpenHelper.CHANNELS_COLUMN_ISFORRECEIVING},
                null, null, null, null,
                SqlOpenHelper.CHANNELS_COLUMN_ID);
        List<Channel> channels = new ArrayList<Channel>();

        listCursor.moveToFirst();

        Channel needle = null;
        if (!listCursor.isAfterLast()) {
            do {
                Log.d(TFApp.LOGKEY, String.format("loading channel, "
                                + "searching ... - current id/val: %d/%s",
                        listCursor.getLong(0), listCursor.getString(1)));
                if (id == listCursor.getLong(0)) {
                    needle = new Channel(
                            listCursor.getInt(0),
                            listCursor.getString(1),
                            listCursor.getString(2),
                            listCursor.getString(3),
                            (listCursor.getInt(4) > 0));
                }
            } while ((null == needle) && listCursor.moveToNext());
        }
        listCursor.close();

        return needle;
    }


    public ArrayList<Messagekey> loadAllKeysForReceiving(long conversationID) {
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS,
                new String[]{SqlOpenHelper.MESSAGEKEYS_COLUMN_ID,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_KEYBODY,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISFORRECEIVING,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYUSED},
                String.format("%s=%d and %s=1", SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION, conversationID, SqlOpenHelper.MESSAGEKEYS_COLUMN_ISFORRECEIVING),
                null, null, null, SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION);
        ArrayList<Messagekey> keys = new ArrayList<Messagekey>();

        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
            do {
                Messagekey k = new Messagekey(
                        listCursor.getInt(0),
                        listCursor.getBlob(2),
                        listCursor.getBlob(3),
                        (listCursor.getInt(4) > 0),
                        (listCursor.getInt(5) > 0),
                        (listCursor.getInt(6) > 0));

                //Log.d(TFApp.LOGKEY, String.format("loadAllKeysForReceiving: loading key %s for conversation %d",
                //                                                           k.toString(), conversationID));
                keys.add(k);
            } while (listCursor.moveToNext());
        }
        listCursor.close();

        return keys;
    }

    public ArrayList<Messagekey> loadAllKeysForSending(long conversationID) {
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS,
                new String[]{SqlOpenHelper.MESSAGEKEYS_COLUMN_ID,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_KEYBODY,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISFORRECEIVING,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYUSED},
                String.format("%s=%d and %s=0", SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION, conversationID, SqlOpenHelper.MESSAGEKEYS_COLUMN_ISFORRECEIVING),
                null, null, null, SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION);
        ArrayList<Messagekey> keys = new ArrayList<Messagekey>();

        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
            do {
                Messagekey k = new Messagekey(
                        listCursor.getInt(0),
                        listCursor.getBlob(2),
                        listCursor.getBlob(3),
                        (listCursor.getInt(4) > 0),
                        (listCursor.getInt(5) > 0),
                        (listCursor.getInt(6) > 0));

                //Log.d(TFApp.LOGKEY, String.format("loadAllKeysForSending: loading key %s for conversation %d",
                //                                                           k.toString(), conversationID));
                keys.add(k);
            } while (listCursor.moveToNext());
        }
        listCursor.close();

        return keys;
    }


    // TODO: load more efficient
    public Messagekey loadMessagekey(long messagekeyID) {
        Messagekey needle = null;
        boolean hasNeedle = false;
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS,
                new String[]{SqlOpenHelper.MESSAGEKEYS_COLUMN_ID,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_KEYBODY,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISFORRECEIVING,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED,
                        SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYUSED},
                null, null, null, null, SqlOpenHelper.MESSAGEKEYS_COLUMN_ID);

        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
            do {
                if (listCursor.getInt(0) == messagekeyID) {
                    needle = new Messagekey(
                            listCursor.getInt(0),
                            listCursor.getBlob(1),
                            listCursor.getBlob(2),
                            (listCursor.getInt(3) > 0),
                            (listCursor.getInt(4) > 0),
                            (listCursor.getInt(5) > 0));
                    hasNeedle = true;
                }

            } while (!hasNeedle && listCursor.moveToNext());
        }
        listCursor.close();

        return needle;
    }


    public int deleteMessagekey(long messagekeyID) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String whereString = SqlOpenHelper.MESSAGEKEYS_COLUMN_ID + "=?";
        String[] whereArgs = new String[]{String.format("%d", messagekeyID)};

        int affectedRows = db.delete(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS, whereString, whereArgs);
        return affectedRows;
    }


    public List<Conversation> loadAllConversations() {
        List<Long> allConversationIDs = loadAllConversationIDs();
        List<Conversation> allConversations = new ArrayList<Conversation>();
        for (Long convID : allConversationIDs) {
            Conversation conv = loadConversation(convID);
            if (null != conv) {
                allConversations.add(conv);
            } else {
                Log.d(TFApp.LOGKEY, String.format("Could not load Conversation for id %d!", convID));
            }
        }
        return allConversations;
    }


    public List<Long> loadAllConversationIDs() {
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_CONVERSATIONS,
                new String[]{SqlOpenHelper.CONVERSATIONS_COLUMN_ID},
                null, null, null, null,
                SqlOpenHelper.CONVERSATIONS_COLUMN_LAST_MESSAGE_DATE);
        List<Long> entries = new ArrayList<Long>();

        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
            do {
                entries.add(listCursor.getLong(0));
            } while (listCursor.moveToNext());
        }
        listCursor.close();

        return entries;
    }


    /**
     *
     **/
    public Conversation loadConversation(long id) {
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_CONVERSATIONS,
                new String[]{SqlOpenHelper.CONVERSATIONS_COLUMN_ID,
                        SqlOpenHelper.CONVERSATIONS_COLUMN_IDCHANNEL_RECEIVING,
                        SqlOpenHelper.CONVERSATIONS_COLUMN_IDCHANNEL_SENDING,
                        SqlOpenHelper.CONVERSATIONS_COLUMN_NICK,
                        SqlOpenHelper.CONVERSATIONS_COLUMN_NUMBERRECEIVED,
                        SqlOpenHelper.CONVERSATIONS_COLUMN_NUMBERSENT,
                        SqlOpenHelper.CONVERSATIONS_COLUMN_LAST_MESSAGE_DATE},
                null, null, null, null,
                SqlOpenHelper.CONVERSATIONS_COLUMN_ID);

        boolean hasConversationID = false;
        String nick = "n.a.";
        int idchannel_receiving = -1;
        int idchannel_sending = -1;
        int number_received = -1;
        int number_sent = -1;
        Date lastmessagetime = new Date();

        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
            do {
                Log.d(TFApp.LOGKEY, String.format("loading conversation, "
                                + "searching ... - current id/val: %d/%s",
                        listCursor.getLong(0), listCursor.getString(3)));
                if (id == listCursor.getLong(0)) {
                    idchannel_receiving = listCursor.getInt(1);
                    idchannel_sending = listCursor.getInt(2);
                    nick = listCursor.getString(3);
                    number_received = listCursor.getInt(4);
                    number_sent = listCursor.getInt(5);
                    long lastmessagetimestamp = listCursor.getLong(6);
                    lastmessagetime.setTime(lastmessagetimestamp);
                    hasConversationID = true;
                }
            } while (!hasConversationID && listCursor.moveToNext());
        }
        listCursor.close();

        // construct Conversation
        Conversation result = null;
        if (hasConversationID) {
            // load channels for conversation
            Channel receiving = null;
            if (-1 != idchannel_receiving) {
                receiving = loadChannel(database, idchannel_receiving);
            }
            if (null == receiving) {
                Log.d(TFApp.LOGKEY, "could not instantiate receiving channel");
            }
            Channel sending = null;
            if (-1 != idchannel_sending) {
                sending = loadChannel(database, idchannel_sending);
            }
            if (null == sending) {
                Log.d(TFApp.LOGKEY, "could not instantiate sending channel");
            }

            // load messages belonging to conversation
            TreeSet<CountedMessage> treesetMessages = new TreeSet<CountedMessage>();
            listCursor = database.query(SqlOpenHelper.TABLE_NAME_MESSAGES,
                    new String[]{SqlOpenHelper.MESSAGES_COLUMN_IDCONVERSATION, SqlOpenHelper.MESSAGES_COLUMN_DATE,
                            SqlOpenHelper.MESSAGES_COLUMN_ISRECEIVED, SqlOpenHelper.MESSAGES_COLUMN_MESSAGENUMBER,
                            SqlOpenHelper.MESSAGES_COLUMN_MESSAGE, SqlOpenHelper.MESSAGES_COLUMN_ISIMAGE,
                            SqlOpenHelper.MESSAGES_COLUMN_IMAGEDATA},
                    null, null, null, null,
                    SqlOpenHelper.MESSAGES_COLUMN_IDCONVERSATION);
            listCursor.moveToFirst();
            if (!listCursor.isAfterLast()) {
                do {
                    if (id == listCursor.getLong(0)) {
                        long datestamp = listCursor.getLong(1);
                        //Log.d(TFApp.LOGKEY, String.format("loading message - datestamp is %d", datestamp));
                        Date messagetime = new Date();
                        messagetime.setTime(datestamp);
                        boolean isReceived = listCursor.getInt(2) == 1;
                        int messagenumber = listCursor.getInt(3);
                        String messagebody = listCursor.getString(4);
                        boolean isImage = listCursor.getInt(5) == 1;
                        byte[] imagedata = null;
                        if (isImage) {
                            imagedata = listCursor.getBlob(6);
                        }
                        CountedMessage cm = null;
                        if (isImage) {
                            cm = new CountedImageMessage(isReceived, messagenumber, -1, imagedata, messagetime);
                        } else {
                            cm = new CountedMessage(isReceived, messagenumber, -1, messagebody, messagetime);
                        }
                        //Log.d(TFApp.LOGKEY, String.format("loaded message %s", cm.toString()));
                        treesetMessages.add(cm);
                        if (messagetime.after(lastmessagetime)) {
                            lastmessagetime = messagetime;
                        }
                    }
                } while (listCursor.moveToNext());
            }
            listCursor.close();

            List<Messagekey> keysforReceiving = loadAllKeysForReceiving(id);
            List<Messagekey> keysforSending = loadAllKeysForSending(id);

            if (null != sending && null != receiving) {
                Log.d(TFApp.LOGKEY, String.format("new conv %s with time %d", nick, lastmessagetime.getTime()));
                result = new Conversation(id, nick, number_received, number_sent,
                        treesetMessages,
                        keysforReceiving,
                        keysforSending,
                        receiving, sending, lastmessagetime);
            } else {
                Log.d(TFApp.LOGKEY, "could not instantiate Conversation");
            }
        }

        return result;
    }


    public int deleteMessagesAndUsedKeys(long conversationID) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        int affectedRows = 0;

        String messageKeysWhereString = SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYUSED + "=1 AND " + SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION + "=?";
        String[] messageKeysWhereArgs = new String[]{String.format("%d", conversationID)};
        affectedRows += db.delete(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS, messageKeysWhereString, messageKeysWhereArgs);

        String messagesWhereString = SqlOpenHelper.MESSAGES_COLUMN_IDCONVERSATION + "=?";
        String[] messagesWhereArgs = new String[]{String.format("%d", conversationID)};
        affectedRows += db.delete(SqlOpenHelper.TABLE_NAME_MESSAGES, messagesWhereString, messagesWhereArgs);

        return affectedRows;
    }


    public int deleteConversation(long conversationID) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        int affectedRows = 0;

        String messageKeysWhereString = SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION + "=?";
        String[] messageKeysWhereArgs = new String[]{String.format("%d", conversationID)};
        affectedRows += db.delete(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS, messageKeysWhereString, messageKeysWhereArgs);

        String messagesWhereString = SqlOpenHelper.MESSAGES_COLUMN_IDCONVERSATION + "=?";
        String[] messagesWhereArgs = new String[]{String.format("%d", conversationID)};
        affectedRows += db.delete(SqlOpenHelper.TABLE_NAME_MESSAGES, messagesWhereString, messagesWhereArgs);

        String conversationWhereString = SqlOpenHelper.CONVERSATIONS_COLUMN_ID + "=?";
        String[] conversationWhereArgs = new String[]{String.format("%d", conversationID)};
        affectedRows += db.delete(SqlOpenHelper.TABLE_NAME_CONVERSATIONS, conversationWhereString, conversationWhereArgs);

        return affectedRows;
    }


    public long addNewConversation(Conversation conversation) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues conversationValues = new ContentValues();
        conversationValues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_IDCHANNEL_RECEIVING, conversation.getChannelForReceiving().id);
        conversationValues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_IDCHANNEL_SENDING, conversation.getChannelForSending().id);
        conversationValues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_NICK, conversation.getNick());
        conversationValues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_NUMBERRECEIVED, conversation.getCurrentNumberOfReceivedMessages());
        conversationValues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_NUMBERSENT, conversation.getCurrentNumberOfSentMessages());
        conversationValues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_LAST_MESSAGE_DATE, 0);
        long id = db.insert(SqlOpenHelper.TABLE_NAME_CONVERSATIONS, null, conversationValues);
        conversation.setID(id);
        return id;
    }


    public long addNewMessage(TFApp app, CountedMessage message) {
        long id = -1;
        try {
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            ContentValues messageValues = new ContentValues();
            messageValues.put(SqlOpenHelper.MESSAGES_COLUMN_IDCONVERSATION, message.getIDConversation());
            messageValues.put(SqlOpenHelper.MESSAGES_COLUMN_DATE, message.getCreated().getTime());
            messageValues.put(SqlOpenHelper.MESSAGES_COLUMN_ISRECEIVED, (message.getIsReceived() ? 1 : 0));
            messageValues.put(SqlOpenHelper.MESSAGES_COLUMN_MESSAGENUMBER, message.getLocalmessagenumber());
            messageValues.put(SqlOpenHelper.MESSAGES_COLUMN_MESSAGE, message.getMessagebody());
            if (message instanceof CountedImageMessage) {
                CountedImageMessage cim = (CountedImageMessage) message;
                messageValues.put(SqlOpenHelper.MESSAGES_COLUMN_ISIMAGE, 1);
                messageValues.put(SqlOpenHelper.MESSAGES_COLUMN_IMAGEDATA, cim.getImagedata());
            }

            id = db.insert(SqlOpenHelper.TABLE_NAME_MESSAGES, null, messageValues);
            message.setID(id);
        } catch (Exception e) {
            app.logException(e);
        }
        return id;
    }


    public long addNewKeyToConversation(TFApp app, Conversation conversation, Messagekey key) {
        long id = -1;
        try {
            if (conversation.addKey(key)) {
                SQLiteDatabase db = dbhelper.getWritableDatabase();
                ContentValues keyValues = new ContentValues();
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION, conversation.getID());
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISFORRECEIVING, (key.getIsForReceiving() ? 1 : 0));
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED, (key.getIsExchanged() ? 1 : 0));
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYUSED, (key.getIsUsed() ? 1 : 0));
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID_SHORTHASH, key.getHeaderIDShortHash());
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID, key.getHeaderID());
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_KEYBODY, key.getKeybody());
                id = db.insert(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS, null, keyValues);
                key.setID(id);
            }
        } catch (Exception e) {
            app.logException(e);
        }
        return id;
    }


    public int bulkAddKeysToConversationAndSetExchanged(Conversation conversation, ArrayList<Messagekey> keys) {
        int count = 0;
        long conv_id = conversation.getID();
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Messagekey key : keys) {
                ContentValues keyValues = new ContentValues();
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION, conv_id);
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISFORRECEIVING, (key.getIsForReceiving() ? 1 : 0));
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED, 1);
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYUSED, (key.getIsUsed() ? 1 : 0));
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID_SHORTHASH, key.getHeaderIDShortHash());
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID, key.getHeaderID());
                keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_KEYBODY, key.getKeybody());
                db.insert(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS, null, keyValues);
                count++;
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return count;
    }


    public int bulkAddEncodedKeysToConversationAndSetExchanged(TFApp app, Conversation conversation, String[] keys) {
        int count = 0;
        long conv_id = conversation.getID();
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String keystring : keys) {
                if (Messagekey.isWebsafeSerializationOfASingleKey(keystring)) {
                    try {
                        Messagekey key = Messagekey.decodeFromWebsafeSerialization(keystring);
                        key.toggleReceivingMode();
                        ContentValues keyValues = new ContentValues();
                        keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_IDCONVERSATION, conv_id);
                        keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISFORRECEIVING, (key.getIsForReceiving() ? 1 : 0));
                        keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED, 1);
                        keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYUSED, (key.getIsUsed() ? 1 : 0));
                        keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID_SHORTHASH, key.getHeaderIDShortHash());
                        keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_HEADERID, key.getHeaderID());
                        keyValues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_KEYBODY, key.getKeybody());
                        db.insert(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS, null, keyValues);
                        count++;
                    } catch (Exception e) {
                         app.logException(e);
                    }
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return count;
    }


    public int updateKey(Messagekey key) {
        Log.d(TFApp.LOGKEY, String.format("updateKey: saving state of isexchanged for key %s", key.toString()));
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues updatableKeyvalues = new ContentValues();
        updatableKeyvalues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED, (key.getIsExchanged() ? 1 : 0));
        updatableKeyvalues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_ISALREADYUSED, (key.getIsUsed() ? 1 : 0));
        updatableKeyvalues.put(SqlOpenHelper.MESSAGEKEYS_COLUMN_KEYBODY, key.getKeybody());
        String whereClause = String.format("%s=%d", SqlOpenHelper.MESSAGEKEYS_COLUMN_ID, key.getID());
        int affectedRows = db.update(SqlOpenHelper.TABLE_NAME_MESSAGEKEYS, updatableKeyvalues, whereClause, null);
        return affectedRows;
    }


    public int updateChannel(Channel channel) {
        Log.d(TFApp.LOGKEY, String.format("updateChannel: saving endpoint for channel %s", channel.toString()));
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues updatableChannelvalues = new ContentValues();
        updatableChannelvalues.put(SqlOpenHelper.CHANNELS_COLUMN_ENDPOINT, channel.endpoint);
        String whereClause = String.format("%s=%d", SqlOpenHelper.CHANNELS_COLUMN_ID, channel.id);
        int affectedRows = db.update(SqlOpenHelper.TABLE_NAME_CHANNELS, updatableChannelvalues, whereClause, null);
        return affectedRows;
    }


    public int updateConversation(Conversation conversation) {
        Log.d(TFApp.LOGKEY, String.format("updateConversation called for conversation with id %d", conversation.getID()));
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues updatableConversationvalues = new ContentValues();
        updatableConversationvalues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_NICK, conversation.getNick());
        updatableConversationvalues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_NUMBERRECEIVED, conversation.getCurrentNumberOfReceivedMessages());
        updatableConversationvalues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_NUMBERSENT, conversation.getCurrentNumberOfSentMessages());
        updatableConversationvalues.put(SqlOpenHelper.CONVERSATIONS_COLUMN_LAST_MESSAGE_DATE, conversation.getLastMessageTime().getTime());
        String whereClause = String.format("%s=%d", SqlOpenHelper.CONVERSATIONS_COLUMN_ID, conversation.getID());
        int affectedRows = db.update(SqlOpenHelper.TABLE_NAME_CONVERSATIONS, updatableConversationvalues, whereClause, null);
        return affectedRows;
    }


//   public int deleteFullCache() {
//      SQLiteDatabase db = dbhelper.getWritableDatabase();
//      String whereString = "";
//      String[] whereArgs = new String[] { };
//        
//      int affectedRows = db.delete(SqlOpenHelper.TABLE_NAME_SECRETTALKCHANNELCACHE_CONTENT, whereString, whereArgs);
//      return affectedRows;
//
//      // TODO: update meta and set current to -1
//   }

    public String[] loadCacheForChannel(int idchannel, int maxSize) {
        if (maxSize <= 0) throw new IllegalArgumentException("Cachesize must be positive!");

        String[] cachecontent = new String[maxSize];

        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_SECRETTALKCHANNELCACHE_CONTENT,
                new String[]{SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_IDCHANNEL,
                        SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY,
                        SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEVALUE},
                String.format("%s=%d", SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_IDCHANNEL, idchannel),
                null, null, null, SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY);

        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
            do {
                int cachekey = listCursor.getInt(1);
                String content = listCursor.getString(2);
                if (cachekey >= 0 && cachekey < maxSize) {
                    //Log.d(TFApp.LOGKEY, String.format("loadCacheForChannel: loading entry %d with content %s",
                    //                                                        cachekey, content));
                    cachecontent[cachekey] = content;
                } else {
                    Log.d(TFApp.LOGKEY, String.format("Warning: cachekey %d out of range, maxsize is %d, omitting value %s!",
                            cachekey, maxSize, content));
                }
            } while (listCursor.moveToNext());
        }
        listCursor.close();

        return cachecontent;
    }


    public boolean hasCacheEntry(int idchannel, int cachekey) {
        Log.d(TFApp.LOGKEY, String.format("hasCacheEntry: checking existence for cache %d and key %d",
                idchannel, cachekey));
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor listCursor = db.query(SqlOpenHelper.TABLE_NAME_SECRETTALKCHANNELCACHE_CONTENT,
                new String[]{SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_IDCHANNEL,
                        SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY,
                        SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEVALUE},
                String.format("%s=%d AND %s=%d",
                        SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_IDCHANNEL, idchannel,
                        SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY, cachekey),
                null, null, null, SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY);
        int count = 0;
        listCursor.moveToFirst();
        if (!listCursor.isAfterLast()) {
            do {
                int foo = listCursor.getInt(1);
                count++;
            } while (listCursor.moveToNext());
        }
        if (count > 1) {
            Log.d(TFApp.LOGKEY, String.format("WARNING: duplicate entries for cache %d and key %d detected!",
                    idchannel, cachekey));
        }
        boolean result = false;
        if (count >= 1) result = true;
        listCursor.close();
        if (result) {
            Log.d(TFApp.LOGKEY, String.format("hasCacheEntry: cache %d and key %d is existing.",
                    idchannel, cachekey));
        } else {
            Log.d(TFApp.LOGKEY, String.format("hasCacheEntry: cache %d and key %d is not existing.",
                    idchannel, cachekey));
        }
        return result;
    }


    /**
     * method updates or creates appropriate cacheentry
     */
    public boolean setCacheForCacheMetaIDAndKey(int idchannel, int cachekey, String newValue) {
        Log.d(TFApp.LOGKEY, String.format("setCacheForCacheMetaIDAndKey: setting cacheval for cache %d and key %d to %s",
                idchannel, cachekey, newValue));
        boolean isNewEntry = !hasCacheEntry(idchannel, cachekey);

        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues cacheValues = new ContentValues();
        long result = -1;
        if (isNewEntry) {
            cacheValues.put(SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_IDCHANNEL, idchannel);
            cacheValues.put(SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY, cachekey);
            cacheValues.put(SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEVALUE, newValue);
            result = db.insert(SqlOpenHelper.TABLE_NAME_SECRETTALKCHANNELCACHE_CONTENT, null, cacheValues);
        } else {
            cacheValues.put(SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEVALUE, newValue);

            String whereClause = String.format("%s=%d AND %s=%d",
                    SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_IDCHANNEL, idchannel,
                    SqlOpenHelper.SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY, cachekey);
            result = db.update(SqlOpenHelper.TABLE_NAME_SECRETTALKCHANNELCACHE_CONTENT, cacheValues, whereClause, null);
        }
        return (result > 0);
    }


    public int getCurrentOffsetForChannel(int idchannel) {
        Log.d(TFApp.LOGKEY, String.format("getCurrentOffsetForChannel: channel %d", idchannel));

        SQLiteDatabase db = dbhelper.getReadableDatabase();

        if (!db.isDatabaseIntegrityOk()) {
            Log.d(TFApp.LOGKEY, String.format("ERROR: db integrity check failed!"));
            return -1;
        }

        Cursor listCursor = db.query(SqlOpenHelper.TABLE_NAME_SECRETTALKCHANNELCACHE_META,
                new String[]{SqlOpenHelper.SECRETTALKCHANNELCACHE_META_COLUMN_CURRENTOFFSET},
                String.format("%s=%d", SqlOpenHelper.SECRETTALKCHANNELCACHE_META_COLUMN_IDCHANNEL, idchannel),
                null, null, null, null);

        int currentOffset = -1;
        if (listCursor != null && listCursor.moveToFirst()) {
            if (listCursor.getCount() != 1) {
                Log.d(TFApp.LOGKEY, String.format("ERROR: amount of rows is %d", listCursor.getCount()));
                return -1;
            }
            if (listCursor.getColumnCount() != 1) {
                Log.d(TFApp.LOGKEY, String.format("ERROR: amount of columns is %d", listCursor.getColumnCount()));
                return -1;
            }
            int colind = listCursor.getColumnIndex(SqlOpenHelper.SECRETTALKCHANNELCACHE_META_COLUMN_CURRENTOFFSET);
            currentOffset = listCursor.getInt(colind);

            listCursor.close();
        }
        //Log.d(TFApp.LOGKEY, String.format("current offset is %d.", currentOffset));
        return currentOffset;
    }


    public boolean setCurrentOffsetForChannel(int idchannel, int newOffset) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues cacheMetaValues = new ContentValues();
        cacheMetaValues.put(SqlOpenHelper.SECRETTALKCHANNELCACHE_META_COLUMN_CURRENTOFFSET, newOffset);

        String whereClause = String.format("%s=%d", SqlOpenHelper.SECRETTALKCHANNELCACHE_META_COLUMN_IDCHANNEL, idchannel);
        int result = db.update(SqlOpenHelper.TABLE_NAME_SECRETTALKCHANNELCACHE_META, cacheMetaValues, whereClause, null);
        return (result > 0);
    }


    public boolean isDuringUpdate(long maxdeltaInMillis) {
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor listCursor = db.query(SqlOpenHelper.TABLE_NAME_UPDATELOCK,
                new String[]{SqlOpenHelper.UPDATELOCK_COLUMN_ID, SqlOpenHelper.UPDATELOCK_COLUMN_LASTLOCKTIME},
                null, null, null, null,
                SqlOpenHelper.UPDATELOCK_COLUMN_ID);
        List<Channel> channels = new ArrayList<Channel>();

        listCursor.moveToFirst();

        long lasttimestamp = 0;
        if (!listCursor.isAfterLast()) {
            lasttimestamp = listCursor.getLong(1);
        }
        listCursor.close();

        long currenttimestamp = new java.util.Date().getTime();

        return ((0 != lasttimestamp) && (currenttimestamp - lasttimestamp < maxdeltaInMillis));
    }


    public boolean setDuringUpdate(boolean setLock) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        ContentValues updatelockValues = new ContentValues();
        if (setLock) {
            updatelockValues.put(SqlOpenHelper.UPDATELOCK_COLUMN_LASTLOCKTIME, new java.util.Date().getTime());
        } else {
            updatelockValues.put(SqlOpenHelper.UPDATELOCK_COLUMN_LASTLOCKTIME, 0);
        }

        int result = db.update(SqlOpenHelper.TABLE_NAME_UPDATELOCK, updatelockValues, null, null);
        return (result > 0);
    }


    public long appendLogMessage(String message, int loglevel) {
       deleteOldLogEntries();
        long id = -1;
        try {
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            ContentValues logValues = new ContentValues();
            logValues.put(SqlOpenHelper.LOG_COLUMN_LOGLEVEL, loglevel);
            logValues.put(SqlOpenHelper.LOG_COLUMN_MESSAGE, message);
            logValues.put(SqlOpenHelper.LOG_COLUMN_TIMEINSERTED, new java.util.Date().getTime());
            id = db.insert(SqlOpenHelper.TABLE_NAME_LOG, null, logValues);
        } catch (Exception e) { }
        return id;
    }

   public String getFullLog() {
      StringBuilder sb = new StringBuilder();
      SQLiteDatabase database = dbhelper.getReadableDatabase();

      Cursor listCursor = database.query(SqlOpenHelper.TABLE_NAME_LOG,
              new String[]{SqlOpenHelper.LOG_COLUMN_TIMEINSERTED, SqlOpenHelper.LOG_COLUMN_LOGLEVEL, SqlOpenHelper.LOG_COLUMN_MESSAGE},
              null, null, null, null,
              SqlOpenHelper.LOG_COLUMN_ID);

      DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      listCursor.moveToFirst();
      if (!listCursor.isAfterLast()) {
         do {
            Date inserted = new Date();
            inserted.setTime(listCursor.getLong(0));
            sb.append(df.format(inserted));
            sb.append(" [");
            sb.append(String.format("%d", listCursor.getInt(1)));
            sb.append("] ");
            sb.append(listCursor.getString(2));
            sb.append("\n");
         } while (listCursor.moveToNext());
      }
      listCursor.close();

      return sb.toString();
   }

   // keep only one day as log information
   public int deleteOldLogEntries() {
      SQLiteDatabase db = dbhelper.getWritableDatabase();
      String whereString = SqlOpenHelper.LOG_COLUMN_TIMEINSERTED + "<?";
      Date oldestLogTime = new Date(System.currentTimeMillis()-24*60*60*1000);
      String[] whereArgs = new String[]{String.format("%d", oldestLogTime.getTime())};

      int affectedRows = db.delete(SqlOpenHelper.TABLE_NAME_LOG, whereString, whereArgs);
      return affectedRows;
   }

}
