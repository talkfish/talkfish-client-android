package de.kochon.enrico.secrettalkmessenger.test;

import de.kochon.enrico.secrettalkmessenger.model.Messagekey;
import android.test.AndroidTestCase;

public class MessagekeyTest extends AndroidTestCase {
	
	public void testCreation() {
		long id = 1;
		final byte[] headerid = new byte[16];
		final byte[] keybody = new byte[64];
		boolean isForReceiving = true;
		boolean isExchanged = false;
		boolean isUsed = false;

		for(int i=0; i<headerid.length; i++) {
			headerid[i] = 0;
		}
		for(int i=0; i<keybody.length; i++) {
			keybody[i] = 0;
		}
		Messagekey key_1 = new Messagekey(id, headerid, keybody, isForReceiving, isExchanged, isUsed);
		for(int i=0; i<headerid.length; i++) {
			headerid[i] = 1;
		}
		for(int i=0; i<keybody.length; i++) {
			keybody[i] = 1;
		}
		final byte[] idfromkey = key_1.getHeaderID();
		for(int i=0; i<headerid.length; i++) {
			assertTrue(headerid[i]!=idfromkey[i]);
		}
		
		Messagekey key_2 = new Messagekey(id, headerid, keybody, isForReceiving, isExchanged, isUsed);
		assertTrue(key_2.getIsForReceiving());
		isForReceiving = false;
		Messagekey key_3 = new Messagekey(id, headerid, keybody, isForReceiving, isExchanged, isUsed);
		assertTrue(!key_3.getIsForReceiving());
		java.util.Random r = new java.util.Random(42);
		r.nextBytes(keybody);
		Messagekey key_4 = new Messagekey(id, headerid, keybody, isForReceiving, isExchanged, isUsed);
		final byte[] keybodyfromkey = key_4.getKeybody();
		assertEquals(keybody.length, keybodyfromkey.length);
		assertTrue(keybody!=keybodyfromkey);
		for (int i=0; i<keybody.length; i++) {
			assertEquals(keybody[i], keybodyfromkey[i]);
		}
	}
	
	public void testExceptions() {
		long id = 1;
		final byte[] headerid = new byte[16];
		final byte[] keybody = new byte[64];
		boolean isForReceiving = true;
		final byte[] id_toshort = new byte[2];
		final byte[] keybody_tolong = new byte[164];
		boolean isExchanged = false;
		boolean isUsed = false;
		try {
			Messagekey wrongid = new Messagekey(id, id_toshort, keybody, isForReceiving, isExchanged, isUsed);
			fail("to short id accepted.");
		} catch (IllegalArgumentException iae) {
			// ok
		}
		try {
			Messagekey wrongbody = new Messagekey(id, headerid, keybody_tolong, isForReceiving, isExchanged, isUsed);
			fail("to long keybody accepted.");
		} catch (IllegalArgumentException iae) {
			// ok
		}
		try {
			Messagekey wrongidandbody = new Messagekey(id, id_toshort, keybody_tolong, isForReceiving, isExchanged, isUsed);
			fail("to short id and to long keybody accepted.");
		} catch (IllegalArgumentException iae) {
			// ok
		}
	}
	

	public void testEqualness() {
		long id = 1;
		boolean isExchanged = false;
		boolean isUsed = false;
		final byte[] k1_id = new byte[16];
		final byte[] k1_keybody = new byte[64];
		boolean k1_isForSending = true;
		k1_keybody[0] = 1;
		final byte[] k2_id = new byte[16];
		final byte[] k2_keybody = new byte[64];
		k2_keybody[0] = 1;
		boolean k2_isForSending = true;
		final Messagekey k1 = new Messagekey(id, k1_id, k1_keybody, k1_isForSending, isExchanged, isUsed);
		final Messagekey k2 = new Messagekey(id, k2_id, k2_keybody, k2_isForSending, isExchanged, isUsed);

		assertTrue(k1.equals(k2));
		final byte[] k3_id = new byte[16];
		final byte[] k3_keybody = new byte[64];
		boolean k3_isForSending = false;
		k3_keybody[0] = 1;
		final Messagekey k3 = new Messagekey(id, k3_id, k3_keybody, k3_isForSending, isExchanged, isUsed);
		assertTrue(!k1.equals(k3));
		final byte[] k4_id = new byte[16];
		final byte[] k4_keybody = new byte[64];
		boolean k4_isForSending = false;
		k4_keybody[0] = 2;
		final Messagekey k4 = new Messagekey(id, k4_id, k4_keybody, k4_isForSending, isExchanged, isUsed);
		assertTrue(!k3.equals(k4));
	}
	
	// todo check on hashvalue/ sortorder - actually sorting and hashs for keys are not supported 

/* further ideas	
	public void testInvalidationAfterTimetolive() {
		fail("not implemented");
	}
	
	
	public void testInvalidationAfterUsage() {
		fail("not implemented"); // maybe at higher level in protocolstack
	}
   */
}
