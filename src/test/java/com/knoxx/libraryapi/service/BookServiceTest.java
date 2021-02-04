package com.knoxx.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.knoxx.libraryapi.api.service.BookService;
import com.knoxx.libraryapi.entity.Book;
import com.knoxx.libraryapi.exception.BusinessException;
import com.knoxx.libraryapi.repository.BookRepository;
import com.knoxx.libraryapi.service.impl.BookServiceImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

	BookService service;

	private BookRepository repository;

	@BeforeEach
	public void setUp() {
		this.repository = Mockito.mock(BookRepository.class);
		this.service = new BookServiceImpl(repository);
	}

	@Test
	@DisplayName("Should save a book - Service")
	public void saveBookServiceTest() {

		Book book = createValidBook();
		when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

		when(repository.save(book))
				.thenReturn(Book.builder().id(1l).title("As Aventuras").author("Fulano").isbn("123").build());

		Book savedBook = service.save(book);

		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo("123");
		assertThat(savedBook.getTitle()).isEqualTo("As Aventuras");
		assertThat(savedBook.getAuthor()).isEqualTo("Fulano");

	}

	@Test
	@DisplayName("Should throw an exception when trying to create a book with duplicated ISBN - Service")
	public void shouldNotSaveABookWithDuplicatedISBN() {

		Book book = createValidBook();
		when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

		Throwable exception = Assertions.catchThrowable(() -> service.save(book));
		assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("ISBN Ja cadastrado!");

		verify(repository, Mockito.never()).save(book);
	}

	@Test
	@DisplayName("Should get book by id - Service")
	public void getByIdTest() {

		Long id = 1l;

		Book book = createValidBook();

		book.setId(id);

		when(repository.findById(id)).thenReturn(Optional.of(book));

		Optional<Book> foundBook = service.getById(id);

		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(1l);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());

	}

	@Test
	@DisplayName("Should return empty when not find the book by id - Service")
	public void bookNotFoundByIdTest() {

		Long id = 1l;

		when(repository.findById(id)).thenReturn(Optional.empty());

		Optional<Book> book = service.getById(id);

		assertThat(book.isPresent()).isFalse();

	}

	@Test
	@DisplayName("Should delete a book - Service")
	public void deleteBookTest() {

		Book book = createValidBook();

		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

		verify(repository, Mockito.times(1)).delete(book);

	}

	@Test
	@DisplayName("Should throw an exception when the book is null - Delete - Service")
	public void exceptionDeleteBookTest() {

		Throwable exception = Assertions.catchThrowable(() -> service.delete(new Book()));
		assertThat(exception).isInstanceOf(IllegalArgumentException.class).hasMessage("Book Id cant be null.");

		verify(repository, Mockito.never()).delete(new Book());

	}

	@Test
	@DisplayName("Should update a book - Service")
	public void updateBookTest() {

		Book book = createValidBook();

		Book updatedBook = Book.builder().id(1L).author("Pipoca").title("As Aventuras").isbn("123").build();

		when(repository.save(book)).thenReturn(updatedBook);

		Book afterUpdateBook = service.update(book);

		assertThat(afterUpdateBook.getId()).isEqualTo(1l);
		assertThat(afterUpdateBook.getAuthor()).isEqualTo("Pipoca");
		assertThat(afterUpdateBook.getTitle()).isEqualTo(book.getTitle());
		assertThat(afterUpdateBook.getIsbn()).isEqualTo(book.getIsbn());
	}

	@Test
	@DisplayName("Should throw an exception when the book is null - Update - Service")
	public void exceptionUpdateBookTest() {

		Throwable exception = Assertions.catchThrowable(() -> service.update(new Book()));
		assertThat(exception).isInstanceOf(IllegalArgumentException.class).hasMessage("Book Id cant be null.");

		verify(repository, Mockito.never()).save(new Book());

	}

	@Test
	@DisplayName("Should filter a book - Service")
	public void findBookTest() {
		Book book = createValidBook();

		PageRequest pageRequest = PageRequest.of(0, 10);

		List<Book> list = java.util.Arrays.asList(book);

		Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);

		when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

		Page<Book> result = service.find(book, pageRequest);

		assertThat(result.getNumberOfElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(list);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);

	}

	@Test
	@DisplayName("Should return a book by ISBN")
	public void getBookByISBNTest() {

		String isbn = "123";

		when(repository.findByIsbn(isbn)).thenReturn(
				Optional.of(Book.builder().id(1L).author("Fulano").title("As Aventuras").isbn("123").build()));
		
		Optional<Book> book = service.getBookByISBN(isbn);
		
		assertThat(book.isPresent()).isTrue();
		assertThat(book.get().getId()).isEqualTo(1l);
		assertThat(book.get().getIsbn()).isEqualTo("123");
		
		verify(repository, Mockito.times(1)).findByIsbn(isbn);

	}

	private Book createValidBook() {
		return Book.builder().id(1L).author("Fulano").title("As Aventuras").isbn("123").build();
	}

}
