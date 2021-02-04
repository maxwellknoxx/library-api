package com.knoxx.libraryapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.knoxx.libraryapi.entity.Book;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("tests")
@DataJpaTest
public class BookRepositoryTest {
	
	@Autowired
	BookRepository repository;
	
	@Autowired
	TestEntityManager entityManager;
	
	@Test
	@DisplayName("Should return true when ISBN already exists - Repository")
	public void returnTrueWhenISBNExist() {
		String isbn = "123";
		
		Book book = createNewBook(isbn);
		
		entityManager.persist(book);
		
		boolean exists = repository.existsByIsbn(isbn);
		
		assertThat(exists).isTrue();
	}
	
	
	@Test
	@DisplayName("Should return true when ISBN does not exist - Repository")
	public void returnFalseWhenISBNDoesNotExist() {
		String isbn = "123";
		
		boolean exist = repository.existsByIsbn(isbn);
		
		assertThat(exist).isFalse();
	}
	
	
	@Test
	@DisplayName("Should find book by Id - Repository")
	public void findByIdTest() {
		
		Book book = createNewBook("123");
		
		entityManager.persist(book);
		
		Optional<Book> foundBook = repository.findById(book.getId());
		
		assertThat(foundBook.isPresent()).isTrue();
		
	}
	
	
	@Test
	@DisplayName("Should save a book - Repository")
	public void saveBookTest() {
		
		Book book =  createNewBook("123");
		
		Book savedBook = repository.save(book);
		
		assertThat(savedBook.getId()).isNotNull();
		
	}
	
	@Test
	@DisplayName("Should delete a book - Repository")
	public void deleteBookTest() {
		
		Book book =  createNewBook("123");
		entityManager.persist(book);
		
		Book foundBook = entityManager.find(Book.class, book.getId());
		
		repository.delete(foundBook);
		
		Book notFoundBook = entityManager.find(Book.class, book.getId());
		
		assertThat(notFoundBook).isNull();
		
	}
	
	
	public Book createNewBook(String isbn) {
		return Book.builder().author("Autor").title("As Aventuras").isbn(isbn).build();
	}
	

}
