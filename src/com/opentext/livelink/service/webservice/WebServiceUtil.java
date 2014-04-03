// (c) Copyright Open Text Corporation

package com.opentext.livelink.service.webservice;

import com.opentext.ecm.api.OTAuthentication;
import com.opentext.livelink.service.core.*;
import com.opentext.livelink.service.docman.DocumentManagement;
import com.opentext.livelink.service.docman.DocumentManagement_Service;
import com.opentext.livelink.service.memberservice.MemberService;
import com.opentext.livelink.service.memberservice.MemberService_Service;
import com.opentext.livelink.service.searchservices.SearchService;
import com.opentext.livelink.service.searchservices.SearchService_Service;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.WSBindingProvider;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.MTOMFeature;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class WebServiceUtil
{

	private static final String CORE_NAMESPACE	= "urn:Core.service.livelink.opentext.com";
	private static final String ECM_API_NAMESPACE	= "urn:api.ecm.opentext.com";

	@SuppressWarnings("unchecked")
	private static List<Handler> handlers = null;
	
	public static String getAuthenticationToken( URL location, String username, String password )
	throws
            Exception
	{
		Authentication endpoint = getAuthenticationService( location );
	
		return endpoint.authenticateUser( username, password );
	}
	
	public static String getAuthenticationTokenRCS(URL location, String username, String password)
		throws Exception
	{
		com.opentext.ecm.services.authws.Authentication endpoint = getAuthenticationServiceRCS(location);

		return endpoint.authenticate(username, password);
	}

	public static ContentService getContentService( URL location, String token )
		throws Exception
	{
		ContentService endpoint;
	
		ContentService_Service service =
			new ContentService_Service(
				location,
				new QName(
					"urn:Core.service.livelink.opentext.com",
					"ContentService" ) );
	
		// content service is the only one that requires MTOM support
		endpoint = service.getBasicHttpBindingContentService( new MTOMFeature() );
	
		// force the service to use the specified URL, instead of the one defined in the WSDL
		( (BindingProvider) endpoint ).getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location.toExternalForm() );
		
		// instruct the connections to use chunked transfer encoding to support large files
		( (BindingProvider) endpoint ).getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 1024000 );
	
		setSoapHeader( (WSBindingProvider) endpoint, token );
		
		return endpoint;
	}

	public static DocumentManagement getDMService( URL location, String token )
		throws Exception
	{
		DocumentManagement endpoint;
	
		DocumentManagement_Service service =
			new DocumentManagement_Service(
				location,
				new QName(
					"urn:DocMan.service.livelink.opentext.com",
					"DocumentManagement" ) );
	
		endpoint = service.getBasicHttpBindingDocumentManagement();
		
		// force the service to use the specified URL, instead of the one defined in the WSDL
		( (BindingProvider) endpoint ).getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location.toExternalForm() );
	
		setSoapHeader( (WSBindingProvider) endpoint, token );
		
		return endpoint;
	}

	public static MemberService getMemberService( URL location, String token )
		throws Exception
	{
		MemberService endpoint;
	
		MemberService_Service service =
			new MemberService_Service(
				location,
				new QName(
					"urn:MemberService.service.livelink.opentext.com",
					"MemberService" ) );
	
		endpoint = service.getBasicHttpBindingMemberService();
		
		// force the service to use the specified URL, instead of the one defined in the WSDL
		( (BindingProvider) endpoint ).getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location.toExternalForm() );
	
		setSoapHeader( (WSBindingProvider) endpoint, token );
		
		return endpoint;
	}

	public static SearchService getSearchService( URL location, String token )
		throws Exception
	{
		SearchService endpoint;
	
		SearchService_Service service =
			new SearchService_Service(
				location,
				new QName(
					"urn:SearchServices.service.livelink.opentext.com",
					"SearchService" ) );
	
		endpoint = service.getBasicHttpBindingSearchService();
		
		// force the service to use the specified URL, instead of the one defined in the WSDL
		( (BindingProvider) endpoint ).getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location.toExternalForm() );
	
		setSoapHeader( (WSBindingProvider) endpoint, token );
		
		return endpoint;
	}

	public static void setSoapHeader(
		WSBindingProvider bindingProvider,
		OTAuthentication otAuth )
	throws
            Exception
	{
		setSoapHeader( bindingProvider, otAuth, null, null );
	}

	public static void setSoapHeader(
		WSBindingProvider bindingProvider,
		OTAuthentication otAuth,
		String contextID,
		FileAtts fileAtts )
	throws
            Exception
	{
		List<Header> headers = new ArrayList<Header>();
		SOAPMessage message = MessageFactory.newInstance().createMessage();
		SOAPPart part = message.getSOAPPart();
		SOAPEnvelope envelope = part.getEnvelope();
		SOAPHeader header = envelope.getHeader();
	
		
		if ( otAuth != null )
		{
			headers.add( getOTAuthenticationHeader( header, otAuth ) );
	
			if ( contextID != null && fileAtts != null )
			{		
				headers.add( getContextIDHeader( header, contextID ) );
				headers.add( getFileAttsHeader( header, fileAtts ) );
			}
			
			bindingProvider.setOutboundHeaders( headers );
		}
	}

	public static void setSoapHeader(
		WSBindingProvider bindingProvider,
		String authToken )
	throws
            Exception
	{
		OTAuthentication otAuth = new OTAuthentication();
		
		
		otAuth.setAuthenticationToken( authToken );
		
		setSoapHeader( bindingProvider, otAuth );
	}

	public static XMLGregorianCalendar getCalendar()
	{
		DatatypeFactory df = null;
		XMLGregorianCalendar xmlCalendar = null;
		
		try
		{
			df = DatatypeFactory.newInstance();
			xmlCalendar = df.newXMLGregorianCalendar( new GregorianCalendar() );
		}
		catch ( Exception ex )
		{
			throw new RuntimeException( ex );
		}
		
		return xmlCalendar;
	}

	public static XMLGregorianCalendar getCalendar( Calendar cal )
	{
		DatatypeFactory df = null;
		XMLGregorianCalendar xmlCalendar = null;
		
		try
		{
			df = DatatypeFactory.newInstance();
			xmlCalendar = df.newXMLGregorianCalendar( (GregorianCalendar) cal );
		}
		catch ( Exception ex )
		{
			throw new RuntimeException( ex );
		}
		
		return xmlCalendar;
	}

	public static Authentication getAuthenticationService( URL location )
		throws Exception
	{
		Authentication endpoint;
	
		Authentication_Service service =
			new Authentication_Service(
				location,
				new QName(
					"urn:Core.service.livelink.opentext.com",
					"Authentication" ) );
	
		endpoint = service.getBasicHttpBindingAuthentication();
		
		// force the service to use the specified URL, instead of the one defined in the WSDL
		( (BindingProvider) endpoint ).getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location.toExternalForm() );
		( (BindingProvider) endpoint ).getBinding().setHandlerChain(getHandlers());

		return endpoint;
	}
	
	
	public static com.opentext.ecm.services.authws.Authentication getAuthenticationServiceRCS(
			URL location) throws Exception {
		com.opentext.ecm.services.authws.Authentication endpoint;

		com.opentext.ecm.services.authws.AuthenticationService service = new com.opentext.ecm.services.authws.AuthenticationService(
				location, new QName("urn:authws.services.ecm.opentext.com",
						"AuthenticationService"));

		endpoint = service.getAuthenticationPort();

		// force the service to use the specified URL, instead of the one
		// defined in the WSDL
		((BindingProvider) endpoint).getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				location.toExternalForm());
		((BindingProvider) endpoint).getBinding()
				.setHandlerChain(getHandlers());

		return endpoint;
	}

	public static Header getContextIDHeader( SOAPHeader header, String contextID )
		throws Exception
	{
		SOAPHeaderElement contextIDElement;
	
	
		contextIDElement = header.addHeaderElement(
			new QName( CORE_NAMESPACE, "contextID" ) );
		contextIDElement.setPrefix( "" );
		contextIDElement.addTextNode( contextID );			
	
		return Headers.create(contextIDElement);
	}

	public static Header getOTAuthenticationHeader( SOAPHeader header, OTAuthentication otAuth )
		throws Exception
	{
		SOAPHeaderElement otAuthElement;
		SOAPElement authTokenElement;
		
	
		otAuthElement = header.addHeaderElement(new QName( ECM_API_NAMESPACE, "OTAuthentication" ) );
		otAuthElement.setPrefix( "" );
	
		authTokenElement = otAuthElement.addChildElement(
			new QName( ECM_API_NAMESPACE, "AuthenticationToken" ) );
		authTokenElement.setPrefix( "" );
		
		authTokenElement.addTextNode( otAuth.getAuthenticationToken() );
	
		return Headers.create(otAuthElement);
	}

	// create a new location string that references the given serviceName, using an existing
	// location as a guide
	public static String getServiceLocation( String location, String serviceName )
	{
		if ( location != null )
		{
			int index = location.lastIndexOf( "/" );
			int dotNetIndex = location.lastIndexOf( ".svc" );
	
			if ( index > 0 )
			{
				location = location.substring( 0, index ) + "/" + serviceName;
	
				if ( dotNetIndex > 0 )
				{
					location += ".svc";
				}
			}
		}
	
		return location;
	}

	public static Header getFileAttsHeader( SOAPHeader header, FileAtts fileAtts )
		throws Exception
	{
		SOAPHeaderElement fileAttsElement;
		SOAPElement createdDateElement;
		SOAPElement modifiedDateElement;
		SOAPElement fileSizeElement;
		SOAPElement fileNameElement;
		
	
		fileAttsElement = header.addHeaderElement(
			new QName( CORE_NAMESPACE, "fileAtts" ) );
		fileAttsElement.setPrefix( "" );
	
		createdDateElement = fileAttsElement.addChildElement(
			new QName( CORE_NAMESPACE, "CreatedDate" ) );
		createdDateElement.setPrefix( "" );
		createdDateElement.addTextNode( fileAtts.getCreatedDate().toString() );
	
		fileNameElement = fileAttsElement.addChildElement(
			new QName( CORE_NAMESPACE, "FileName" ) );
		fileNameElement.setPrefix( "" );
		fileNameElement.addTextNode( fileAtts.getFileName() );
	
		fileSizeElement = fileAttsElement.addChildElement(
			new QName( CORE_NAMESPACE, "FileSize" ) );
		fileSizeElement.setPrefix( "" );
		fileSizeElement.addTextNode( fileAtts.getFileSize().toString() );
	
		modifiedDateElement = fileAttsElement.addChildElement(
			new QName( CORE_NAMESPACE, "ModifiedDate" ) );
		modifiedDateElement.setPrefix( "" );
		modifiedDateElement.addTextNode( fileAtts.getModifiedDate().toString() );
	
		return Headers.create(fileAttsElement);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Handler> getHandlers() {
		if (handlers == null) {
			handlers = new ArrayList<Handler>();
		}

		return handlers;
	}
	
	@SuppressWarnings("unchecked")
	public static void addHandler(Handler handler) {
		if (handlers == null) {
			getHandlers();
		}
		if (!handlers.contains(handler)) {
			handlers.add(handler);
		}
	}

}
