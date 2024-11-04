package com.example.RecetaApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RecetaApp.model.Despacho;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {
}