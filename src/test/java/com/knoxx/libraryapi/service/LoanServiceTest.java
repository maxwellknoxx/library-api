package com.knoxx.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.knoxx.libraryapi.api.dto.LoanFilterDTO;
import com.knoxx.libraryapi.api.service.LoanService;
import com.knoxx.libraryapi.entity.Book;
import com.knoxx.libraryapi.entity.Loan;
import com.knoxx.libraryapi.exception.BusinessException;
import com.knoxx.libraryapi.repository.LoanRepository;
import com.knoxx.libraryapi.service.impl.LoanServiceImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("Test")
public class LoanServiceTest {

	LoanService service;

	LoanRepository repository;

	@BeforeEach
	public void setUp() {
		this.repository = Mockito.mock(LoanRepository.class);
		this.service = new LoanServiceImpl(repository);
	}

	@Test
	@DisplayName("Should save a loan - Service")
	public void saveLoanTest() {

		String isbn = "123";

		Loan savingLoan = createLoan(isbn);

		Loan savedLoan = Loan.builder().id(1l).ISBN(isbn).customer("Fulano").book(savingLoan.getBook())
				.loanDate(LocalDate.now()).build();

		when(repository.existsByBookAndNotReturned(savingLoan.getBook())).thenReturn(false);

		when(repository.save(savingLoan)).thenReturn(savedLoan);

		Loan loan = service.save(savingLoan);

		assertThat(loan.getId()).isEqualTo(savedLoan.getId());
		assertThat(loan.getISBN()).isEqualTo(savedLoan.getISBN());
		assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
		assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());

	}

	@Test
	@DisplayName("Should throw exception when book already loaned - Service")
	public void loanedBookSaveTest() {

		String isbn = "123";

		Loan savingLoan = createLoan(isbn);

		when(repository.existsByBookAndNotReturned(savingLoan.getBook())).thenReturn(true);

		Throwable exception = Assertions.catchThrowable(() -> service.save(savingLoan));

		assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Book already loaned");

		verify(repository, Mockito.never()).save(savingLoan);

	}
	
	
	@Test
	@DisplayName("Should show loan details - Service")
	public void loanDetailsTest() {
		Long id = 1l;
		
		String isbn = "123";
		
		Loan loan = createLoan(isbn );
		loan.setId(id);
		
		Mockito.when(repository.findById(id) ).thenReturn(Optional.of(loan));
		
		Optional<Loan> result = service.getById(id);
		
		assertThat(result.isPresent()).isTrue();
		assertThat(result.get().getId()).isEqualTo(1l);
		assertThat(result.get().getBook()).isEqualTo(loan.getBook());
		assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
		assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate()); 
		
		
		Mockito.verify(repository).findById(id);
	}
	
	
	@Test
	@DisplayName("Should filter a loan - Service")
	public void findLoanTest() {
		
		LoanFilterDTO filter = LoanFilterDTO.builder().customer("Fulano").isbn("123").build();
		
		Loan loan = createLoan("123");
		loan.setId(1l);

		PageRequest pageRequest = PageRequest.of(0, 10);

		List<Loan> list = java.util.Arrays.asList(loan);

		Page<Loan> page = new PageImpl<Loan>(list, pageRequest, list.size());

		when(repository.findByBookIsbnOrCustomer(Mockito.anyString(),
				Mockito.anyString(),
				Mockito.any(PageRequest.class))).thenReturn(page);

		Page<Loan> result = service.find(filter, pageRequest);

		assertThat(result.getNumberOfElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(list);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);

	}
	
	
	
	@Test
	@DisplayName("Should update a loan - Service")
	public void updateLoanTest() {
		Long id = 1l;
		
		Loan loan = createLoan("123");
		loan.setId(id);
		loan.setReturned(true);
		
		Mockito.when( repository.save(loan) ).thenReturn(loan);
		
		Loan updatedLoan = service.update(loan);
		
		assertThat(updatedLoan.getReturned()).isTrue();
		
		Mockito.verify(repository).save(loan);
		
	}

	
	
	public static Loan createLoan(String isbn) {
		Book book = Book.builder().id(1l).author("Fulano").title("As Aventuras").isbn(isbn).build();

		return Loan.builder().ISBN(isbn).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
	}

}
