package com.lms.progressservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private UUID userId;

    @NotNull
    private UUID courseId;

    private boolean completed;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<LectureProgress> lectureProgress = new ArrayList<>();
}