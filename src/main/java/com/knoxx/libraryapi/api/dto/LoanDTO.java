package com.knoxx.libraryapi.api.dto;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
	
	private Long id;
	
	@NotEmpty
	private String ISBN;
	
	@NotEmpty
	private String customer;
	
	@NotEmpty
	private String email;
	
	private BookDTO book;

}