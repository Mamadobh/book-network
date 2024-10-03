package com.global.book_network.feedback;

import com.global.book_network.book.Book;
import com.global.book_network.book.BookRepository;
import com.global.book_network.common.PageResponse;
import com.global.book_network.exception.OperationNotPermittedException;
import com.global.book_network.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final BookRepository bookRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackRepository feedbackRepository;

    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new EntityNotFoundException("No book found  with id " + request.bookId()));

        if (book.isArchived() || !book.isShareable()) {
            throw new EntityNotFoundException("You cannot give a feedback for an archived or not shareable book");
        }
        User user = (User) connectedUser.getPrincipal();
        if (book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You cannot give a feedback for you own book");
        }

        Feedback feedback = feedbackMapper.toFeedback(request);

        return feedbackRepository.save(feedback).getId();
    }


    public PageResponse<FeedbackResponse> findAllFeedbackByBook(Integer bookId, Integer size, Integer pageNumber, Authentication connectedUser) {

        Pageable page = PageRequest.of(pageNumber, size);
        User user = (User) connectedUser.getPrincipal();

        Page<Feedback> feedbacks = feedbackRepository.findFeedbackByBook(bookId, page);

        List<FeedbackResponse> bookFeedbackReponse = feedbacks.stream()
                .map((fedb) -> feedbackMapper.toFeedbackResponse(fedb, user.getId()))
                .toList();
        return new PageResponse<FeedbackResponse>(
                bookFeedbackReponse,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );


    }
}
