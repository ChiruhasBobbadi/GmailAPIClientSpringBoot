package com.demo.gmailAPIClient.controller;

import com.demo.gmailAPIClient.model.EmailRequest;
import com.demo.gmailAPIClient.model.GenericRequest;
import com.demo.gmailAPIClient.model.Resume;
import com.demo.gmailAPIClient.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/contact")
public class ContactController {

  private final ContactService contactService;



  @PostMapping(path = "/sde")
  public void multiEmailRequest(@RequestBody GenericRequest request){

    contactService.sendEmails(request.getRequestList(),request.getSubject(),request.getBody(), Resume.SDE);

  }

  @PostMapping(path = "/fe")
  public void frontEndRequest(@RequestBody GenericRequest request){

    contactService.sendEmails(request.getRequestList(),request.getSubject(),request.getBody(), Resume.FE);

  }



  @PostMapping(path = "/mobile")
  public void mobileRequest(@RequestBody GenericRequest request){
      contactService.sendEmails(request.getRequestList(),request.getSubject(),request.getBody(), Resume.MOBILE);
  }

}
