package de.kochon.enrico.secrettalkmessenger.model;

import java.util.Date;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

public class Conversation {
	
   private long id;
	private String nick;
	
	// splitting of messagenumbers necessary, due to asynchronous communication
	private int currentNumberOfReceivedMessages;
	private int currentNumberOfSentMessages;
	
	private TreeSet<CountedMessage> messages;

   private List<Messagekey> keysForSending; 
   private List<Messagekey> keysForReceiving; 

   private Channel channelForReceiving;
   private Channel channelForSending;

   private boolean hasNewMessages;


   /**
    * minimum constructor
    */
	public Conversation(String nick, Channel channelForReceiving, Channel channelForSending) {
      this.id = -1;
		this.nick = nick;
		this.currentNumberOfReceivedMessages = 0;
		this.currentNumberOfSentMessages = 0;
      this.keysForSending = new ArrayList<Messagekey>();
      this.keysForReceiving = new ArrayList<Messagekey>();
		this.messages = new TreeSet<CountedMessage>();
      this.channelForReceiving = channelForReceiving;
      this.channelForSending = channelForSending;
      this.hasNewMessages = false;
	}
	

   /**
    * constructor intended for loading a conversation from db
    */
	public Conversation(long id, String nick, int received, int sent, 
                       TreeSet<CountedMessage> messages,
                       List<Messagekey> keysForReceiving,
                       List<Messagekey> keysForSending,
                       Channel channelForReceiving, 
                       Channel channelForSending) {
      this.id = id;
		this.nick = nick;
		this.currentNumberOfReceivedMessages = received;
		this.currentNumberOfSentMessages = sent;
		this.messages = messages;
      this.keysForReceiving = keysForReceiving;
      this.keysForSending = keysForSending;
      this.channelForReceiving = channelForReceiving;
      this.channelForSending = channelForSending;
      this.hasNewMessages = false;
	}
	
	public long getID() { return id; }
   public void setID(long id) { this.id = id; }

	public String getNick() { return nick; }
	public void setNick(String newNick) { nick = newNick; }

	public Channel getChannelForReceiving() { return channelForReceiving; }
	public Channel getChannelForSending() { return channelForSending; }
	
	public int getCurrentNumberOfReceivedMessages() { return currentNumberOfReceivedMessages; }

	public int getCurrentNumberOfSentMessages() { return currentNumberOfSentMessages; }
	
   public List<Messagekey> getReceivingKeys() { return keysForReceiving; }

	public TreeSet<CountedMessage> getMessages() {
      return messages;
   }

   public void setHasNewMessages(boolean state) {
      this.hasNewMessages = state;
   }


   public boolean addKey(Messagekey key) {
      if (key.getIsForReceiving()) {
         for(Messagekey k: this.keysForReceiving) {
            if (k.equals(key)) {
               return false;
            }
         }
         this.keysForReceiving.add(key);
      } else {
         for(Messagekey k: this.keysForSending) {
            if (k.equals(key)) {
               return false;
            }
         }
         this.keysForSending.add(key);
      }
      return true;
   }

   public int getSendKeyAmount() {
      return this.keysForSending.size();
   }

   public boolean hasAtLeastOneKeyForSending() {
      for (Messagekey k: this.keysForSending) {
         if (!k.getIsUsed() && k.getIsExchanged()) {
            return true;
         }
      }
      return false;
   }

   public int countActiveSendKeys() {
      int count = 0;
      for (Messagekey k: this.keysForSending) {
         if (!k.getIsUsed() && k.getIsExchanged()) {
            count++;
         }
      }
      return count;
   }

   public int countActiveReceiveKeys() {
      int count = 0;
      for (Messagekey k: this.keysForReceiving) {
         if (!k.getIsUsed() && k.getIsExchanged()) {
            count++;
         }
      }
      return count;
   }

   public int getReceiveKeyAmount() {
      return this.keysForReceiving.size();
   }

   public Messagekey getKeyForSending() {
      for (Messagekey k: this.keysForSending) {
         if (!k.getIsUsed() && k.getIsExchanged()) {
            return k;
         }
      }
      return null;
   }
	

	// safe adding of message in correct order
	// mechanism in place which keeps track of numbering and ensures uniqueness of messagenumbers
	public void addMessage(CountedMessage cm) throws IllegalArgumentException {
		if (cm == null) throw new IllegalArgumentException("Message has to be not null!");
		
		if (cm.getIsReceived()) {
			if (cm.getLocalmessagenumber() != this.currentNumberOfReceivedMessages+1) throw new IllegalArgumentException("Internal Messagenumber does not fit!");
			this.currentNumberOfReceivedMessages++;
		} else {
			if (cm.getLocalmessagenumber() != this.currentNumberOfSentMessages+1) throw new IllegalArgumentException("Internal Messagenumber does not fit!");
			this.currentNumberOfSentMessages++;
		}
		this.messages.add(cm);
	}


	public CountedMessage addSentMessage(String messagebody) {
		return this.addSentMessage(messagebody, new Date());
	}


	public CountedMessage addSentMessage(String messagebody, Date localtime) {
      CountedMessage cm = new CountedMessage(false, this.currentNumberOfSentMessages, -1, messagebody, localtime); 
		return addSentMessage(cm);
	}


   public CountedMessage addSentMessage(CountedMessage cm) {
		this.currentNumberOfSentMessages++;
      cm.setLocalmessagenumber(this.currentNumberOfSentMessages);
      cm.setIDConversation(id);
		this.messages.add(cm);
      return cm;
   }


	public CountedMessage addReceivedMessage(String messagebody) {
		return this.addReceivedMessage(messagebody, new Date());
	}

	
	public CountedMessage addReceivedMessage(String messagebody, Date localtime) {
		this.currentNumberOfReceivedMessages++;
      CountedMessage cm = new CountedMessage(true, this.currentNumberOfReceivedMessages, -1, messagebody, localtime);
		this.messages.add(cm);
      cm.setIDConversation(id);
		return cm;
	}


   public CountedMessage addReceivedMessage(CountedMessage cm) {
	   if (cm.getLocalmessagenumber() != CountedMessage.NOTADDEDNUMBER) throw new IllegalArgumentException("Message may not be added twice!");
		this.currentNumberOfReceivedMessages++;
      cm.setLocalmessagenumber(this.currentNumberOfReceivedMessages);
      cm.setIDConversation(id);
      this.messages.add(cm);
      return cm;
   }
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
      // sb.append("conversation with ");
      // sb.append(this.getNick());
      // sb.append("{ ");
      // sb.append(channelForReceiving.toString());
      // sb.append("/ ");
      // sb.append(channelForSending.toString());
      // sb.append(" }");
		// for(CountedMessage m: this.messages) {
		// 	if (m.getIsReceived()) {
		// 		sb.append(this.getNick());
		// 		sb.append(" -> Me: ");
		// 	} else {
		// 		sb.append("Me -> ");
		// 		sb.append(this.getNick());
		// 		sb.append(": ");
		// 	}
		// 	sb.append(m.toString());
		// 	sb.append("\n");
		// }
      sb.append(this.getNick());
      sb.append(String.format("%s (Schlüssel zum Senden:%d/zum Empfangen:%d)", 
            this.hasNewMessages?" Neue Nachrichten!":"",
            this.countActiveSendKeys(), this.countActiveReceiveKeys())); 
		
		return sb.toString();
	}

}
