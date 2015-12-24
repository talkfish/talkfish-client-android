package de.kochon.enrico.secrettalkmessenger.test;

import java.util.Date;
import java.util.GregorianCalendar;

import android.test.AndroidTestCase;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.CountedMessage;
import de.kochon.enrico.secrettalkmessenger.model.Channel;

public class ConversationTest extends AndroidTestCase {
	
	public void testCreation() {
      Channel devnullReceive = new Channel(0, "devnull", "null", "null", true);
      Channel devnullSend = new Channel(0, "devnull", "null", "null", false);
		Conversation nonsense = new Conversation("foobar", devnullReceive, devnullSend);
		assertEquals(0, nonsense.getCurrentNumberOfReceivedMessages());
		assertEquals(0, nonsense.getCurrentNumberOfSentMessages());
	}

	public void testAddingMessages() {
      Channel devnullReceive = new Channel(0, "devnull", "null", "null", true);
      Channel devnullSend = new Channel(0, "devnull", "null", "null", false);
		Conversation nonsense = new Conversation("foobar", devnullReceive, devnullSend);
		nonsense.addSentMessage("hey ho lets go");
		CountedMessage manualMessage1 = new CountedMessage(false, 2, 2, "bar foo", new Date());
		CountedMessage manualMessage2 = new CountedMessage(true, 1, 1, "bar foo too", new Date());
		nonsense.addMessage(manualMessage1);
		nonsense.addMessage(manualMessage2);
		assertEquals(1, nonsense.getCurrentNumberOfReceivedMessages());
		assertEquals(2, nonsense.getCurrentNumberOfSentMessages());
	}
	
	public void testExceptions() {
      Channel devnullReceive = new Channel(0, "devnull", "null", "null", true);
      Channel devnullSend = new Channel(0, "devnull", "null", "null", false);
		try {
			Conversation bla = new Conversation("foo", devnullReceive, devnullSend);
			CountedMessage wrongNumberedMessage = new CountedMessage(true, 42, 42, "lore ipsum", new Date());
			bla.addMessage(wrongNumberedMessage);
			fail("there should be an exception regarding the wrong messagenumber!");
		} catch (IllegalArgumentException iae) {
			// ok
		}
		try {
			Conversation bla = new Conversation("foo", devnullReceive, devnullSend);
			CountedMessage correctNumberedMessage = new CountedMessage(false, 1, 1, "foo", new Date());
			CountedMessage wrongNumberedMessage = new CountedMessage(true, 3, 3, "lore ipsum", new Date());
			bla.addMessage(correctNumberedMessage);
			bla.addMessage(wrongNumberedMessage);
			fail("there should be an exception regarding the wrong messagenumber!");
		} catch (IllegalArgumentException iae) {
			// ok
		}

	}
	
	public void testFormattedOutput() {
		// construct conversation
      GregorianCalendar messageDate = new GregorianCalendar();
      messageDate.set(2014,9,11,2,3,1);
      CountedMessage i_1 = new CountedMessage(true, 1, 1, "hi howdy", messageDate.getTime());
      messageDate.set(2014,9,11,12,3,5);
      CountedMessage o_1 = new CountedMessage(false, 1, 1, "hi back", messageDate.getTime());
      messageDate.set(2014,9,11,22,31,49);
      CountedMessage o_2 = new CountedMessage(false, 2, 2, "how is it going?", messageDate.getTime());

      Channel devnullReceive = new Channel(0, "devnull", "null", "null", true);
      Channel devnullSend = new Channel(0, "devnull", "null", "null", false);
		Conversation blabla = new Conversation("Mister X", devnullReceive, devnullSend);
		blabla.addMessage(i_1);
		blabla.addMessage(o_1);
		blabla.addMessage(o_2);

      assertEquals( "Mister X -> Me: (i#1 2014/10/11 02:03:01) hi howdy\n"
        			+ "Me -> Mister X: (o#1 2014/10/11 12:03:05) hi back\n"
        			+ "Me -> Mister X: (o#2 2014/10/11 22:31:49) how is it going?\n", 
        		blabla.toString());
	}

}
