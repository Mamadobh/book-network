package com.global.book_network.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    @Query("""
                 SELECT book
                 FROM Book book 
                 WHERE book.archived=false
                 AND book.shareable=true
                 AND book.owner.id != :currentUserId
            """)
    Page<Book> findAllDisplayableBooks(Pageable page, @Param(value = "currentUserId") Integer UserId);

}