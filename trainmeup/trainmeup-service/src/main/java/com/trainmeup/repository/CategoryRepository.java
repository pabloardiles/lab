
package com.trainmeup.repository;

import com.trainmeup.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {

	Category findByCategoryId(String categoryId);

}
