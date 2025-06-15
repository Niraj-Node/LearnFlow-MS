package com.lms.progressservice.dto;

import com.lms.progressservice.model.LectureProgress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressResponse {
    private List<LectureProgress> progress;
    private boolean completed;
}
