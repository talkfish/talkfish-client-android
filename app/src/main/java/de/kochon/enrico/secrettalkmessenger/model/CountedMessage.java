package de.kochon.enrico.secrettalkmessenger.model;

import java.util.Calendar;
import java.util.Date;

public class CountedMessage implements Comparable<CountedMessage> {

   public static final int NOTADDEDNUMBER = -1;

	// maximum amount of characters for a message, intended as upper limit for prevention of buffer overflow
	// the usual messagesize should be far below this border
	public final int MAXLENGTH = 20480; 
	
	private long id;
	private long idconversation;
	private int localmessagenumber;
	private int transmittedmessagenumber;

	private final Date created;
	private final boolean isReceived; // direction of sending: false = message was sent to contact
	private final String messagebody;
	
	public CountedMessage(boolean isReceived, int localmessagenumber, int transmittedmessagenumber, String messagebody, Date localtime) {
		if (messagebody == null) throw new IllegalArgumentException("Messagebody has to be not null!");
		if (messagebody.length() > MAXLENGTH) throw new IllegalArgumentException("Messagesize exceeds internal limit!");
		if (localtime == null) throw new IllegalArgumentException("Localtime has to be not null!");
		Calendar currentCal = Calendar.getInstance();
		currentCal.setTime(localtime);
		if (currentCal.get(Calendar.YEAR)<2015) { 
         throw new IllegalArgumentException(String.format("Plausibilitycheck: current year is %d, but it has to be greater or equal 2015!", 
                                                            currentCal.get(Calendar.YEAR)));
      }
		
		this.created = localtime;
		this.isReceived = isReceived;
		this.localmessagenumber = localmessagenumber;
		this.transmittedmessagenumber = transmittedmessagenumber;
		this.messagebody = messagebody;
	}
	
   public long getID() {return id;}
   public void setID(long id) {this.id = id;}

   public long getIDConversation() {return idconversation;}
   public void setIDConversation(long idconversation) {this.idconversation = idconversation;}

	public Date getCreated() { return created; }

	public int getLocalmessagenumber() { return localmessagenumber; }
   public void setLocalmessagenumber(int localmessagenumber) { this.localmessagenumber = localmessagenumber; }

	public int getTransmittedmessagenumber() { return transmittedmessagenumber; }
   public void setTransmittedmessagenumber(int transmittedmessagenumber) { this.transmittedmessagenumber = transmittedmessagenumber; }

	public boolean getIsReceived() { return isReceived; }
	public String getMessagebody() { return messagebody; }

	
	public int compareTo(final CountedMessage otherCountedMessage) {

		int ret = getCreated().compareTo(otherCountedMessage.getCreated());
		
		if (0 == ret) {
			if (getIsReceived() && !otherCountedMessage.getIsReceived()) {
				ret = -1;
			}
			if (!getIsReceived() && otherCountedMessage.getIsReceived()) {
				ret = 1;
			}
			if (getIsReceived() == otherCountedMessage.getIsReceived()) {
				if (getLocalmessagenumber() < otherCountedMessage.getLocalmessagenumber()) {
					ret = -1;
				}
				if (getLocalmessagenumber() > otherCountedMessage.getLocalmessagenumber()) {
					ret = 1;
				}
				if (getLocalmessagenumber() == otherCountedMessage.getLocalmessagenumber()) {
					ret = getMessagebody().compareTo(otherCountedMessage.getMessagebody());
				}
			}
		}
		
		return ret;
	}

	
	@Override
	/**
	 * standardimplementation based on compareTo 
	 * taken from 
	 * Michael Inden, "Der Weg zum Java Profi", 2.aktualisierte und erweiterte Auflage, Seite 291
	 **/
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CountedMessage otherCountedMessage = (CountedMessage) obj;
		return (0 == compareTo(otherCountedMessage));
	}

	
	@Override
	/**
	 * ideas taken from
	 * Michael Inden, "Der Weg zum Java Profi", 2.aktualisierte und erweiterte Auflage, Seite 283ff
	 */
	public int hashCode() {
		final int basePrime = 43;
		return basePrime * basePrime * basePrime * (getIsReceived() ? 1: 2)
				+ basePrime * basePrime * getMessagebody().hashCode()
				+ basePrime * ((int) getLocalmessagenumber())
				+ getCreated().hashCode();
	}

	
	@Override
	public String toString() {
		Calendar currentCal = Calendar.getInstance();
		currentCal.setTime(getCreated());
		int year = currentCal.get(Calendar.YEAR);
		int month = 1 + currentCal.get(Calendar.MONTH);
		int day = currentCal.get(Calendar.DAY_OF_MONTH);
		int hour = currentCal.get(Calendar.HOUR_OF_DAY);
		int minute = currentCal.get(Calendar.MINUTE);
		int second = currentCal.get(Calendar.SECOND);
		
		return String.format("(%s#%d/%d %04d/%02d/%02d %02d:%02d:%02d) %s", 
				(getIsReceived()?"i":"o"), getLocalmessagenumber(), getTransmittedmessagenumber(),
				year, month, day,
				hour, minute, second,
				getMessagebody() );
	}
	
}
