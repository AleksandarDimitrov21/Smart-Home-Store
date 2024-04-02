package com.ninjas.gig.service;


import com.ninjas.gig.entity.Product;
//import com.ninjas.gig.repository.CategoryRepository;
import com.ninjas.gig.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.similarity.LevenshteinDistance;

@Service
public class ProductService {
    @Autowired
    private ProductsRepository productRepository;

    public Product addProduct(Product product){
        return productRepository.save(product);
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }


    public List<Product> filterByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    public List<Product> findSimilarProducts(String name) {
        LevenshteinDistance distance = new LevenshteinDistance();
        List<Product> allProducts = getAll();
        List<Product> similarProducts = new ArrayList<>();

        String cleanedName = name.replaceAll("\\s+", "").toLowerCase();

        for (Product product : allProducts) {
            String productName = product.getName().toLowerCase();
            boolean isSimilar = false;

            String[] productWords = productName.split("\\s+");
            for (String productWord : productWords) {
                String cleanedProductWord = productWord.replaceAll("\\s+", "");
                if (distance.apply(cleanedProductWord, cleanedName) <= 2) {
                    isSimilar = true;
                    break;
                }
            }
            if (isSimilar) {
                similarProducts.add(product);
            }
        }
        return similarProducts;
    }

}
