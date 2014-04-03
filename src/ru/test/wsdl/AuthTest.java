package ru.test.wsdl;

import com.opentext.ecm.api.OTAuthentication;
import com.opentext.livelink.service.core.Attachment;
import com.opentext.livelink.service.docman.DocumentManagement;
import com.opentext.livelink.service.webservice.CWSAuthenticationHandler;
import com.opentext.livelink.service.webservice.WebServiceUtil;

import javax.xml.ws.BindingProvider;
import java.io.FileOutputStream;
import java.net.URL;

//import com.sun.xml.internal.ws.api.message.Header;

/**
 * User: a.arzamastsev Date: 01.04.14 Time: 13:36
 * http://srv-ecm-llr8.itps.local:8080/les-services/services/Authentication?wsdl - сервис аутентификации
 * http://srv-ecm-llr8.itps.local:8080/les-services/services/DocumentManagement?wsdl - сервис управления документами
 */
public class AuthTest {
    public static void main(String[] argv) throws Exception {


        URL authLocation = new URL("http://srv-ecm-llr8.itps.local/les-services/Authentication.svc?wsdl");
        String cwsServiceUrl = "http://srv-ecm-llr8.itps.local/les-services/Authentication.svc";
//        String cwsServiceUrl = "http://srv-ecm-llr8.itps.local:8080/les-services/services/Authentication";
        String token = WebServiceUtil.getAuthenticationToken(authLocation, username, password);

        WebServiceUtil.addHandler(new CWSAuthenticationHandler(new URL(cwsServiceUrl), username, password));

        OTAuthentication fOTAuth = new OTAuthentication();
        fOTAuth.setAuthenticationToken(token);

        DocumentManagement docManService = WebServiceUtil.getDMService(
                new URL(WebServiceUtil.getServiceLocation(cwsServiceUrl, "DocumentManagement")),
                token);
        ((BindingProvider) docManService).getBinding().setHandlerChain(WebServiceUtil.getHandlers());

        Attachment attach = docManService.getVersionContents(1504892, 0);

        FileOutputStream fo = new FileOutputStream("c:\\1.docx");
        fo.write(attach.getContents());
        fo.close();

        System.out.println("Done!");

    }

    private static final String username = "aav";
    private static final String password = "Qwerty1";
}