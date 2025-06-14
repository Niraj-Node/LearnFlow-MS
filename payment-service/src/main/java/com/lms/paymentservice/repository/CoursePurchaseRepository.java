package com.lms.paymentservice.repository;

import com.lms.paymentservice.enums.Status;
import com.lms.paymentservice.model.CoursePurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CoursePurchaseRepository extends JpaRepository<CoursePurchase, UUID> {
    Optional<CoursePurchase> findByCourseIdAndUserId(UUID courseId, UUID userId);
    Optional<CoursePurchase> findByPaymentId(String paymentId);
    boolean existsByUserIdAndCourseIdAndStatus(UUID userId, UUID courseId, Status status);
}

