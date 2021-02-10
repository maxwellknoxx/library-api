package com.knoxx.libraryapi.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.knoxx.libraryapi.api.dto.BookDTO;
import com.knoxx.libraryapi.api.resource.BookController;
import com.knoxx.libraryapi.api.service.BookService;
import com.knoxx.libraryapi.api.service.LoanService;
import com.knoxx.libraryapi.entity.Book;
import com.knoxx.libraryapi.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

	static String BOOK_API = "/api/books";

	@Autowired
	MockMvc mvc;
 
	@MockBean
	BookService service;
	
	@MockBean
	LoanService loanService;

	@Test
	@DisplayName("Should create a new book successfully - Controller")
	public void createBookControllerTest() throws Exception {

		BookDTO dto = createNewBook();

		Book savedBook = Book.builder().id(10l).title("As Aventuras").author("Arthur").isbn("123456").build();

		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

		String json = new ObjectMapper().writeValueAsString(dto);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isCreated())
				.andExpect(jsonPath("id").isNotEmpty())
				.andExpect(jsonPath("title").value(dto.getTitle()))
				.andExpect(jsonPath("author").value(dto.getAuthor()))
				.andExpect(jsonPath("isbn").value(dto.getISBN()));
	}

	@Test
	@DisplayName("Should throw an exception due invalid book data - Controller")
	public void createInvalidBookControllerTest() throws Exception {

		String json = new ObjectMapper().writeValueAsString(new BookDTO());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", Matchers.hasSize(3)));
	}

	@Test
	@DisplayName("Should not allow to create a book with duplicated ISBN - Controller")
	public void createBookWithDuplicatedISBNTest() throws Exception {

		BookDTO dto = createNewBook();
		String json = new ObjectMapper().writeValueAsString(dto);

		String errorMessage = "ISBN Ja cadastrado!";
		BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(errorMessage));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value(errorMessage));

	}

	@Test
	@DisplayName("Should get book's details - Controller")
	public void getBookDetailsTest() throws Exception {

		Long id = 1l;

		Book book = Book.builder().id(id).author(createNewBook().getAuthor()).title(createNewBook().getTitle())
				.isbn(createNewBook().getISBN()).build();

		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk())
				.andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(createNewBook().getTitle()))
				.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
				.andExpect(jsonPath("isbn").value(createNewBook().getISBN()));

	}

	@Test
	@DisplayName("Should return not found - Controller")
	public void bookNotFoundTest() throws Exception {

		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + 58))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isNotFound());

	}

	@Test
	@DisplayName("Should delete a book - Controller")
	public void deleteBookTest() throws Exception {

		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1l).build()));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Should throw exception when trying to delete a book - Controller")
	public void exceptionDeletingBookTest() throws Exception {
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Should update a book - Controller")
	public void updateBookTest() throws Exception {
		Long id = 1l;

		Book book = Book.builder().id(1l).author("Joao do pulo").title("Como pular").isbn("666").build();

		String json = new ObjectMapper().writeValueAsString(book);

		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

		Book livroAtualizado = Book.builder().id(id).author("Joao do despulo").title("Como nao pular").isbn("666")
				.build();
		BDDMockito.given(service.update(book)).willReturn(livroAtualizado);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isOk())
				.andExpect(jsonPath("id").value(1l))
				.andExpect(jsonPath("author").value("Joao do despulo"))
				.andExpect(jsonPath("title").value("Como nao pular"))
				.andExpect(jsonPath("isbn").value("666"));

	}

	@Test
	@DisplayName("Should throw exception trying to update a empty book - Controller")
	public void updateEmptyBookTest() throws Exception {

		String json = new ObjectMapper().writeValueAsString(createNewBook());

		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isNotFound());

	}

	@Test
	@DisplayName("Should find and filter books")
	public void findBooksTest() throws Exception {
		Long id = 1l;

		Book book = Book.builder().id(id).author(createNewBook().getAuthor()).title(createNewBook().getTitle())
				.isbn(createNewBook().getISBN()).build();

		BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Book>(java.util.Arrays.asList(book), PageRequest.of(0, 100), 1));

		String queryString = String.format("?title=%s&author=%s&page=0&size100", book.getTitle(), book.getAuthor());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request).andExpect(status().isOk())
		.andExpect(jsonPath("content", Matchers.hasSize(1)))
		.andExpect(jsonPath("totalElements").value(1))
		.andExpect(jsonPath("pageable.pageSize").value(20))
		.andExpect(jsonPath("pageable.pageNumber").value(0));
		
	}
	
	
	
	

	private BookDTO createNewBook() {
		return BookDTO.builder().id(1l).title("As Aventuras").author("Arthur").ISBN("123456").build();
	}

	
}
