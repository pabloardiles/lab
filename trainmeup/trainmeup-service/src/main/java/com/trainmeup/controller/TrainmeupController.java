package com.trainmeup.controller;

import com.trainmeup.model.Category;
import com.trainmeup.model.CategoryRequest;
import com.trainmeup.model.Question;
import com.trainmeup.model.QuestionRequest;
import com.trainmeup.repository.CategoryRepository;
import com.trainmeup.repository.QuestionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TrainmeupController {

    private static final Logger LOGGER = LogManager.getLogger(TrainmeupController.class);

    private static final String CORS_URL = "http://localhost:4200";

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/category", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Category> getCategories() {
        LOGGER.info("Get all categories");
        return this.categoryRepository.findAll();
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/category", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Category saveCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        LOGGER.info("Saving category '" + categoryRequest.getSubpath() + "'");
        Category parent = this.categoryRepository.findByCategoryId(categoryRequest.getCategoryParentId());

        for (String name : categoryRequest.getSubCategoryNames()) {
            Category category = new Category();
            category.setCategoryId(parent.getNextCategoryId());
            category.setName(name);
            category.setNextCategoryId(createNextCategoryId(category.getCategoryId()));
            category.setNextQuestionId(createNextQuestionId(category.getCategoryId()));
            category.setParentId(parent.getCategoryId());

            parent.setNextCategoryId(computeNextCategoryId(parent.getNextCategoryId()));

            this.categoryRepository.save(parent);
            parent = this.categoryRepository.save(category);
        }
        return parent;
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/question", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Question saveQuestion(@Valid @RequestBody QuestionRequest questionRequest) {
        LOGGER.info("Saving question '" + questionRequest.getQuestion() + "'");
        Category parent = this.categoryRepository.findByCategoryId(questionRequest.getCategoryParentId());
        Question question = new Question();
        question.setQuestionId(parent.getNextQuestionId());
        question.setParentId(parent.getCategoryId());
        question.setQuestion(questionRequest.getQuestion());
        question.setAnswer(questionRequest.getAnswer());
        question.setAttempts(0);
        question.setRank(Question.Rank.NOT_TAKEN);
        question.setCreateDate(LocalDate.now());
        question.setUpdateDate(LocalDate.now());

        parent.setNextQuestionId(computeNextQuestionId(parent.getNextQuestionId()));

        this.categoryRepository.save(parent);
        return this.questionRepository.save(question);
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/question/hit", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Question hit(@Valid @RequestParam String categoryId) {
        LOGGER.info("Hit a '" + categoryId + "' question");
        String prefix = "q" + categoryId.substring(1) + "_";
        List<Question> list = this.questionRepository.findByQuestionIdLike(prefix);
        int index = (int)(Math.random() * list.size());
        return  list.get(index);
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/question/score", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Question score(@Valid @RequestParam String questionId, @Valid @RequestParam String guessResult) {
        LOGGER.info("New score for '" + questionId + "' with result '" + guessResult + "'");
        Optional<Question> q = this.questionRepository.findByQuestionId(questionId);
        q.get().setAttempts(q.get().getAttempts()+1);
        q.get().setUpdateDate(LocalDate.now());
        if (guessResult.equalsIgnoreCase("correct")) {
            q.get().setRank(Question.Rank.promote(q.get().getRank()));
            return this.questionRepository.save(q.get());
            // audit
        } else if (guessResult.equalsIgnoreCase("incorrect")) {
            if (Question.Rank.NOT_TAKEN.equals(q.get().getRank())) {
                // move to rookie anyways
                q.get().setRank(Question.Rank.promote(q.get().getRank()));
            } else {
                q.get().setRank(Question.Rank.downgrade(q.get().getRank()));
            }
            return this.questionRepository.save(q.get());
            // audit
        } else if (guessResult.equalsIgnoreCase("partial")) {
            //just audit
        }
        return q.get();
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/path", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String path(@Valid @RequestParam String categoryId) {
        LOGGER.info("Return path for categoryId '" + categoryId + "'");
        String path = "";
        Category c = this.categoryRepository.findByCategoryId(categoryId);
        while(c.getParentId() != null) {
            path = c.getName() + "/" + path;
            c = this.categoryRepository.findByCategoryId(c.getParentId());
        }
        path = "/" + c.getName() + "/" + path;
        return "{\"path\":\"" + path + "\"}";
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> health() {
        LOGGER.info("Check service health");
        final String message = "{\"message\":\"%s\"}";
        try {
            //check database connection
            Category c = this.categoryRepository.findByCategoryId("c1");
            if (c == null) {
                String err = String.format(message, "error");
                return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String hty = String.format(message, "healthy");
            return new ResponseEntity(hty, HttpStatus.OK);

        } catch (Exception e) {
            LOGGER.error("check health error", e);
            String err = String.format(message, "error");
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String computeNextCategoryId(String categoryId) {
        String prefix = categoryId.substring(0, categoryId.lastIndexOf('_'));
        int last = Integer.parseInt(categoryId.substring(categoryId.lastIndexOf('_')+1)) + 1;
        return prefix + "_" + last;
    }

    private String computeNextQuestionId(String categoryId) {
        String prefix = categoryId.substring(1, categoryId.lastIndexOf('_'));
        int last = Integer.parseInt(categoryId.substring(categoryId.lastIndexOf('_')+1)) + 1;
        return "q" + prefix + "_" + last;
    }

    private String createNextCategoryId(String categoryId) {
        return categoryId + "_1";
    }

    private String createNextQuestionId(String categoryId) {
        return "q" + categoryId.substring(1) + "_1";
    }

}
