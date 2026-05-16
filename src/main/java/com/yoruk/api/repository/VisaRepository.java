package com.yoruk.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoruk.api.model.Visa;

@Repository
public interface VisaRepository extends JpaRepository<Visa, Long> {
    Optional<Visa> findByNameAndPassport(String name, int passportIndex);
}