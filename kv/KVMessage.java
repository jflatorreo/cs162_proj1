/**
 * XML Parsing library for the key-value store
 * 
 * @author Mosharaf Chowdhury (http://www.mosharaf.com)
 * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
 * 
 * Copyright (c) 2012, University of California at Berkeley
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *	notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *	notice, this list of conditions and the following disclaimer in the
 *	documentation and/or other materials provided with the distribution.
 *  * Neither the name of University of California, Berkeley nor the
 *	names of its contributors may be used to endorse or promote products
 *	derived from this software without specific prior written permission.
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

import java.io.FilterInputStream;
import java.io.InputStream;
import java.net.Socket;

/** Part I */
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import nachos.kv.KVException;
/** Part I END */

/**
 * This is the object that is used to generate messages the XML based messages 
 * for communication between clients and servers. 
 */
public class KVMessage {
	//Fields
	private String msgType = null;
	private String key = null;
	private String value = null;
	private String status = null;
	private String message = null;

	//Helper Methods
	public final String getMsgType() { return msgType; }
	public final String getKey() { return key; }
	public final String getValue() { return value; }
	public final String getStatus() { return status; }
	public final String getMessage() { return message; }

	public final void setKey(String key) { this.key = key; }
	public final void setValue(String value) { this.value = value; }
	public final void setStatus(String status) { this.status = status; }
	public final void setMessage(String message) { this.message = message; }

	/** Part I */
	private boolean validMsgType(String msgType) {
		if (msgType=="getreq" || msgType=="putreq" || msgType=="delreq" || msgType=="resp")
			return true;
		else
			return false;
	}
	/** Part I END */

	/* Solution from http://weblogs.java.net/blog/kohsuke/archive/2005/07/socket_xml_pitf.html */
	private class NoCloseInputStream extends FilterInputStream {
		public NoCloseInputStream(InputStream in) {
			super(in);
		}
		public void close() {} // ignore close
	}

	/** Part I */
	/***
	 * 
	 * @param msgType
	 * @throws KVException of type "resp" with message "Message format incorrect" if msgType is unknown
	 */
	public KVMessage(String msgType) throws KVException {
		if (validMsgType(msgType) == false)
			throw new KVException(new KVMessage("resp", "Message format incorrect"));
		else
			this.msgType = msgType;
	}

	public KVMessage(String msgType, String message) throws KVException {
		if (validMsgType(msgType) == false)
			throw new KVException(new KVMessage("resp", "Message format incorrect"));
		else if (msgType != "resp" && (message == "" || message == null))
			throw new KVException(new KVMessage("resp", "Message format incorrect"));
		else {
			this.msgType = msgType;
			this.message = message;
		}
	}

	/***
	 * Parse KVMessage from incoming network connection
	 * @param sock
	 * @throws KVException if there is an error in parsing the message. The exception should be of type resp and message should be :
	 * a. "XML Error: Received unparseable message" - if the received message is not valid XML.
	 * b. "Network Error: Could not receive data" - if there is a network error causing an incomplete parsing of the message.
	 * c. "Message format incorrect" - if there message does not conform to the required specifications. Examples include incorrect message type. 
	 */
	public KVMessage(InputStream input) throws KVException {
		Exception ex = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(input);

			Element root = doc.getDocumentElement();
			root.normalize();
			
			if (root.getNodeName() != "KVMessage")
				throw new KVException(new KVMessage("resp", "Message format incorrect"));

			String msgType = root.getAttribute("type");
			NodeList keyList = root.getElementsByTagName("Key");
			NodeList valueList = root.getElementsByTagName("Value");
			NodeList messageList = root.getElementsByTagName("Message");

			if (keyList.getLength()>1 || valueList.getLength()>1 || messageList.getLength()>1)
				throw new KVException(new KVMessage("resp", "Message format incorrect"));
			
			switch (msgType) {
				case ("putreq"):
					if (keyList.getLength()==0 || valueList.getLength()==0)
						throw new KVException(new KVMessage("resp", "Message format incorrect"));
					else {
						String key = keyList.item(0).getTextContent();
						String value = valueList.item(0).getTextContent();
						if (key=="" || key==null || value=="" || value==null)
							throw new KVException(new KVMessage("resp", "Message format incorrect"));
					}
					break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate the XML representation for this message.
	 * @return the XML String
	 * @throws KVException if not enough data is available to generate a valid KV XML message
	 */
	public String toXML() throws KVException {
		return null;
		// TODO: implement me
	}

	public void sendMessage(Socket sock) throws KVException {
		// TODO: implement me
	}
	/** Part I END */
}
