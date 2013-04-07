/**
 * Persistent Key-Value storage layer. Current implementation is transient, 
 * but assume to be backed on disk when you do your project.
 * 
 * @author Mosharaf Chowdhury (http://www.mosharaf.com)
 * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
 * 
 * Copyright (c) 2012, University of California at Berkeley
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of University of California, Berkeley nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
//package edu.berkeley.cs162;
package nachos.kv;
import java.io.FileWriter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.xml.stream.events.XMLEvent;


/**
 * This is a dummy KeyValue Store. Ideally this would go to disk, 
 * or some other backing store. For this project, we simulate the disk like 
 * system using a manual delay.
 *
 */
public class KVStore implements KeyValueInterface {
	private Dictionary<String, String> store = null;
	
	protected class Pair {
		public String pairKey;
		public String pairValue;
		
		public Pair () {}
	}
	
	public KVStore() {
		resetStore();
	}

	private void resetStore() {
		store = new Hashtable<String, String>();
	}
	
	public boolean put(String key, String value) throws KVException {
		AutoGrader.agStorePutStarted(key, value);
		
		try {
			putDelay();
			store.put(key, value);
			return false;
		} finally {
			AutoGrader.agStorePutFinished(key, value);
		}
	}
	
	public String get(String key) throws KVException {
		AutoGrader.agStoreGetStarted(key);
		
		try {
			getDelay();
			String retVal = this.store.get(key);
			if (retVal == null) {
			    KVMessage msg = new KVMessage("resp", "key \"" + key + "\" does not exist in store");
			    throw new KVException(msg);
			}
			return retVal;
		} finally {
			AutoGrader.agStoreGetFinished(key);
		}
	}
	
	public void del(String key) throws KVException {
		AutoGrader.agStoreDelStarted(key);

		try {
			delDelay();
			if(key != null)
				this.store.remove(key);
		} finally {
			AutoGrader.agStoreDelFinished(key);
		}
	}
	
	private void getDelay() {
		AutoGrader.agStoreDelay();
	}
	
	private void putDelay() {
		AutoGrader.agStoreDelay();
	}
	
	private void delDelay() {
		AutoGrader.agStoreDelay();
	}
	
    public String toXML() throws KVException {
        // TODO: implement me
        return null;
    }        

    public void dumpToFile(String fileName) throws KVException {
    	try {
    		String xmlString = this.toXML();
    		FileWriter fw = new FileWriter(fileName)
    		fw.write(xmlString);
    	}
    	catch (Exception e) {
    		throw new KVException ("IO Error");
    	}
    	return;
    }

    public void restoreFromFile(String fileName) throws KVException{
    		// KVPair is a private class containing two public instance variables, key and
    		// value
    		int parseErrors = 0;
    		List<E> <Pair> pairs = new List<Pair>();
    		FileInputStream fs = new FileInputStream(fileName);
    		XMLInputFactory xmlif = XMLInputFactory.newInstance();
    		XMLEventReader eventReader = xmlif.createXMLEventReader(fs);
    		Pair pair;
    		try {
	    		while (eventReader.hasNext()) {
		    		XMLEvent event = eventReader.nextEvent();
		    		if (event.isStartElement()) {
		    			StartElement element = event.asStartElement().getName().getLocalPart();
			    		if (element.equals("KVStore")) {
			    			continue;
			    		}
			    		else if (element.equals("KVPair")) {
			    			pair new Pair();
			    			continue;
			    		}
			    		else if (element.equals("Key")) {
			    			event = eventReader.nextEvent();
			    			pair.pairKey = event.asCharacters().getData();
			    			continue;
			    		}
			    		else if (element.equals("Value")) {
			    			event = eventReader.nextEvent();
			    			pair.pairValue = event.asCharacters().getData();
			    			continue;
			    		}
			    		else {
			    			parseErrors++;
			    			break;
			    		}
		    		}
		    		else if (event.isEndElement()) {
		    			EndElement element = event.asEndElement().getName().getLocalPart();
			    		else if (element.equals("KVPair")) {
			    			pairs.add(pair);
			    			pair = null;
			    			continue;
		    			}
			    		else if (element.equals("KVStore") || element.equals("Key") || element.equals("Value")) {
			    			continue;
			    		}
			    		else {
			    			parseErrors++;
			    			break;
			    		}
		    		}
	    		}
    		}
    		catch (Exception e) {
    			throw new KVException();
    		}
    		if (parseErrors > 0 || pair != null) {
    			throw new KVException();
    		}
    		for (Pair p: pairs) {
    			store.put(p.pairKey, p.pairValue);
    		}
    		return;
    }
}
