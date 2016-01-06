package de.kochon.enrico.secrettalkmessenger.backend;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;

public class SqlOpenHelper extends SQLiteOpenHelper {
	
	public static final String DBNAME = "messagedb.sqlite";
	public static final int VERSION = 5;

	public static final String TABLE_NAME_CONFIG = "config";
	public static final String CONFIG_COLUMN_ID = "id";
	public static final String CONFIG_COLUMN_KEY = "key";
	public static final String CONFIG_COLUMN_VALUE = "value";
	
	public static final String TABLE_NAME_CHANNELS = "channels";
	public static final String CHANNELS_COLUMN_ID = "id";
	public static final String CHANNELS_COLUMN_NAME = "name";
	public static final String CHANNELS_COLUMN_PROTOCOL = "protocol";
	public static final String CHANNELS_COLUMN_ENDPOINT = "endpoint";
	public static final String CHANNELS_COLUMN_ISFORRECEIVING = "isforreceiving";

	public static final String TABLE_NAME_CONVERSATIONS = "conversations";
	public static final String CONVERSATIONS_COLUMN_ID = "id";
	public static final String CONVERSATIONS_COLUMN_IDCHANNEL_RECEIVING = "idchannelreceiving";
	public static final String CONVERSATIONS_COLUMN_IDCHANNEL_SENDING = "idchannelsending";
	public static final String CONVERSATIONS_COLUMN_NICK = "nick";
	public static final String CONVERSATIONS_COLUMN_NUMBERRECEIVED = "received";
	public static final String CONVERSATIONS_COLUMN_NUMBERSENT = "sent";

	public static final String TABLE_NAME_MESSAGES = "messages";
	public static final String MESSAGES_COLUMN_ID = "id";
	public static final String MESSAGES_COLUMN_IDCONVERSATION = "idconversation";
	public static final String MESSAGES_COLUMN_DATE = "date";
	public static final String MESSAGES_COLUMN_ISRECEIVED = "isreceived";
	public static final String MESSAGES_COLUMN_MESSAGENUMBER = "messagenumber";
	public static final String MESSAGES_COLUMN_MESSAGE = "message";
	
	public static final String TABLE_NAME_MESSAGEKEYS = "messagekeys";
	public static final String MESSAGEKEYS_COLUMN_ID = "id";
	public static final String MESSAGEKEYS_COLUMN_IDCONVERSATION = "idconversation";
	public static final String MESSAGEKEYS_COLUMN_ISFORRECEIVING = "isforreceiving";
	public static final String MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED = "isalreadyexchanged";
	public static final String MESSAGEKEYS_COLUMN_ISALREADYUSED = "isalreadyused";
	public static final String MESSAGEKEYS_COLUMN_HEADERID_SHORTHASH = "headerid_shorthash";
	public static final String MESSAGEKEYS_COLUMN_HEADERID = "headerid";
	public static final String MESSAGEKEYS_COLUMN_KEYBODY = "keybody";

   public static final String TABLE_NAME_SECRETTALKCHANNELCACHE_META = "secrettalkchannelcachemeta";
   public static final String SECRETTALKCHANNELCACHE_META_COLUMN_ID = "id";
   public static final String SECRETTALKCHANNELCACHE_META_COLUMN_IDCHANNEL = "idchannel";
   public static final String SECRETTALKCHANNELCACHE_META_COLUMN_CURRENTOFFSET = "currentoffset";
	
   public static final String TABLE_NAME_SECRETTALKCHANNELCACHE_CONTENT = "secrettalkchannelcachecontent";
   public static final String SECRETTALKCHANNELCACHE_CONTENT_COLUMN_ID = "id";
   public static final String SECRETTALKCHANNELCACHE_CONTENT_COLUMN_IDCHANNEL = "idchannel";
   public static final String SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY = "cachekey";
   public static final String SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEVALUE = "cachevalue";
	
   public static final String TABLE_NAME_UPDATELOCK = "updatelock";
   public static final String UPDATELOCK_COLUMN_ID = "id";
   public static final String UPDATELOCK_COLUMN_LASTLOCKTIME = "lastlocktime";

	private boolean lockedDB;
	
	public SqlOpenHelper(Context context) {
		super(context, DBNAME, null, VERSION);
		lockedDB = false;
	}
	
	public boolean getLock() {
		if (!lockedDB) {
			lockedDB = true;
			return true;
		} else {
			return false;
		}
	}
	
	public void releaseLock() {
		lockedDB = false;
	}
	
	
	// -- scheme definition

   private void createStructure(SQLiteDatabase db) {
		db.execSQL("create table " + TABLE_NAME_CONFIG+ "(" +
				CONFIG_COLUMN_ID + " integer primary key autoincrement not null, " +
				CONFIG_COLUMN_KEY + " text," +
				CONFIG_COLUMN_VALUE + " text" +
		");");
		db.execSQL("create table " + TABLE_NAME_CHANNELS + "(" +
				CHANNELS_COLUMN_ID + " integer primary key autoincrement not null, " +
				CHANNELS_COLUMN_NAME + " string not null, " +
				CHANNELS_COLUMN_PROTOCOL + " string not null, " +
				CHANNELS_COLUMN_ENDPOINT + " string not null," +
				CHANNELS_COLUMN_ISFORRECEIVING + " integer not null" +
		");");
		db.execSQL("create table " + TABLE_NAME_CONVERSATIONS + "(" +
				CONVERSATIONS_COLUMN_ID + " integer primary key autoincrement not null, " +
            CONVERSATIONS_COLUMN_IDCHANNEL_RECEIVING + " integer not null, " +
            CONVERSATIONS_COLUMN_IDCHANNEL_SENDING + " integer not null, " +
				CONVERSATIONS_COLUMN_NICK + " text not null, " +
            CONVERSATIONS_COLUMN_NUMBERRECEIVED  + " integer not null, " +
            CONVERSATIONS_COLUMN_NUMBERSENT   + " integer not null " +
		");");
		db.execSQL("create table " + TABLE_NAME_MESSAGES + "(" +
				MESSAGES_COLUMN_ID + " integer primary key autoincrement not null, " +
				MESSAGES_COLUMN_IDCONVERSATION + " integer not null, " +
				MESSAGES_COLUMN_DATE + " integer not null, " +
				MESSAGES_COLUMN_ISRECEIVED + " integer not null, " +
				MESSAGES_COLUMN_MESSAGENUMBER + " integer not null, " +
				MESSAGES_COLUMN_MESSAGE + " text" +
		");");
		db.execSQL("create table " + TABLE_NAME_MESSAGEKEYS + "(" +
				MESSAGEKEYS_COLUMN_ID + " integer primary key autoincrement not null, " +
				MESSAGEKEYS_COLUMN_IDCONVERSATION + " integer not null, " +
				MESSAGEKEYS_COLUMN_ISFORRECEIVING + " integer not null, " +
				MESSAGEKEYS_COLUMN_ISALREADYEXCHANGED + " integer not null, " +
				MESSAGEKEYS_COLUMN_ISALREADYUSED + " integer not null, " +
				MESSAGEKEYS_COLUMN_HEADERID_SHORTHASH + " integer not null, " +
				MESSAGEKEYS_COLUMN_HEADERID + " blob not null, " +
				MESSAGEKEYS_COLUMN_KEYBODY + " blob not null" +
		");");
		db.execSQL("create table " + TABLE_NAME_SECRETTALKCHANNELCACHE_META  + "(" +
				SECRETTALKCHANNELCACHE_META_COLUMN_ID + " integer primary key autoincrement not null, " +
				SECRETTALKCHANNELCACHE_META_COLUMN_IDCHANNEL  + " integer not null, " +
				SECRETTALKCHANNELCACHE_META_COLUMN_CURRENTOFFSET + " integer not null" +
      ");");
		db.execSQL("create table " + TABLE_NAME_SECRETTALKCHANNELCACHE_CONTENT  + "(" +
				SECRETTALKCHANNELCACHE_CONTENT_COLUMN_ID + " integer primary key autoincrement not null, " +
				SECRETTALKCHANNELCACHE_CONTENT_COLUMN_IDCHANNEL  + " integer not null, " +
				SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEKEY + " integer not null, " +
				SECRETTALKCHANNELCACHE_CONTENT_COLUMN_CACHEVALUE + " string not null" +
      ");");
		db.execSQL("create table " + TABLE_NAME_UPDATELOCK  + "(" +
				UPDATELOCK_COLUMN_ID  + " integer primary key autoincrement not null, " +
				UPDATELOCK_COLUMN_LASTLOCKTIME + " integer not null" +
      ");");
   }
	

   private void initializeData(SQLiteDatabase db) {
      // create default channel for directions receiving and sending
      final String DEFAULTENDPOINT = "http://fishnode1.de/prod";

      Log.d(SecretTalkMessengerApplication.LOGKEY, "initializing data, creating default channels");
      ContentValues channelValuesDefaultReceive = new ContentValues();
      channelValuesDefaultReceive.put(SqlOpenHelper.CHANNELS_COLUMN_ID, 1);
      channelValuesDefaultReceive.put(SqlOpenHelper.CHANNELS_COLUMN_NAME, "default");
      channelValuesDefaultReceive.put(SqlOpenHelper.CHANNELS_COLUMN_PROTOCOL, "secrettalk");
      channelValuesDefaultReceive.put(SqlOpenHelper.CHANNELS_COLUMN_ENDPOINT, DEFAULTENDPOINT);
      channelValuesDefaultReceive.put(SqlOpenHelper.CHANNELS_COLUMN_ISFORRECEIVING, 1);
      db.insert(SqlOpenHelper.TABLE_NAME_CHANNELS, null, channelValuesDefaultReceive);
      ContentValues channelValuesDefaultSend = new ContentValues();
      channelValuesDefaultSend.put(SqlOpenHelper.CHANNELS_COLUMN_ID, 2);
      channelValuesDefaultSend.put(SqlOpenHelper.CHANNELS_COLUMN_NAME, "default");
      channelValuesDefaultSend.put(SqlOpenHelper.CHANNELS_COLUMN_PROTOCOL, "secrettalk");
      channelValuesDefaultSend.put(SqlOpenHelper.CHANNELS_COLUMN_ENDPOINT, DEFAULTENDPOINT);
      channelValuesDefaultSend.put(SqlOpenHelper.CHANNELS_COLUMN_ISFORRECEIVING, 0);
      db.insert(SqlOpenHelper.TABLE_NAME_CHANNELS, null, channelValuesDefaultSend);

      ContentValues cacheMetaDefault = new ContentValues();
      cacheMetaDefault.put(SqlOpenHelper.SECRETTALKCHANNELCACHE_META_COLUMN_IDCHANNEL, 1);
      cacheMetaDefault.put(SqlOpenHelper.SECRETTALKCHANNELCACHE_META_COLUMN_CURRENTOFFSET, -1);
      db.insert(SqlOpenHelper.TABLE_NAME_SECRETTALKCHANNELCACHE_META, null, cacheMetaDefault);  

      ContentValues updateLockDefault = new ContentValues();
      updateLockDefault.put(SqlOpenHelper.UPDATELOCK_COLUMN_LASTLOCKTIME, 0);
      db.insert(SqlOpenHelper.TABLE_NAME_UPDATELOCK, null, updateLockDefault);  
   }

	@Override
	public void onCreate(SQLiteDatabase db) {
      createStructure(db);
      initializeData(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ((newVersion > oldVersion) && (oldVersion == 4)) {
         db.execSQL("create table " + TABLE_NAME_UPDATELOCK  + "(" +
               UPDATELOCK_COLUMN_ID  + " integer primary key autoincrement not null, " +
               UPDATELOCK_COLUMN_LASTLOCKTIME + " integer not null" +
         ");");
         ContentValues updateLockDefault = new ContentValues();
         updateLockDefault.put(SqlOpenHelper.UPDATELOCK_COLUMN_LASTLOCKTIME, 0);
         db.insert(SqlOpenHelper.TABLE_NAME_UPDATELOCK, null, updateLockDefault);  
      }
		if ((newVersion > oldVersion) && (oldVersion < 4)) {
			db.execSQL("drop table " + TABLE_NAME_MESSAGES + ";");
			db.execSQL("drop table " + TABLE_NAME_CONFIG + ";");
			db.execSQL("drop table " + TABLE_NAME_CHANNELS + ";");
			db.execSQL("drop table " + TABLE_NAME_CONVERSATIONS  + ";");
			db.execSQL("drop table " + TABLE_NAME_MESSAGEKEYS + ";");
         if (oldVersion == 3) {
            db.execSQL("drop table " + TABLE_NAME_SECRETTALKCHANNELCACHE_META + ";");
            db.execSQL("drop table " + TABLE_NAME_SECRETTALKCHANNELCACHE_CONTENT + ";");
         }
         createStructure(db);
         initializeData(db);
		}
	}
	
}
