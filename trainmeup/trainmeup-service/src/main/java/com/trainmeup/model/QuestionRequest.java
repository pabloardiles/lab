package com.trainmeup.model;

import javax.validation.constraints.NotEmpty;

public class QuestionRequest {

    @NotEmpty
    private String categoryParentId;
    @NotEmpty
    private String question;
    @NotEmpty
    private String answer;
    @NotEmpty
    private String reference;

    public String getCategoryParentId() {
        return categoryParentId;
    }

    public void setCategoryParentId(String categoryParentId) {
        this.categoryParentId = categoryParentId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
