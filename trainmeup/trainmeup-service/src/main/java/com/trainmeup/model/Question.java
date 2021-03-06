package com.trainmeup.model;

import org.springframework.data.annotation.Id;

import java.time.LocalDate;

public class Question {

    public enum Rank {
        NOT_TAKEN(1),
        ROOKIE(2),
        BEGINNER(3),
        SEASONED(4),
        PROFICIENT(5),
        EXPERT(6);

        int id;

        Rank(int rid) {
            id = rid;
        }

        public static Rank promote(Rank rank) {
            switch(rank) {
                case NOT_TAKEN: return ROOKIE;
                case ROOKIE: return BEGINNER;
                case BEGINNER: return SEASONED;
                case SEASONED: return PROFICIENT;
                case PROFICIENT: return EXPERT;
                case EXPERT: return EXPERT;
            }
            return NOT_TAKEN;
        }

        public static Rank downgrade(Rank rank) {
            switch(rank) {
                case NOT_TAKEN: return NOT_TAKEN;
                case ROOKIE: return ROOKIE;
                case BEGINNER: return ROOKIE;
                case SEASONED: return BEGINNER;
                case PROFICIENT: return SEASONED;
                case EXPERT: return PROFICIENT;
            }
            return NOT_TAKEN;
        }
    }

    @Id private String id;
    private String questionId;
    private String question;
    private String answer;
    private String reference;
    private String parentId;
    private LocalDate createDate;
    private LocalDate updateDate;
    private Rank rank;
    private int attempts;

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public LocalDate getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDate updateDate) {
        this.updateDate = updateDate;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
