package de.kochon.enrico.secrettalkmessenger.tools;

/**
 * Created by enrico on 09.08.16.
 */
public class ByteToStringEncoding {


    /**
     * ascii range of small letters from 97 = a to 122 = z,
     * source: http://de.wikipedia.org/wiki/Ascii
     *
     * using ints is necessary due to javas lack of support to unsigned values
     * for more details see: http://darksleep.com/player/JavaAndUnsignedTypes.html
     */
    public static String encodeToSmallLetters(byte data[]) {
        StringBuffer encodedData = new StringBuffer();
        for (int i=0; i<data.length; i++) {
            int lowpart = 0x0f & data[i];
            int highpart = 0x0f & (data[i] >> 4);
            // System.out.println(String.format("byte: %d, low: %d, high: %d", 0xff & data[i], lowpart, highpart));
            encodedData.append((char)(lowpart + 'a'));
            encodedData.append((char)(highpart + 'a'));
        }

        return encodedData.toString();
    }


    public static byte[] unencodeFromSmallLetters(String encoded) {
        byte[] data = new byte[encoded.length()/2];
        for (int i=0; i<data.length; i++) {
            if (encoded.charAt(i*2)<'a' || encoded.charAt(i*2)>'p' ||
                    encoded.charAt(i*2+1)<'a' || encoded.charAt(i*2+1)>'p')
            {
                throw new IllegalArgumentException("character of string to decode not in valid range!");
            }
            data[i] = (byte) ((0x0f & (encoded.charAt(i*2) - 'a')) | ((0x0f & (encoded.charAt(i*2+1) - 'a')) << 4));
        }
        return data;
    }

}
