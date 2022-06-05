package be.teletask.onvif;


import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Random;

/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class OnvifXMLBuilder {
    //Constants
    public static final String TAG = OnvifXMLBuilder.class.getSimpleName();

    //Attributes
    public static String getSoapHeader(Credentials cred) {
        String nonce = null;
        String created = null;
        String digest = null;

        if (cred != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");

                byte[] bytes = new byte[20];
                new Random().nextBytes(bytes);
                nonce = Base64.getEncoder().encodeToString(bytes);
                created = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                byte[] createdByteArray = created.getBytes(StandardCharsets.UTF_8);
                byte[] passwordByteArray = cred.getPassword().getBytes(StandardCharsets.UTF_8);
                byte[] c = new byte[bytes.length + createdByteArray.length + passwordByteArray.length];
                System.arraycopy(bytes, 0, c, 0, bytes.length);
                System.arraycopy(createdByteArray, 0, c, bytes.length, createdByteArray.length);
                System.arraycopy(passwordByteArray, 0, c, bytes.length + createdByteArray.length, passwordByteArray.length);

                md.update(c);
                digest = Base64.getEncoder().encodeToString(md.digest());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" soap:mustUnderstand=\"true\" " +
                "xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" >" +
                (digest == null ? "" : "<soap:Header><Security soap:mustUnderstand=\"1\" xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"><UsernameToken><Username>" + cred.getUserName() + "</Username><Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + digest + "</Password>" +
                        "<Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">" + nonce + "</Nonce>" +
                        "<Created xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" + created + "</Created>" +
                        "</UsernameToken></Security></soap:Header>") +

                "<soap:Body>";
    }

    public static String getEnvelopeEnd() {
        return "</soap:Body></soap:Envelope>";
    }

    public static String getDiscoverySoapHeader(String uuid) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">\n" +
                "  <soap:Header>\n" +
                "    <a:Action soap:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</a:Action>\n" +
                "    <a:MessageID>uuid:" + uuid + "</a:MessageID>\n" +
                "    <a:ReplyTo>\n" +
                "      <a:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</a:Address>\n" +
                "    </a:ReplyTo>\n" +
                "    <a:To soap:mustUnderstand=\"1\">urn:schemas-xmlsoap-org:ws:2005:04:discovery</a:To>\n" +
                "  </soap:Header>\n" +
                "  <soap:Body>\n";
    }

    public static String getDiscoverySoapBody(String type) {
        return "<Probe xmlns=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\">" +
                "<d:Types xmlns:d=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\" xmlns:dp0=\"http://www.onvif.org/ver10/network/wsdl\">dp0:" + type + "</d:Types>\n" +
                "</Probe>";
    }

}
