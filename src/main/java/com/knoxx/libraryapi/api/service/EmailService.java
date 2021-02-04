package com.knoxx.libraryapi.api.service;

import java.util.List;

public interface EmailService {

	public void sendEmails(String message, List<String> mailsList);

}
