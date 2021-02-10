package com.knoxx.libraryapi.api.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.knoxx.libraryapi.api.dto.BookDTO;
import com.knoxx.libraryapi.api.dto.LoanDTO;
import com.knoxx.libraryapi.api.service.BookService;
import com.knoxx.libraryapi.api.service.LoanService;
import com.knoxx.libraryapi.entity.Book;
import com.knoxx.libraryapi.entity.Loan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/books")
@Api("Book API")
@Slf4j
public class BookController {

	BookService service;
	
	ModelMapper modelMapper;
	
	LoanService loanService;
	
	
	 public BookController(BookService service, LoanService loanService, ModelMapper modelMapper) {
		this.service = service;
		this.loanService = loanService;
		this.modelMapper = modelMapper;
	}
	

	@GetMapping
	@ApiOperation("Finds a book by paramters")
	public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
		Book filter = modelMapper.map(dto, Book.class);

		Page<Book> result = service.find(filter, pageRequest);

		List<BookDTO> list = result.getContent().stream().map(entity -> modelMapper.map(entity, BookDTO.class))
				.collect(Collectors.toList());

		return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
	}

	@GetMapping("{id}")
	@ApiOperation("Gets a book details by id")
	public BookDTO get(@PathVariable Long id) {
		log.info("Getting details for book id {}", id);
		return service.getById(id).map(book -> modelMapper.map(book, BookDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@PutMapping("{id}")
	@ApiOperation("Updates a book by id")
	public BookDTO updateBook(@PathVariable Long id, @RequestBody @Valid BookDTO dto) {
		log.info("Updating book id {}", id);
		return service.getById(id).map(book -> {
			book.setAuthor(dto.getAuthor());
			book.setTitle(book.getTitle());
			book = service.update(book);
			return modelMapper.map(book, BookDTO.class);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation("Deletes a book by id")
	@ApiResponses({
		@ApiResponse(code = 204, message = "Book succesfully deleted")
	})
	public void delete(@PathVariable Long id) {
		log.info("Deleting book id {}", id);
		Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		service.delete(book);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Creates a book")
	public BookDTO create(@RequestBody @Valid BookDTO dto) {
		log.info("creating a book for isbn {}", dto.getISBN());
		Book entity = modelMapper.map(dto, Book.class);
		entity = service.save(entity);
		return modelMapper.map(entity, BookDTO.class);
	}
	
	
	
	@GetMapping("{id}/loans")
	@ApiOperation("Gets all loans from a book")
	public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable){
		Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Page<Loan> result = loanService.getLoansByBook(book, pageable);
		List<LoanDTO> list = result.getContent()
		.stream()
		.map(entity -> {
			Book loanBook = entity.getBook();
			BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
			LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
			loanDTO.setBook(bookDTO);
			return loanDTO;
		}).collect(Collectors.toList());
		return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
	}

}
