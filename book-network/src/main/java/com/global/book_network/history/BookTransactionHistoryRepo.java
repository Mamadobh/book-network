package com.global.book_network.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookTransactionHistoryRepo extends JpaRepository<BookTransactionHistory, Integer> {
    @Query("""
                 SELECT history
                 FROM BookTransactionHistory history
                 WHERE history.user.id = :currentUserId
            """)
    Page<BookTransactionHistory> findAllBorrowedBooks(@Param(value = "currentUserId") Integer userId, Pageable page);


    @Query("""
                 SELECT history
                 FROM BookTransactionHistory history
                 WHERE history.book.owner.id= :currentUserId
            """)
    Page<BookTransactionHistory> findAllReturnedBooks(@Param(value = "currentUserId") Integer userId, Pageable page);

    @Query("""
                 SELECT
                 (COUNT(*)>0) AS isBorrowed
                 FROM BookTransactionHistory history
                 WHERE history.book.id = :bookId
                 AND history.returnedApproved=false
            """)
    boolean isAlreadyBorrowedByUser(Integer bookId);

    @Query("""
                 SELECT
                 history
                 FROM BookTransactionHistory history
                 WHERE history.user.id= :userId
                 AND history.book.id = :bookId
                 AND history.returned=false
                 AND history.returnedApproved=false
            """)
    Optional<BookTransactionHistory> findTransactionHistoryByBookIdAndUserId(Integer bookId, Integer userId);

    @Query("""
                 SELECT
                 history
                 FROM BookTransactionHistory history
                 WHERE history.book.owner.id= :ownerId
                 AND history.book.id = :bookId
                 AND history.returned=true
                 AND history.returnedApproved=false
            """)
    Optional<BookTransactionHistory> findTransactionHistoryByBookIdAndOwnerId(Integer bookId, Integer ownerId
    );
}
