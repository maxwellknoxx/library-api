package com.knoxx.libraryapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.knoxx.libraryapi.entity.Book;
import com.knoxx.libraryapi.entity.Loan;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("Test")
@DataJpaTest
public class LoanRepositoryTest {

	@Autowired
	private LoanRepository repository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	@DisplayName("Should verify if the book is still loaned - Repository")
	public void existsByBookAndNotReturnedTest() {

		Loan loan = createAndPersisteLoan(LocalDate.now());
		Book book = loan.getBook();

		boolean exists = repository.existsByBookAndNotReturned(book);

		assertThat(exists).isTrue();

	}

	@Test
	@DisplayName("Should find loan by book isbn or customer - Repository")
	public void findByBookIsbnOrCustomerTest() {
		Loan loan = createAndPersisteLoan(LocalDate.now());

		Page<Loan> result = repository.findByBookIsbnOrCustomer(loan.getISBN(), loan.getCustomer(),
				PageRequest.of(0, 10));
		
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getTotalElements()).isEqualTo(1);

	}

	private Loan createAndPersisteLoan(LocalDate loanDate) {
		Book book = Book.builder().author("Autor").title("As Aventuras").isbn("123").build();

		entityManager.persist(book);

		Loan loan = Loan.builder().customer("Fulano").loanDate(loanDate).book(book).build();

		entityManager.persist(loan);
		return loan;
	}
	
	
	@Test
	@DisplayName("Should get loan where loan date is less or equals to 3 days and not returned - Repository")
	public void findByLoanDateLessThanAndNotReturned() {
		Loan loan = createAndPersisteLoan(LocalDate.now().minusDays(5));
		
		List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
		
		
		assertThat(result).hasSize(1).contains(loan);
	}
	
	@Test
	@DisplayName("Should return empty when there is no delayed loan - Repository")
	public void notFindByLoanDateLessThanAndNotReturned() {
		createAndPersisteLoan(LocalDate.now());
		
		List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
		
		
		assertThat(result).isEmpty();
	}

}
