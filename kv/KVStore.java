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
import java.io.File;
import java.io.FileWriter;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


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
		public Pair(String k, String v) {
			this.pairKey = new String (k);
			this.pairValue = new String (v);
		}
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
	/*
	 * <?xml version="1.0" encoding="UTF-8"?>
<KVStore>
    <KVPair>
      <Key>key</Key>
      <Value>value</Value>
    </KVPair>
    <KVPair>
      <Key>key2</Key>
      <Value>value2</Value>
    </KVPair>
</KVStore>
	 */
    public String toXML() {
    	try { 
    		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    		Document doc = docBuilder.newDocument();
    		Element rElem, pElem, kElem, vElem;
    		Node kValue, vValue;
    		rElem = doc.createElement("KVStore");
    		doc.appendChild(rootElement);
    		for (String key: store.keys()) {
    			pElem = doc.createElement("KVPair");
    			//Key
                kValue = doc.createTextNode(key);
                kElem = doc.createElement("Key");
                kElem.appendChild(kValue);
                pElem.appendChild(kElem);
                //Value
                vValue = doc.createTextNode(store.get(key));
                vElem = doc.createElement("Value");
                vElem.appendChild(vValue);
    			pElem.appendChild(vElem);
    			//Append pair to root
                rElem.appendChild(pElem);	
    		}
    		DOMSource domSource = new DOMSource(doc);
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(domSource, streamResult);
            return stringWriter.toString();
    	}
        catch (Exception e) {
        	throw new KVException(new KVMessage("resp", "Error during KVStore toXML"));
        }
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
    		
    public void restoreFromFile(String fileName) throws Exception {
        File f = new File(fileName);
        if (!f.exists() || !f.canRead()) {
            throw new IOException(fileName + " could not be opened");
        }
        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            doc = docBuilder.parse(fileSrc);
	        if (!doc.getXmlEncoding().equals("UTF-8")) {
	            throw new KVException(new KVMessage("resp", "Unknown Error: Incorrect XML char encoding."));
	        }
	        NodeList node = doc.getElementsByTagName("KVStore");
	        if (nodes.getLength() != 1) {
	            throw new KVException(new KVMessage("resp", "Unknown Error: XML format incorrect"));
	        }

	        // Restore K-V pairs
	        NodeList pairNodes = ((Element)nodes.item(0)).getElementsByTagName("KVPair");
	        Element pElem, kElem, vElem;
	        for (int i = 0; i < pairNodes.getLength(); i++) {
	        	pElem = (Element) pairNodes.item(i);
	        	kElem = (Element) pElem.getElementsByTagName("Key")[0];
	        	vElem = (Element) pElem.getElementsByTagName("Value")[0];
	        	put (vElem.getFirstChild().getNodeValue(), kElem.getFirstChild().getNodeValue())
	        }
        } catch (IOException ioe) {
            throw new KVException(new KVMessage("resp", "I/O Error during restoreFromFile"));
        } catch (SAXException se){
            throw new KVException(new KVMessage("resp", "XML Error during restoreFromFile"));
        } catch (IllegalArgumentException iae) {
            throw new KVException(new KVMessage("resp", filename + " is null..."));
        } catch (ParserConfigurationException pce) {
            throw new KVException(new KVMessage("resp", "Unknown Error: ParserConfigurationException"));
        }
        return;
    }
}
