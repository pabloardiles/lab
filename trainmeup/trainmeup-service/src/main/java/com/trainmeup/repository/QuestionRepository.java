
package com.trainmeup.repository;

import com.trainmeup.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {

	List<Question> findByQuestionIdLike(String idPrefix);

}
