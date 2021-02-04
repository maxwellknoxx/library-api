package com.knoxx.libraryapi.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knoxx.libraryapi.api.dto.LoanDTO;
import com.knoxx.libraryapi.api.dto.LoanFilterDTO;
import com.knoxx.libraryapi.api.dto.ReturnedBookDTO;
import com.knoxx.libraryapi.api.resource.LoanControllers;
import com.knoxx.libraryapi.api.service.BookService;
import com.knoxx.libraryapi.api.service.LoanService;
import com.knoxx.libraryapi.entity.Book;
import com.knoxx.libraryapi.entity.Loan;
import com.knoxx.libraryapi.exception.BusinessException;
import com.knoxx.libraryapi.service.LoanServiceTest;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("Test")
@WebMvcTest(controllers = LoanControllers.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

	static final String LOAN_API = "/api/loans";

	@Autowired
	MockMvc mvc;

	@MockBean
	BookService service;

	@MockBean
	LoanService loanService;

	@Test
	@DisplayName("Should loan a book - Controller")
	public void loanBookTest() throws Exception {

		LoanDTO dto = LoanDTO.builder().ISBN("123").email("customer@email.com").customer("Fulano").build();

		Book book = Book.builder().id(1l).isbn("123").build();

		Loan loan = createNewLoan("123", book);
		loan.setId(1l);

		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(service.getBookByISBN("123")).willReturn(Optional.of(book));

		BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isCreated()).andExpect(content().string("1"));

	}

	@Test
	@DisplayName("Should return error trying to loan invalid book - Controller")
	public void invalidISBNTest() throws Exception {

		LoanDTO dto = LoanDTO.builder().ISBN("123").customer("Fulano").build();

		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(service.getBookByISBN("123")).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Book not found for this ISBN"));

	}

	@Test
	@DisplayName("Should return error trying to loan already loaned book - Controller")
	public void loanedBookOnCreateLoanTest() throws Exception {

		LoanDTO dto = LoanDTO.builder().ISBN("123").customer("Fulano").build();

		String json = new ObjectMapper().writeValueAsString(dto);

		Book book = Book.builder().id(1l).isbn("123").build();

		BDDMockito.given(service.getBookByISBN("123")).willReturn(Optional.of(book));

		BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
				.willThrow(new BusinessException("Book already loaned"));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Book already loaned"));

	}

	@Test
	@DisplayName("Should return a book - Controller")
	public void returnBookTest() throws Exception {

		ReturnedBookDTO dto = ReturnedBookDTO.builder().returned(true).build();

		Loan loan = Loan.builder().id(1l).build();
		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

		String json = new ObjectMapper().writeValueAsString(dto);

		mvc.perform(MockMvcRequestBuilders.patch(LOAN_API.concat("/1")).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk());

		Mockito.verify(loanService, Mockito.times(1)).update(loan);

	}

	@Test
	@DisplayName("Should return 404 not found a book to return - Controller")
	public void returnNotFoundBookTest() throws Exception {

		ReturnedBookDTO dto = ReturnedBookDTO.builder().returned(true).build();

		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

		mvc.perform(MockMvcRequestBuilders.patch(LOAN_API.concat("/1")).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isNotFound());

	}

	@Test
	@DisplayName("Should find and filter loans - Controller")
	public void findLoansTest() throws Exception {
		Long id = 1l;
		Loan loan = LoanServiceTest.createLoan("123");
		loan.setId(id);

		BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Loan>(java.util.Arrays.asList(loan), PageRequest.of(0, 100), 1));

		String queryString = String.format("?isbn=%s&customer=%s&page=0&size100", loan.getBook().getIsbn(),
				loan.getCustomer());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(LOAN_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("content", Matchers.hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1)).andExpect(jsonPath("pageable.pageSize").value(20))
				.andExpect(jsonPath("pageable.pageNumber").value(0));

	}

	public Loan createNewLoan(String ISBN, Book book) {
		return Loan.builder().ISBN(ISBN).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
	}

}
