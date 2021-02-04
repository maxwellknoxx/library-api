package com.knoxx.libraryapi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.knoxx.libraryapi.api.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
	
	private final JavaMailSender javaMailSender;
	
	@Value("${application.mail.default-sender}")
	private String sender;

	@Override
	public void sendEmails(String message, List<String> mailsList) {
		String[] mails = mailsList.toArray(new String[mailsList.size()]);
		
		
		SimpleMailMessage mailMessage =  new SimpleMailMessage();
		mailMessage.setFrom(sender); 
		mailMessage.setSubject("Delayed loan");
		mailMessage.setText(message);
		mailMessage.setTo(mails);
		
		javaMailSender.send(mailMessage);
	}

}
