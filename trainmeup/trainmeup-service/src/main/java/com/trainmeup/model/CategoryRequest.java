package com.trainmeup.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class CategoryRequest {

    @NotEmpty
    private String categoryParentId;

    @Pattern(regexp = "^(\\w(\\w|\\s)*\\w\\/)+$")
    private String subpath;

    public String getCategoryParentId() {
        return categoryParentId;
    }

    public void setCategoryParentId(String categoryParentId) {
        this.categoryParentId = categoryParentId;
    }

    public String getSubpath() {
        return subpath;
    }

    public void setSubpath(String name) {
        this.subpath = name;
    }

    public String[] getSubCategoryNames() {
        return subpath.split("/");
    }
}
