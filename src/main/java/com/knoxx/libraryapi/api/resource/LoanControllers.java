package com.knoxx.libraryapi.api.resource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.knoxx.libraryapi.api.dto.BookDTO;
import com.knoxx.libraryapi.api.dto.LoanDTO;
import com.knoxx.libraryapi.api.dto.LoanFilterDTO;
import com.knoxx.libraryapi.api.dto.ReturnedBookDTO;
import com.knoxx.libraryapi.api.service.BookService;
import com.knoxx.libraryapi.api.service.LoanService;
import com.knoxx.libraryapi.entity.Book;
import com.knoxx.libraryapi.entity.Loan;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanControllers {

	private final BookService bookService;

	private final LoanService loanService;
	
	private final ModelMapper modelMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody LoanDTO dto) {
		Book book = bookService.getBookByISBN(dto.getISBN())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for this ISBN"));

		Loan entity = Loan.builder().ISBN(dto.getISBN()).customer(dto.getCustomer()).book(book)
				.loanDate(LocalDate.now()).build();

		entity = loanService.save(entity);

		return entity.getId();
	}

	@PatchMapping("{id}")
	public void returnBook(@PathVariable Long id, @RequestBody ReturnedBookDTO dto) {
		Loan loan = loanService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		loan.setReturned(dto.getReturned());
		loanService.update(loan);
	}
	
	
	@GetMapping
	public Page<LoanDTO> find(LoanFilterDTO filter, Pageable pageable){
		Page<Loan> result = loanService.find(filter, pageable);
		List<LoanDTO> loans = result
		.getContent()
		.stream()
		.map(entity ->  {
			Book book = entity.getBook();
			BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
			LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
			loanDTO.setBook(bookDTO);
			return loanDTO;
		}).collect(Collectors.toList());
		return new PageImpl<LoanDTO>(loans, pageable, result.getTotalElements());
	}
	

}
