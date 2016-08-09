package de.kochon.enrico.secrettalkmessenger.model;

import java.security.SecureRandom;

import de.kochon.enrico.secrettalkmessenger.tools.ByteToStringEncoding;

public class Messagekey {
	

	public final static int HEADERID_LENGTH = 16;
	public final static int KEYBODY_LENGTH = 64;
   public final static int VERSION_0_ENCODED_KEYLENGTH = 164;

   private final int headerid_shorthash;

   private long id;
	private final byte[] headerid;
	private final byte[] keybody;
	private boolean isForReceiving;
	private boolean isExchanged;
	private boolean isUsed;


   private int createHeaderIDShortHash() {
      int result = 0;
      final int baseprime = 17;
      int currentbase = baseprime;
      if (headerid!=null) {
         for(int i=0; i<HEADERID_LENGTH; i++) {
            result += currentbase*(0xff & (int)this.headerid[i]);
            currentbase = currentbase*baseprime;
         }
      }
      return result;
   }


   public Messagekey(boolean isForReceiving) {
      this.id = -1;
		this.headerid = new byte[HEADERID_LENGTH];
		this.keybody = new byte[KEYBODY_LENGTH];
      SecureRandom bytegenHeader = new SecureRandom();
      bytegenHeader.nextBytes(this.headerid);
      SecureRandom bytegenBody = new SecureRandom();
      bytegenBody.nextBytes(this.keybody);
      this.headerid_shorthash = createHeaderIDShortHash();
		this.isForReceiving = isForReceiving;
		this.isExchanged = false;
		this.isUsed = false;
   }


	public Messagekey(long id, byte[] headerid, byte[] keybody, boolean isForReceiving, boolean isExchanged, boolean isUsed) {
		if (headerid.length != HEADERID_LENGTH) 
         throw new IllegalArgumentException(String.format("Size of HEADERID does not fit, expected %d, but got %d chars!", HEADERID_LENGTH, headerid.length)); 
		if (keybody.length != KEYBODY_LENGTH) 
         throw new IllegalArgumentException(String.format("Size of KEYBODY does not fit, expected %d, but got %d chars!", KEYBODY_LENGTH, keybody.length)); 
      this.id = id;
		this.headerid = new byte[HEADERID_LENGTH];
		this.keybody = new byte[KEYBODY_LENGTH];
		for(int i=0; i<HEADERID_LENGTH; i++) {
			this.headerid[i] = headerid[i];
		}
      this.headerid_shorthash = createHeaderIDShortHash();

		for(int i=0; i<KEYBODY_LENGTH; i++) {
			this.keybody[i] = keybody[i];
		}
		
		this.isForReceiving = isForReceiving;
		this.isExchanged = isExchanged;
		this.isUsed = isUsed;
	}

	public long getID() { return id; }
   public void setID(long id) { this.id = id; }



   /**
     * messagekey has a fixed header and a fixed size string encoded binary part
     * current version 0 is exactly 164 chars long
     * the employed encoding algorithm is very simple:
     * split every byte into two halfbytes and encode them into the first 16 letters
     * not as efficent like the uuencode-algorithm, but sufficient for many purposes
     * result is a charrange from a=97 till including p=112
     * structure of serialized key:
     * "k0rabhgfm.....dfdfzbha...dfhfdabjkop"
     * char 0: fixed "k" as classification for key
     * char 1: version currently 0
     * char 2: "r" or "s" for Receive or Send
     * char 3-34: encoded-keyid
     * char 35: "z" static and only for better readability
     * char 36-163: encoded-keybody
     * @return a full serialization of entire Messagekey in String format
     */
   public String getWebsafeSerialization() {
		StringBuffer sb = new StringBuffer();
      sb.append("k0");
      if (isForReceiving) {
         sb.append("r");
      } else {
         sb.append("s");
      }
      sb.append(ByteToStringEncoding.encodeToSmallLetters(headerid));
      sb.append("z");
      sb.append(ByteToStringEncoding.encodeToSmallLetters(keybody));
      String serializedKey = sb.toString();
		if (serializedKey.length() != VERSION_0_ENCODED_KEYLENGTH) throw new IllegalArgumentException("Size of serialized key does not fit!");
      return serializedKey;
   }


   /**
     * counterpart to method getWebsafeSerialization()
     */
   public static Messagekey decodeFromWebsafeSerialization(String serializedKey) {
		if (serializedKey.length() != VERSION_0_ENCODED_KEYLENGTH) throw new IllegalArgumentException("Size of serialized key does not fit!");
      if (serializedKey.charAt(1) != '0') throw new IllegalArgumentException("Error: key version mismatch!");
      boolean isForReceiving = (serializedKey.charAt(2) == 'r');
      byte[] keyid_bytes = ByteToStringEncoding.unencodeFromSmallLetters(serializedKey.substring(3, 35));
      byte[] keybody_bytes = ByteToStringEncoding.unencodeFromSmallLetters(serializedKey.substring(36, 164));

	   return new Messagekey(-1, keyid_bytes, keybody_bytes, isForReceiving, false, false);
   }


   /**
     * precheck for method getWebsafeSerialization()
     */
   public static boolean isWebsafeSerializationOfASingleKey(String serializedKey) {
      boolean isProperKey = true;
		if (serializedKey.length() != VERSION_0_ENCODED_KEYLENGTH) isProperKey=false;
      if (serializedKey.charAt(0) != 'k') isProperKey = false;
      if (serializedKey.charAt(1) != '0') isProperKey = false;
      if ( (serializedKey.charAt(2) != 'r') && (serializedKey.charAt(2) != 's') ) isProperKey = false;
      for (int i=0; i<32; i++) {
         if (serializedKey.charAt(i+3)<'a' || serializedKey.charAt(i+3)>'p') isProperKey = false;
      }
      if (serializedKey.charAt(35) != 'z') isProperKey = false;
      for (int i=0; i<128; i++) {
         if (serializedKey.charAt(i+36)<'a' || serializedKey.charAt(i+36)>'p') isProperKey = false;
      }

      return isProperKey;
   }


	public boolean getIsForReceiving() {
		return isForReceiving;
	}


   public void toggleReceivingMode() {
      this.isForReceiving = !this.isForReceiving;
   }


	public boolean getIsExchanged() {
		return isExchanged;
	}


	public void setIsExchanged(boolean flag) {
		isExchanged = flag;
	}


	public boolean getIsUsed() {
		return isUsed;
	}


	public void setIsUsed(boolean flag) {
		isUsed = flag;
      if (true == isUsed) {
         for(int i=0; i<KEYBODY_LENGTH; i++) {
            this.keybody[i] = 0;
         }
      }
	}


   public int getHeaderIDShortHash() {
      return headerid_shorthash;
   }


	public byte[] getHeaderID() {
		final byte[] copyOfHeaderID = new byte[HEADERID_LENGTH];
		for(int i=0; i<HEADERID_LENGTH; i++) {
			copyOfHeaderID[i] = this.headerid[i];
		}
		return copyOfHeaderID;
	}


   public boolean isHeaderIdentical(byte[] headerForComparison) {
      if (headerForComparison.length != this.headerid.length) return false;
      for (int i=0; i<this.headerid.length; i++) {
         if (headerForComparison[i] != this.headerid[i]) return false;
      }
      return true;
   }


	public byte[] getKeybody() {
		final byte[] copyOfkeybody = new byte[KEYBODY_LENGTH];
		for(int i=0; i<KEYBODY_LENGTH; i++) {
			copyOfkeybody[i] = this.keybody[i];
		}
		return copyOfkeybody;
	}


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

		final Messagekey otherKey = (Messagekey) obj;

      if (this.getIsUsed() != otherKey.getIsUsed()) {
         return false;
      }
      if (this.getIsExchanged() != otherKey.getIsExchanged()) {
         return false;
      }
      if (this.getIsForReceiving() != otherKey.getIsForReceiving()) {
         return false;
      }
      byte[] myHeaderID = this.getHeaderID();
      byte[] otherHeaderID = otherKey.getHeaderID();
      for (int i=0;i<HEADERID_LENGTH;i++) {
         if (myHeaderID[i] != otherHeaderID[i]) {
            return false;
         }
      }
      byte[] myKeybody = this.getKeybody();
      byte[] otherKeybody = otherKey.getKeybody();
      for (int i=0;i<KEYBODY_LENGTH;i++) {
         if (myKeybody[i] != otherKeybody[i]) {
            return false;
         }
      }

      return true;
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
      if (isUsed) {
         sb.append("used!_");
      } else {
         sb.append("fresh_");
      }
      if (isForReceiving) {
         sb.append("R:");
      } else {
         sb.append("S:");
      }
      if (isExchanged) {
         sb.append("X");
      } else {
         sb.append("L");
      }
      sb.append(headerid_shorthash);

      return sb.toString();
   }
}
