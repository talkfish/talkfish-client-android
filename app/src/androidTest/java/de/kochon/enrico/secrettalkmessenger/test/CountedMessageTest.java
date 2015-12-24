package de.kochon.enrico.secrettalkmessenger.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeSet;

import android.test.AndroidTestCase;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;

public class CountedMessageTest extends AndroidTestCase {

	public void testExceptions() {
		try {
			CountedMessage cm = new CountedMessage(true, -1, -1, "sdkf", new Date());
			fail("expected IllegalArgumentException for wrong number");
		} catch (IllegalArgumentException eae) {
			// ok
		}
		try {
			CountedMessage cm = new CountedMessage(true, 1, 1, null, new Date());
			fail("expected IllegalArgumentException for null string");
		} catch (IllegalArgumentException eae) {
			// ok
		}
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<3000; i++) {
			sb.append("lore ipsum");
		}
		try {
			CountedMessage cm = new CountedMessage(true, 1, 1, sb.toString(), new Date());
			fail("expected IllegalArgumentException for huge string");
		} catch (IllegalArgumentException eae) {
			// ok
		}
		try {
			GregorianCalendar earlyDate = new GregorianCalendar();
			earlyDate.set(Calendar.YEAR, 2003);
			CountedMessage cm = new CountedMessage(true, 1, 1, "foobar", earlyDate.getTime());
			fail("expected IllegalArgumentException for too early year");
		} catch (IllegalArgumentException iae) {
			// ok
		}
	}
	

	public void testDate() {
		Date before = new Date();
		CountedMessage cm = new CountedMessage(true, 1, 1, "foobar", new Date());
		Date after = new Date();
		if (before.compareTo(cm.getCreated()) > 0) fail("creationtime to early");
		if (after.compareTo(cm.getCreated()) < 0) fail("creationtime to late");
		Calendar currentCal = Calendar.getInstance();
		currentCal.setTime(cm.getCreated());
		if (currentCal.get(Calendar.YEAR)<2014) fail("yearinformation not properly set");
	}
	
	
	public void testEquals() {
        GregorianCalendar messageDate = new GregorianCalendar();
        messageDate.set(2014,9,11,2,3,1);
        CountedMessage i_1 = new CountedMessage(true, 1, 1, "hi howdy", messageDate.getTime());
        messageDate.set(2014,9,11,2,3,2);
        CountedMessage i_2 = new CountedMessage(true, 1, 1, "hi howdy", messageDate.getTime());
        CountedMessage i_3 = new CountedMessage(true, 1, 1, "hi howdy ho", messageDate.getTime());
        messageDate.set(2014,9,11,2,3,1);
        CountedMessage i_4 = new CountedMessage(true, 1, 1, "hi howdy", messageDate.getTime());
        String hi = "hi";
        CountedMessage i_5 = new CountedMessage(true, 1, 1, hi + " howdy", messageDate.getTime());
        assertTrue(i_1.equals(i_4));
        assertTrue(!i_1.equals(i_2));
        assertTrue(!i_1.equals(i_3));
        assertTrue(!i_2.equals(i_3));
        assertTrue(i_1.equals(i_5));
	}
	

	public void testHashing() {
		Set<CountedMessage> set = new TreeSet<CountedMessage>();
		Date now = new Date();
		set.add(new CountedMessage(true, 1, 1, "foobaaaar", now));
		set.add(new CountedMessage(true, 1, 1, "foobaaaar", now));
		assertEquals(1, set.size());
	}
	

	public void testSorting() {
        GregorianCalendar messageDate = new GregorianCalendar();
        messageDate.set(2014,9,11,2,3,1);
        CountedMessage i_1 = new CountedMessage(true, 1, 1, "hi howdy", messageDate.getTime());
        messageDate.set(2014,9,11,2,3,2);
        CountedMessage i_2 = new CountedMessage(true, 1, 1, "hi howdy", messageDate.getTime());
        CountedMessage i_3 = new CountedMessage(true, 1, 1, "hi howdy ho", messageDate.getTime());
        messageDate.set(2014,9,11,2,3,2);
        CountedMessage i_4 = new CountedMessage(true, 2, 2, "hi howdy", messageDate.getTime());
        TreeSet<CountedMessage> sortingSet = new TreeSet<CountedMessage>();
        sortingSet.add(i_3);
        sortingSet.add(i_2);
        sortingSet.add(i_4);
        sortingSet.add(i_1);
        assertEquals(4, sortingSet.size());
        assertEquals(i_1, sortingSet.first());
        assertEquals(i_4, sortingSet.last());
	}
	
	
	public void testToString() {
        GregorianCalendar messageDate = new GregorianCalendar();
        messageDate.set(2014,9,11,2,3,1);
        CountedMessage i_1 = new CountedMessage(true, 1, 1, "hi howdy", messageDate.getTime());
        messageDate.set(2014,9,11,12,3,5);
        CountedMessage i_2 = new CountedMessage(false, 1, 1, "hi back", messageDate.getTime());
        messageDate.set(2014,11,11,12,3,5);
        CountedMessage i_3 = new CountedMessage(false, 1, 1, "hi back", messageDate.getTime());
        messageDate.set(2014,0,11,12,3,5);
        CountedMessage i_4 = new CountedMessage(false, 1, 1, "hi back", messageDate.getTime());
        assertEquals("(i#1 2014/10/11 02:03:01) hi howdy", i_1.toString());
        assertEquals("(o#1 2014/10/11 12:03:05) hi back", i_2.toString());
        assertEquals("(o#1 2014/12/11 12:03:05) hi back", i_3.toString());
        assertEquals("(o#1 2014/01/11 12:03:05) hi back", i_4.toString());
	}
	

}
