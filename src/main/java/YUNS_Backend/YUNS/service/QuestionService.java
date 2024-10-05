package YUNS_Backend.YUNS.service;

import YUNS_Backend.YUNS.dto.QuestionDto;
import YUNS_Backend.YUNS.entity.Question;
import YUNS_Backend.YUNS.entity.User;
import YUNS_Backend.YUNS.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<QuestionDto> getQuestionsByPage(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize); // 페이지는 0부터 시작하므로 1을 빼줌
        Page<Question> paginatedQuestions = questionRepository.findAll(pageable);

        return paginatedQuestions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<QuestionDto> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId).map(this::convertToDto);
    }

    public QuestionDto createQuestion(QuestionDto dto, User user) {
        Question question = Question.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .state(false)
                .date(LocalDateTime.now())
                .user(user)
                .build();

        Question savedQuestion = questionRepository.save(question);
        return convertToDto(savedQuestion);
    }

    public Optional<QuestionDto> updateQuestion(Long questionId, QuestionDto dto) {
        return questionRepository.findById(questionId).map(existingQuestion -> {
            // 기존 질문을 빌더 패턴으로 수정
            Question updatedQuestion = Question.builder()
                    .questionId(existingQuestion.getQuestionId())
                    .title(dto.getTitle() != null ? dto.getTitle() : existingQuestion.getTitle())
                    .content(dto.getContent() != null ? dto.getContent() : existingQuestion.getContent())
                    .imageUrl(dto.getImageUrl() != null ? dto.getImageUrl() : existingQuestion.getImageUrl())
                    .date(existingQuestion.getDate())
                    .state(existingQuestion.isState())
                    .answer(existingQuestion.getAnswer())
                    .user(existingQuestion.getUser())
                    .build();

            Question savedQuestion = questionRepository.save(updatedQuestion);
            return convertToDto(savedQuestion);
        });
    }

    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    private QuestionDto convertToDto(Question question) {
        return QuestionDto.builder()
                .questionId(question.getQuestionId())
                .title(question.getTitle())
                .content(question.getContent())
                .date(question.getDate())
                .state(question.isState())
                .answer(question.getAnswer())
                .imageUrl(question.getImageUrl())
                .userStudentNumber(question.getUser().getStudentNumber())
                .build();
    }


}
