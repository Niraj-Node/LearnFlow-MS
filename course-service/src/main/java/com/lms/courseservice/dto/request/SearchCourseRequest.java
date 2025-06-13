package com.lms.courseservice.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchCourseRequest {

    private String query = "";

    private List<String> categories;

    private String sortByPrice = "";
}
