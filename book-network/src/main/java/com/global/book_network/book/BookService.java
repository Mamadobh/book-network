package com.global.book_network.book;

import com.global.book_network.common.PageResponse;
import com.global.book_network.exception.OperationNotPermittedException;
import com.global.book_network.file.FileStorageService;
import com.global.book_network.history.BookTransactionHistory;
import com.global.book_network.history.BookTransactionHistoryRepo;
import com.global.book_network.user.User;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.global.book_network.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
@Log4j2
public class BookService {
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepo bookTransactionHistroyRepo;
    private final FileStorageService fileStorageService;


    public Integer save(BookDto bookDto, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.fromDtoToBook(bookDto);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer id) {
        return bookRepository.findById(id).map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No book found with Id : " + id));
    }

    public PageResponse<BookResponse> findAllBook(int pageNumber, int pageSize, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable page = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(page, user.getId());
        List<BookResponse> bookResponse = books.stream().map(bookMapper::toBookResponse).toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast());



    }

    public PageResponse<BookResponse> findBooksByOwner(int pageNumber, int pageSize, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable page = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(withOwnerId(user.getId()), page);
        List<BookResponse> bookResponse = books.stream().map(bookMapper::toBookResponse).toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast());
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int pageNumber, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable page = PageRequest.of(pageNumber, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistroyRepo.findAllBorrowedBooks(user.getId(), page);
        List<BorrowedBookResponse> borrowedBookRsesponse = allBorrowedBooks.stream().map(bookMapper::toBorrowedBookResponse).toList();

        return new PageResponse<>(
                borrowedBookRsesponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast());
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int pageNumber, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable page = PageRequest.of(pageNumber, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistroyRepo.findAllReturnedBooks(user.getId(), page);
        List<BorrowedBookResponse> borrowedBookRsesponse = allBorrowedBooks.stream().map(bookMapper::toBorrowedBookResponse).toList();

        return new PageResponse<>(
                borrowedBookRsesponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast());

    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("book not found to be updates with id " + bookId));

        if (!book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("book not found to be updates with id " + bookId));

        if (!book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books Archived status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("No book found with id " + bookId));
        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("The request borrowed book cannot be borrowed since it is archived or not shareable");
        }

        User user = (User) connectedUser.getPrincipal();
        if (book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow you own book");
        }

        final boolean isAlreadyBorrowed = bookTransactionHistroyRepo.isAlreadyBorrowedByUser(bookId);
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("The request book is already borrowed");
        }
        BookTransactionHistory history = BookTransactionHistory.builder()
                .book(book)
                .user(user)
                .returned(false)
                .returnedApproved(false)
                .build();
        return bookTransactionHistroyRepo.save(history).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found  with id " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new EntityNotFoundException("cannot return  book archived or not shareable");
        }
        User user = (User) connectedUser.getPrincipal();
        if (book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or returned you own book");
        }
        BookTransactionHistory history = bookTransactionHistroyRepo.findTransactionHistoryByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> {
                    throw new OperationNotPermittedException("you did not borrow this book with id  " + bookId);
                });
        history.setReturned(true);
        bookTransactionHistroyRepo.save(history);
        return history.getId();
    }

    public Integer approveReturnBorrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found  with id " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new EntityNotFoundException("cannot approved return  book archived or not shareable");
        }
        User user = (User) connectedUser.getPrincipal();
        if (!book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You cannot approved returned a book you do not own ");
        }
        BookTransactionHistory history = bookTransactionHistroyRepo.findTransactionHistoryByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> {
                    throw new OperationNotPermittedException("The book not returned yet . So you can not approved the return " + bookId);
                });
        history.setReturnedApproved(true);
        bookTransactionHistroyRepo.save(history);
        return history.getId();
    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found  with id " + bookId));
        User user = (User) connectedUser.getPrincipal();
        var bookCover = fileStorageService.saveFile(file, user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);


    }
}
