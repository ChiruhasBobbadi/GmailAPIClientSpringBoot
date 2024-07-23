package com.demo.gmailAPIClient.service;

import com.demo.gmailAPIClient.model.EmailRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@Service
public class ContactService {

    private final GmailAPIService gmailAPIService;

    public ContactService(GmailAPIService gmailAPIService) {
        this.gmailAPIService = gmailAPIService;
    }



    public void sendEmails(List<EmailRequest> requestList, String subject, String body){

        System.out.println("hello");

        for(EmailRequest request : requestList){
            try {

                gmailAPIService.sendMessage(
                        request.getEmail(),
                        subject,
                        "Hi "+request.getName()+","+"\n"+body
                );

            } catch (MessagingException | IOException e) {
                System.out.println("in contact service " + e.toString());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not able to process request.");
            }
        }




    }

}
