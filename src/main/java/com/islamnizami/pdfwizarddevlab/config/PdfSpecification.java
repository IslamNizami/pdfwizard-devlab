package com.islamnizami.pdfwizarddevlab.config;

import com.islamnizami.pdfwizarddevlab.model.dto.PdfSearchCriteriaDTO;
import com.islamnizami.pdfwizarddevlab.model.entity.PdfDocument;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class PdfSpecification {

    public static Specification<PdfDocument> withCriteria(PdfSearchCriteriaDTO criteria){
        return (root,query,cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(criteria == null){
                return cb.conjunction();
            }

            if(criteria.getFileName() != null && !criteria.getFileName().isBlank()){
                predicates.add(cb.like(cb.lower(root.get("fileName")),"%" + criteria.getFileName().toLowerCase() + "%"));
            }

            if (criteria.getUser() != null && !criteria.getUser().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("createdBy")), criteria.getUser().toLowerCase()));
            }

            if (criteria.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getFromDate()));
            }

            if (criteria.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
