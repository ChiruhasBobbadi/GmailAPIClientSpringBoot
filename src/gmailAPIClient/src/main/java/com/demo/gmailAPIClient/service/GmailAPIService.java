package com.demo.gmailAPIClient.service;

import com.demo.gmailAPIClient.model.GmailCredential;
import com.demo.gmailAPIClient.model.GoogleTokenResponse;
import com.demo.gmailAPIClient.model.Resume;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;

import lombok.SneakyThrows;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Service
public class GmailAPIService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final HttpTransport httpTransport;
    private GmailCredential gmailCredential;
    @Value("${spring.google.client-id}")
    private String clientId;
    @Value("${spring.google.client-secret}")
    private String secretKey;
    @Value("${spring.google.refresh-token}")
    private String refreshToken;

    @Value("${FROM_EMAIL}")
    private String fromEmail;

    @SneakyThrows
    public GmailAPIService() {

        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        this.gmailCredential = new GmailCredential(
                clientId,
                secretKey,
                refreshToken,
                null,
                null,
                fromEmail
        );

    }

    public boolean sendMessage(
            String toEmail,
            String subject,
            String body, Resume resume) throws MessagingException, IOException {

        refreshAccessToken();


        File file;

        switch (resume){
            case FE:
                file = new File("docs/chiruhas_resume_fe.pdf");
                break;
            case SDE:
                file = new File("docs/chiruhas_resume_sde.pdf");
                break;
            case MOBILE:
                file = new File("docs/chiruhas_resume_mobile.pdf");
                break;
            default:
                file = new File("docs/chiruhas_resume_sde.pdf");
                break;
        }

        //MimeMessage mime = createEmail(toEmail, fromEmail, subject, body, multipartFile);
        Message message = createMessage(fromEmail,toEmail,subject,body,file);

        return createGmail()
                .users()
                .messages()
                .send(fromEmail, message)
                .execute()
                .getLabelIds()
                .contains("SENT");


    }

    private Gmail createGmail() {

        Credential credential = authorize();

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .build();

    }


    public Message createMessage(String fromEmailAddress, String toEmailAddress, String messageSubject, String bodyText, File file)
                throws MessagingException, IOException {


            // Encode as MIME message
            MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
            email.setFrom(new InternetAddress(fromEmailAddress));
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(toEmailAddress));
            email.setSubject(messageSubject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(bodyText, "text/html");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            mimeBodyPart = new MimeBodyPart();

            DataSource source = new FileDataSource(file);
            mimeBodyPart.setDataHandler(new DataHandler(source));
            mimeBodyPart.setFileName(file.getName());
            multipart.addBodyPart(mimeBodyPart);
            email.setContent(multipart);

            // Encode and wrap the MIME message into a gmail message
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
            Message message = new Message();
            message.setRaw(encodedEmail);

            return message;


        }


    private Credential authorize() {

        try {

            TokenResponse tokenResponse = refreshAccessToken();

            return new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(
                    tokenResponse);

        } catch (Exception e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Not able to process request.");

        }

    }

    private TokenResponse refreshAccessToken() {

        RestTemplate restTemplate = new RestTemplate();

        GmailCredential gmailCredentialsDto = new GmailCredential(
                clientId,
                secretKey,
                refreshToken,
                "refresh_token",
                null,
                null
        );

        HttpEntity<GmailCredential> entity = new HttpEntity(gmailCredentialsDto);

        try {

            GoogleTokenResponse response = restTemplate.postForObject(
                    "https://www.googleapis.com/oauth2/v4/token",
                    entity,
                    GoogleTokenResponse.class);

            gmailCredential = new GmailCredential(
                    clientId,
                    secretKey,
                    refreshToken,
                    null,
                    response.getAccessToken(),
                    fromEmail
            );

            return response;

        } catch (Exception e) {

            e.printStackTrace();

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Not able to process request.");

        }
    }

}
