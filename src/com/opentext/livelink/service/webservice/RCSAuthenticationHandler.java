package com.opentext.livelink.service.webservice;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Iterator;
import java.util.Set;

public class RCSAuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

	protected static final QName OTAUTH_NAME = new QName("urn:api.ecm.opentext.com", "OTAuthentication");

	protected SOAPHeaderElement fAuthHeader;

	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext mc) {
		// Do nothing
	}

	public boolean handleFault(SOAPMessageContext smc) {
		return handleMessage(smc);
	}

	@SuppressWarnings("unchecked")
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		if (outboundProperty.booleanValue() && fAuthHeader != null) {
			// Outbound message
			try {
				SOAPMessage message = smc.getMessage();
				SOAPHeader sh = message.getSOAPHeader();

				if (sh == null) {
					SOAPPart part = message.getSOAPPart();
					SOAPEnvelope envelope = part.getEnvelope();
					sh = envelope.addHeader();
				}

				// Remove old AuthenticationToken
				Iterator it = sh.getChildElements(OTAUTH_NAME);
				while (it.hasNext()) {
					sh.removeChild((Node) it.next());
				}
				sh.addChildElement(fAuthHeader);

			} catch (Exception e) {
				System.out.println("Exception in handler: " + e);
			}
		} else {
			try {
				SOAPHeader sh = smc.getMessage().getSOAPHeader();
				if (sh != null) {
					Iterator it = sh.getChildElements(OTAUTH_NAME);
					fAuthHeader = it.hasNext() ? (SOAPHeaderElement) it.next() : null;
				}
			} catch (Exception e) {
				System.out.println("Exception in handler: " + e);
			}
		}

		return true;
	}

}
