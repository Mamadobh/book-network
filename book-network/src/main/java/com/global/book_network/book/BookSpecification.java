package com.global.book_network.book;


import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.logging.Level;

public class BookSpecification {


    public static Specification<Book> withOwnerId(Integer ownerId) {
        return (root, query, cb) -> {
            return cb.equal(root.get("owner").get("id"), ownerId);
        };
    }


}
