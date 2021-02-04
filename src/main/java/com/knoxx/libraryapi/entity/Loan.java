package com.knoxx.libraryapi.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Loan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100)
	private String ISBN;

	@Column
	private String customer;
	
	@Column(name = "customer_email")
	private String customerEmail;

	@JoinColumn(name = "id_book")
	@ManyToOne
	private Book book;

	@Column(name = "loan_date")
	private LocalDate loanDate;

	@Column
	private Boolean returned;

}
