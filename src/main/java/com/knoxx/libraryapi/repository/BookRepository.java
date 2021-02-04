package com.knoxx.libraryapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.knoxx.libraryapi.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
	
	boolean existsByIsbn(String isbn);

	Optional<Book> findByIsbn(String isbn);

}
