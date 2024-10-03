package com.global.book_network.feedback;

import com.global.book_network.book.Book;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FeedbackMapper {
    public Feedback toFeedback(FeedbackRequest request) {
        return Feedback.builder()
                .note(request.note())
                .comment(request.comment())
                .book(Book.builder()
                        .id(request.bookId())
                        .archived(false)//not required just to satisfy lombok
                        .shareable(false)//not required just to satisfy lombok
                        .build())
                .build();
    }

    public FeedbackResponse toFeedbackResponse(Feedback feedback, Integer userId) {
        return FeedbackResponse.builder()
                .note(feedback.getNote())
                .comment((feedback.getComment()))
                .ownFeedBack(Objects.equals(feedback.getCreatedBy(), userId))
                .build();

    }
}
