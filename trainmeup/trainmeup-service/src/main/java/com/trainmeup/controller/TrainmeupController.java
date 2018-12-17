package com.trainmeup.controller;

import com.trainmeup.model.Category;
import com.trainmeup.model.CategoryRequest;
import com.trainmeup.model.Question;
import com.trainmeup.model.QuestionRequest;
import com.trainmeup.repository.CategoryRepository;
import com.trainmeup.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TrainmeupController {

    private static final String CORS_URL = "http://localhost:4200";

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/category", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Category> getCategories() {
        return this.categoryRepository.findAll();
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/category", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        Category parent = this.categoryRepository.findByCategoryId(categoryRequest.getCategoryParentId());
        Category category = new Category();
        category.setCategoryId(parent.getNextCategoryId());
        category.setName(categoryRequest.getName());
        category.setNextCategoryId(createNextCategoryId(category.getCategoryId()));
        category.setNextQuestionId(createNextQuestionId(category.getCategoryId()));
        category.setParentId(parent.getCategoryId());

        parent.setNextCategoryId(computeNextCategoryId(parent.getNextCategoryId()));

        this.categoryRepository.save(parent);
        this.categoryRepository.save(category);
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/question", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveQuestion(@Valid @RequestBody QuestionRequest questionRequest) {
        Category parent = this.categoryRepository.findByCategoryId(questionRequest.getCategoryParentId());
        Question question = new Question();
        question.setQuestionId(parent.getNextQuestionId());
        question.setParentId(parent.getCategoryId());
        question.setQuestion(questionRequest.getQuestion());
        question.setAnswer(questionRequest.getAnswer());
        question.setAttempts(0);
        question.setRank(Question.Rank.UNTAKEN);
        question.setCreateDate(LocalDate.now());
        question.setUpdateDate(LocalDate.now());

        parent.setNextQuestionId(computeNextQuestionId(parent.getNextQuestionId()));

        this.categoryRepository.save(parent);
        this.questionRepository.save(question);
    }

    @CrossOrigin(origins = CORS_URL)
    @RequestMapping(value = "/question/hit", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Question hit(@Valid @RequestParam String categoryId) {

        String prefix = "q" + categoryId.substring(1) + "_";
        List<Question> list = this.questionRepository.findByQuestionIdLike(prefix);
        int index = (int)(Math.random() * list.size());
        return  list.get(index);
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
