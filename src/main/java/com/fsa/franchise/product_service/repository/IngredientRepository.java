package com.fsa.franchise.product_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fsa.franchise.product_service.entity.Ingredient;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

}
