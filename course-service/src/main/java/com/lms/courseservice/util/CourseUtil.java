package com.lms.courseservice.util;

import com.lms.courseservice.dto.request.SearchCourseRequest;
import com.lms.courseservice.dto.response.CourseResponse;
import com.lms.courseservice.grpc.GrpcUserClient;
import com.lms.courseservice.mapper.CourseMapper;
import com.lms.courseservice.model.Course;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.grpc.SlimUser;
import jakarta.persistence.criteria.Predicate;

import java.util.*;
import java.util.stream.Collectors;

public class CourseUtil {

    public static List<CourseResponse> enrichWithCreatorInfo(List<Course> courses, GrpcUserClient grpcUserClient) {
        // Step 1: Collect unique creator IDs
        Set<UUID> creatorIds = courses.stream()
                .map(Course::getCreatorId)
                .collect(Collectors.toSet());

        // Step 2: Batch fetch using gRPC
        Map<UUID, SlimUser> userMap = grpcUserClient.getUsersByIds(new ArrayList<>(creatorIds));

        // Step 3: Map users to courses
        return courses.stream()
                .map(course -> {
                    SlimUser creator = userMap.get(course.getCreatorId());
                    String name = creator != null ? creator.getName() : null;
                    String photoUrl = creator != null ? creator.getPhotoUrl() : null;
                    return CourseMapper.toFullResponse(course, name, photoUrl);
                })
                .toList();
    }

    public static List<Course> searchCoursesWithFilter(SearchCourseRequest request, CourseRepository courseRepository) {
        String query = request.getQuery();
        List<String> categories = request.getCategories();
        String sortByPrice = request.getSortByPrice();

        // Use Specification to query with filters
        List<Course> courses = courseRepository.findAll((root, queryObj, cb) -> {
            var predicates = new ArrayList<Predicate>();

            // isPublished = true
            predicates.add(cb.isTrue(root.get("isPublished")));

            // fuzzy match
            String queryLower = "%" + query.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("courseTitle")), queryLower),
                    cb.like(cb.lower(root.get("subTitle")), queryLower),
                    cb.like(cb.lower(root.get("category")), queryLower)
            ));

            // category filter
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").in(categories));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });

        // Sorting
        if ("low".equalsIgnoreCase(sortByPrice)) {
            courses.sort(Comparator.comparing(Course::getCoursePrice));
        } else if ("high".equalsIgnoreCase(sortByPrice)) {
            courses.sort(Comparator.comparing(Course::getCoursePrice).reversed());
        }

        return courses;
    }
}
