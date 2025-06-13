package com.lms.courseservice.model;

import com.lms.courseservice.enums.CourseLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_creator", columnList = "creatorId"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_is_published", columnList = "isPublished")
})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String courseTitle;

    private String subTitle;

    private String description;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseLevel courseLevel = CourseLevel.BEGINNER;

    private Double coursePrice;

    private String courseThumbnail;

    @Column(nullable = false)
    private Boolean isPublished = false;

    @Column(nullable = false)
    private UUID creatorId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "course_enrolled_students", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "student_id")
    private List<UUID> enrolledStudentIds = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "course_lectures", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "lecture_id")
    private List<UUID> lectureIds = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
