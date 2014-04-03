package com.opentext.livelink.service.webservice;

import com.opentext.livelink.service.core.Authentication;
import com.sun.xml.ws.developer.WSBindingProvider;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;

public class CWSAuthenticationHandler extends RCSAuthenticationHandler {

	private String fUsername;
	private String fPassword;
	private Authentication fAuthService;
	private Calendar fExpiration;

	public CWSAuthenticationHandler(URL authServiceUrl, String username, String password)
			throws Exception {
		fUsername = username;
		fPassword = password;
		fAuthService = WebServiceUtil.getAuthenticationService(authServiceUrl);
		fExpiration = Calendar.getInstance();
	}

	@SuppressWarnings("unchecked")
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		if (outboundProperty.booleanValue() && fAuthHeader != null) {
			// Outbound message
			try {
				SOAPMessage message = smc.getMessage();
				SOAPHeader sh = message.getSOAPHeader();

				// Renew auth token if about to expire (within 1 minute)
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, 1);

				if (cal.after(fExpiration)) {
					String newToken = fAuthService.authenticateUser(fUsername, fPassword);
					fAuthHeader.getFirstChild().setTextContent(newToken);

					WebServiceUtil.setSoapHeader((WSBindingProvider) fAuthService, newToken);

					XMLGregorianCalendar expDate = fAuthService.getSessionExpirationDate();
					fExpiration = (expDate != null) ? expDate.toGregorianCalendar() : Calendar.getInstance();
				}

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
