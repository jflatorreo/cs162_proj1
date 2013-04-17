package nachos.kv;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

public class KVMessageUnitTest {
	UnitTestChecker test = null;
	
	public void constructor1() {
		//Test #1 for constructor1
		test = new UnitTestChecker("KVMessage","constructor1",1);
		try {
			KVMessage kv = new KVMessage("getreq");
			test.assertTrue(kv.getMsgType() == "getreq");
		}
		catch (KVException e) {
			System.out.println(test.fail());
		}
		
		//Test #2 for constructor1
		test = new UnitTestChecker("KVMessage","constructor1",2);
		try {
			KVMessage kv = new KVMessage("wrongType");
			System.out.println(test.fail());
		}
		catch (KVException e) {
			KVMessage kv = e.getMsg();
			test.assertTrue(kv.getMsgType()=="resp" && kv.getMessage()=="Message format incorrect");
		}
	}
	
	public void constructor2() {
		//Test #1 for constructor2
		test = new UnitTestChecker("KVMessage","constructor2",1);
		try {
			KVMessage kv = new KVMessage("resp", "This is a sample message");
			test.assertTrue(kv.getMsgType()=="resp" && kv.getMessage()=="This is a sample message");
		}
		catch (KVException e) {
			System.out.println(test.fail());
		}
		
		//Test #2 for constructor2
		test = new UnitTestChecker("KVMessage","constructor2",2);
		try {
			KVMessage kv = new KVMessage("wrongType");
			System.out.println(test.fail());
		}
		catch (KVException e) {
			KVMessage kv = e.getMsg();
			test.assertTrue(kv.getMsgType()=="resp" && kv.getMessage()=="Message format incorrect");
		}
	}
	
	public void constructor3() {
		//Test #1 for constructor3
		test = new UnitTestChecker("KVMessage","constructor3",1);
		try {
			String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>sampleKey</Key></KVMessage>";
			InputStream stream = new ByteArrayInputStream(input.getBytes());
            KVMessage kv = new KVMessage(stream);
            test.assertTrue(kv.getMsgType().equals("getreq") && kv.getKey().equals("sampleKey"));
		}
		catch (KVException e) {
			System.out.println(test.fail());
		}
		
		//Test #2 for constructor3
		test = new UnitTestChecker("KVMessage","constructor3",2);
		try {
			String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>sampleKey</Key><Value>sampleValue</Value></KVMessage>";
			InputStream stream = new ByteArrayInputStream(input.getBytes());
            KVMessage kv = new KVMessage(stream);
            test.assertTrue(kv.getMsgType().equals("putreq") && kv.getKey().equals("sampleKey") && kv.getValue().equals("sampleValue"));
		}
		catch (KVException e) {
			System.out.println(test.fail());
		}
		
		//Test #3 for constructor3
		test = new UnitTestChecker("KVMessage","constructor3",3);
		try {
			String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"delreq\"><Key>sampleKey</Key></KVMessage>";
			InputStream stream = new ByteArrayInputStream(input.getBytes());
            KVMessage kv = new KVMessage(stream);
            test.assertTrue(kv.getMsgType().equals("delreq") && kv.getKey().equals("sampleKey"));
		}
		catch (KVException e) {
			System.out.println(test.fail());
		}
		
		//Test #4 for constructor3
		test = new UnitTestChecker("KVMessage","constructor3",4);
		try {
			String wrongInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"WRONG_TYPE\"><Key>sampleKey</Key></KVMessage>";
			InputStream stream = new ByteArrayInputStream(wrongInput.getBytes());
            KVMessage kv = new KVMessage(stream);
            System.out.println(test.fail());
		}
		catch (KVException e) {
			KVMessage kv = e.getMsg();
			test.assertTrue(kv.getMsgType()=="resp" && kv.getMessage()=="Message format incorrect");
		}
	}
	
	public void toXML() {
		//Test #1 for toXML
		test = new UnitTestChecker("KVMessage","toXML",1);
		try {
			KVMessage kv = new KVMessage("getreq");
			kv.setKey("sampleKey");
			String answer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>sampleKey</Key></KVMessage>";
			test.assertTrue(kv.toXML().equals(answer));
		}
		catch (KVException e) {
			System.out.println(test.fail());
		}
		
		//Test #2 for toXML
		test = new UnitTestChecker("KVMessage","toXML",2);
		try {
			KVMessage kv = new KVMessage("putreq");
			kv.setKey("sampleKey");
			kv.setValue("sampleValue");
			String answer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>sampleKey</Key><Value>sampleValue</Value></KVMessage>";
			test.assertTrue(kv.toXML().equals(answer));
		}
		catch (KVException e) {
			System.out.println(test.fail());
		}
		
		//Test #3 for toXML
		test = new UnitTestChecker("KVMessage","toXML",3);
		try {
			KVMessage kv = new KVMessage("delreq");
			kv.setKey("sampleKey");
			String answer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"delreq\"><Key>sampleKey</Key></KVMessage>";
			test.assertTrue(kv.toXML().equals(answer));
		}
		catch (KVException e) {
			System.out.println(test.fail());
		}
	}
	
	public void run() {
		this.constructor1();
		this.constructor2();
		this.constructor3();
		this.toXML();
	}
}