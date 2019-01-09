
package com.trainmeup.repository;

import com.trainmeup.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends MongoRepository<Question, String> {

	List<Question> findByQuestionIdLike(String idPrefix);
	Optional<Question> findByQuestionId(String questionId);
}
