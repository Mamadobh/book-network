package com.global.book_network.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BookDto {
    private Integer id;
    @NotBlank(message = "title is required")
    @NotNull(message = "title is required")
    private String title;
    @NotBlank(message = "autherNAme is required")
    @NotNull(message = "    private String authorName;\n is required")
    private String authorName;
    @NotBlank(message = "isbn is required")
    @NotNull(message = "isbn is required")
    private String isbn;
    @NotBlank(message = "synopsis is required")
    @NotNull(message = "synopsis is required")
    private String synopsis;

    private boolean shareable;


}
