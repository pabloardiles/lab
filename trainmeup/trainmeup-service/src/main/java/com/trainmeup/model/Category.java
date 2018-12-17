package com.trainmeup.model;

import org.springframework.data.annotation.Id;

public class Category {

	@Id private String id;
	private String categoryId;
	private String name;
	private String nextCategoryId;
	private String nextQuestionId;
	private String parentId;

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNextCategoryId() {
		return nextCategoryId;
	}

	public void setNextCategoryId(String nextCategoryId) {
		this.nextCategoryId = nextCategoryId;
	}

	public String getNextQuestionId() {
		return nextQuestionId;
	}

	public void setNextQuestionId(String nextQuestionId) {
		this.nextQuestionId = nextQuestionId;
	}
}
