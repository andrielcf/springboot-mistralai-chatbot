package com.nutrisync.nutrisync.respository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nutrisync.nutrisync.entity.DietPlan;


public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    

    List<DietPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
