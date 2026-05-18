package com.yoruk.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoruk.api.model.Gemini;

@Repository
public interface GeminiRepository extends JpaRepository<Gemini, Long> {
    Optional<Gemini> findByNameAndPassport(String name, String passport);
}