package com.knoxx.libraryapi.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.knoxx.libraryapi.api.service.EmailService;
import com.knoxx.libraryapi.api.service.LoanService;
import com.knoxx.libraryapi.entity.Loan;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl {
	
	private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";
	
	private final LoanService loanService;
	
	private final EmailService emailService;
	
	@Value("${application.mail.lateloans.message}")
	private String message = "";
	
	@Scheduled(cron = CRON_LATE_LOANS)
	public void sendEmailToLateLoan() {
		List<Loan> allLateLoans = loanService.getAllLateLoans();
		List<String> mailsList = allLateLoans.stream().map(loan -> loan.getCustomerEmail()).collect(Collectors.toList());
		
		
		emailService.sendEmails(message, mailsList);
	}
	

}
