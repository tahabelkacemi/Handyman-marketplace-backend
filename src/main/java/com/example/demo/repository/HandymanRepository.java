package com.example.demo.repository;

import com.example.demo.entity.Handyman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HandymanRepository extends JpaRepository<Handyman, Long> {
    java.util.Optional<Handyman> findByOwnerUsername(String ownerUsername);

   @Query("SELECT h FROM Handyman h WHERE " +
       "(:skill IS NULL OR LOWER(h.skillName) LIKE LOWER(CONCAT('%', :skill, '%'))) AND " +
       "(:location IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :location, '%')))")
List<Handyman> searchProfessionals(@Param("skill") String skill, 
                                   @Param("location") String location);
}                                 