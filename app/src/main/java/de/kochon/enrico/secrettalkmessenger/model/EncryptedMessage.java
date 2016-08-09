package de.kochon.enrico.secrettalkmessenger.model;

import java.util.Date;
import java.util.List;

import de.kochon.enrico.secrettalkmessenger.tools.ByteToStringEncoding;

/** transient helper for creation of transport level security
 *  not necessary to persist in database
 */
public class EncryptedMessage {

   public final static int VERSION_0_ENCODED_ENCRYPTEDMESSAGELENGTH = 163;

	public final byte[] header;
	public final byte[] body;


	public EncryptedMessage(byte[] header, byte[] body) {
		if (header.length != Messagekey.HEADERID_LENGTH) 
         throw new IllegalArgumentException(String.format("Size of HEADER does not fit, expected %d, but got %d chars!", Messagekey.HEADERID_LENGTH, header.length)); 
		if (body.length != Messagekey.KEYBODY_LENGTH) 
         throw new IllegalArgumentException(String.format("Size of BODY does not fit, expected %d, but got %d chars!", Messagekey.KEYBODY_LENGTH, body.length)); 
		this.header = new byte[Messagekey.HEADERID_LENGTH];
		this.body = new byte[Messagekey.KEYBODY_LENGTH];
		for(int i=0; i<Messagekey.HEADERID_LENGTH; i++) {
			this.header[i] = header[i];
		}

		for(int i=0; i<Messagekey.KEYBODY_LENGTH; i++) {
			this.body[i] = body[i];
		}
	}


   public EncryptedMessage(String websafeSerialization) {
		if (websafeSerialization.length() != VERSION_0_ENCODED_ENCRYPTEDMESSAGELENGTH) throw new IllegalArgumentException("Size of serialized encrypted message does not fit!");
      if (websafeSerialization.charAt(0) != 'c') throw new IllegalArgumentException("Error: header mismatch!");
      if (websafeSerialization.charAt(1) != '0') throw new IllegalArgumentException("Error: key version mismatch!");
      header = ByteToStringEncoding.unencodeFromSmallLetters(websafeSerialization.substring(2, 34));
      body = ByteToStringEncoding.unencodeFromSmallLetters(websafeSerialization.substring(35,163));
   }


	public byte[] getBody() {
		final byte[] copyOfbody = new byte[Messagekey.KEYBODY_LENGTH];
		for(int i=0; i<Messagekey.KEYBODY_LENGTH; i++) {
			copyOfbody[i] = this.body[i];
		}
		return copyOfbody;
	}


   public static EncryptedMessage encrypt(StructuredMessageBody m, Messagekey k) {
      byte vanillabodybytes[] = m.getFullBody();
      if (vanillabodybytes.length > Messagekey.KEYBODY_LENGTH)
         throw new IllegalArgumentException(String.format("Messages huger than %d bytes are not supported, current message has %d bytes!", 
                                                         Messagekey.KEYBODY_LENGTH, vanillabodybytes.length));
      byte cryptogram[] = k.getKeybody();
      for (int i=0; i<vanillabodybytes.length; i++) {
         cryptogram[i] = (byte) (cryptogram[i] ^ vanillabodybytes[i]);
      }
      return new EncryptedMessage(k.getHeaderID(), cryptogram);
   }


   public Messagekey findMatchingKey(List<Messagekey> hay) {
      for(Messagekey haypiece: hay) {
         if (haypiece.isHeaderIdentical(this.header)) return haypiece;
      }
      return null;
   }


   public StructuredMessageBody decrypt(Messagekey k) {
      byte vanillabodybytes[] = this.getBody();
      if (vanillabodybytes.length > Messagekey.KEYBODY_LENGTH)
         throw new IllegalArgumentException(String.format("Messages huger than %d bytes are not supported, current message has %d bytes!", 
                                                         Messagekey.KEYBODY_LENGTH, vanillabodybytes.length));

      byte cryptogram[] = k.getKeybody();
      for (int i=0; i<vanillabodybytes.length; i++) {
         cryptogram[i] = (byte) (cryptogram[i] ^ vanillabodybytes[i]);
      }

      return new StructuredMessageBody(cryptogram);
   }


   /**
     * EncryptedMessage has a fixed header and a fixed size string encoded binary part
     * current version 0 is exactly 163 chars long
     * the employed encoding algorithm is very simple:
     * split every byte into two halfbytes and encode them into the first 16 letters
     * not as efficent like the uuencode-algorithm, but sufficient for many purposes
     * result is a charrange from a=97 till including p=112
     * structure of serialized encrypted message:
     * "c0abhgfm.....dfdfzbha...dfhfdabjkop"
     * char 0: fixed "c" as classification for cryptogram
     * char 1: version currently 0
     * char 2-33: encoded-keyid
     * char 34: "z" static and only for better readability
     * char 35-162: encoded-keybody
     * @return a full serialization of entire encrypted message in String format
     */
   public String getWebsafeSerialization() {
		StringBuffer sb = new StringBuffer();
      sb.append("c0");
      sb.append(ByteToStringEncoding.encodeToSmallLetters(header));
      sb.append("z");
      sb.append(ByteToStringEncoding.encodeToSmallLetters(body));
      String serializedEncryptedMessage = sb.toString();
		if (serializedEncryptedMessage.length() != VERSION_0_ENCODED_ENCRYPTEDMESSAGELENGTH) throw new IllegalArgumentException("Size of serialized encrypted message does not fit!");
      return serializedEncryptedMessage;
   }

   public static boolean isEncryptedMessage(String m) {
      if (null == m || m.equals("")) return false;
      if (m.length() != VERSION_0_ENCODED_ENCRYPTEDMESSAGELENGTH) return false;
      if (m.charAt(0) != 'c') return false;
      if (m.charAt(1) != '0') return false;
      for (int i=0; i<32; i++) {
         if (m.charAt(i+2)<'a' || m.charAt(i+2)>'p') return false;
      }
      if (m.charAt(34) != 'z') return false;
      for (int i=0; i<128; i++) {
         if (m.charAt(i+35)<'a' || m.charAt(i+35)>'p') return false;
      }
      return true;
   }

}
