package com.example.demo.model;

import com.example.demo.dto.request.SearchDTO;
import com.example.demo.exception.InternalServerException;
import com.example.demo.exception.InvalidRequestException;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public abstract class EntitySearchModel<T extends SearchDTO> {
    protected Map<String, Condition> criteria;
    protected boolean usingOr;
    protected Sort sort;
    protected Pagination pagination;

    public EntitySearchModel(T searchDTO) {
        this.criteria = this.constructsCriteria(searchDTO);
        this.usingOr = this.constructsMode(searchDTO.getMode());
        this.sort = this.constructsSort(searchDTO.getSortBy(), searchDTO.getOrder());
        this.pagination = this.constructsPagination(searchDTO.getPage(), searchDTO.getSize());
    }

    protected static Condition formatCondition(String propertyName, String condition) {
        // may put this into an "event" function that will be called after receiving and parsing request to SearchDTO
        if (propertyName == null) {
            throw new InternalServerException("Property name is not found");
        }

        if (condition == null) {
            throw new InvalidRequestException("Value of " + propertyName + " cannot be null or blank");
        }

        // may accept & check the 'list' format here

        // apply a common regex to all property's conditions
        if (!condition.matches("^[a-zA-Z0-9-._]+( [a-zA-Z0-9-._]+)*$")) {
            throw new InvalidRequestException("Invalid value of " + propertyName);
        }

        // at least 1 "word"
        String[] splitCondition = condition.split(" ");

        if (splitCondition.length == 1) {
            return new Condition(propertyName, "=", condition);
        }

        // at least 2 "words"
        for (String operator : SearchDTO.OPERATORS) {
            if (condition.startsWith(operator)) {
                String value = condition.replaceFirst(operator.concat(" "), "");

                return new Condition(propertyName, operator, value);
            }
        }

        throw new InvalidRequestException("Invalid operator of " + propertyName);
    }

    // relate to specific searchDto; need to be overridden
    protected abstract Map<String, Condition> constructsCriteria(T searchDTO);

    public <R> List<Predicate> criteriaToPredicates(CriteriaBuilder builder, Root<R> root) {
        return this.criteria.values().stream().map((condition) -> {
            String attributeName = condition.getAttributeName();

            // catch exception from conversing user's String input to Date or Number
            try {
                // assign an appropriate Expression<T> object to condition.expressionValue
                // relate to specific searchDto
                if (condition.getExpressionValue() == null) {
                    condition.setExpressionValue(this.createExpression(builder, condition));
                }
            } catch (NumberFormatException e) {
                throw new InvalidRequestException(e.getMessage());
            }

            return switch (condition.getOperator()) {
                // allow some other data types and serve as "equal"
                case "like" -> builder.like(root.get(attributeName), condition.getExpressionValue());
                case "equal", "eq", "=" -> builder.equal(root.get(attributeName), condition.getExpressionValue());
                case "le", "<=" -> builder.lessThanOrEqualTo(root.get(attributeName), condition.getExpressionValue());
                case "lt", "<" -> builder.lessThan(root.get(attributeName), condition.getExpressionValue());
                case "ge", ">=" ->
                        builder.greaterThanOrEqualTo(root.get(attributeName), condition.getExpressionValue());
                case "gt", ">" -> builder.greaterThan(root.get(attributeName), condition.getExpressionValue());
                default -> throw new RuntimeException("operator is not in the allowed types");
            };
        }).toList();
    }

    // relate to specific searchDto; need to be overridden

    protected abstract Expression<?> createExpression(CriteriaBuilder builder, Condition condition);

    /**
     * Use 'and' in conditional expression if there's no criterion passed by user
     *
     * @param mode String = SearchDTO.OR_SEARCH or SearchDTO.AND_SEARCH
     * @return boolean; true if using 'or' and there's at least 1 criterion, and false inversely.
     */
    protected boolean constructsMode(String mode) {
        // may throw InvalidRequestException if mode is specified but there's no criterion
        return mode != null && mode.equals(SearchDTO.OR_SEARCH)
                && !this.criteria.isEmpty();
    }

    /**
     * If needed, 'sortBy' always is specified first. When it's done, order may be specified or not (asc by default)
     *
     * @param sortBy String, an allowed arbitrary field name
     * @param order  String
     * @return org.springframework.data.domain.Sort
     */
    protected Sort constructsSort(String sortBy, String order) {
        if (order != null && sortBy == null) {
            throw new InvalidRequestException("\"sortBy\" must be specified along with \"order\"");
        }

        // check if the field exists
        if (sortBy != null && Arrays.stream(this.getSearchDtoClass().getDeclaredFields())
                .noneMatch(field -> field.getName().equals(sortBy))) {
            throw new InvalidRequestException("\"" + sortBy + "\"" + " is not a valid field");
        }

        Sort sort;

        if (order != null) {
            sort = switch (order) {
                case SearchDTO.ASCENDING -> Sort.by(Sort.Direction.ASC, sortBy);
                case SearchDTO.DESCENDING -> Sort.by(Sort.Direction.DESC, sortBy);
                default -> throw new InvalidRequestException("Invalid order argument");
            };
        } else if (sortBy != null) {
            sort = Sort.by(Sort.Direction.ASC, sortBy);
        } else {
            sort = Sort.unsorted(); // ???
        }

        return sort;
    }

    protected abstract Class<T> getSearchDtoClass();

    public <R> List<Order> sortToCriteriaOrders(CriteriaBuilder builder, Root<R> root) {
        return this.sort.stream().map(sortOrder -> sortOrder.isDescending() ?
                builder.desc(root.get(sortOrder.getProperty()))
                :
                builder.asc(root.get(sortOrder.getProperty()))
        ).toList();
    }

    protected Pagination constructsPagination(String page, Integer size) {
        return page == null ? null : new Pagination(page, size);
    }

    @Data
    public static class Condition {
        private String attributeName;
        private String operator;
        private String value;
        private Expression expressionValue;

        public Condition(String attributeName, String operator, String value) {
            this.attributeName = attributeName;
            this.operator = operator;
            this.value = value;
            this.expressionValue = null;
        }

        public void setExpressionValue(Expression expressionValue) {
            this.expressionValue = expressionValue;
        }
    }

    @AllArgsConstructor
    @Data
    public static final class Pagination {
        private Integer from;
        private Integer to;
        private Integer size;

        public Pagination(String page, Integer size) {
            if (page == null) throw new RuntimeException("page value cannot be null");

            int from = 0;
            Integer to = null;

            if (page.matches("^[0-9]+$")) { // only 1 number exists
                from = Integer.parseInt(page);
            } else if (page.matches("^[0-9]+,([0-9]+)?$")) { // the 2nd number may exist or not
                String[] splitPageValue = page.split(",");
                from = Integer.parseInt(splitPageValue[0]);
                // get all remaining records (start from the 'from' page) if the 2nd number isn't specified
                to = splitPageValue.length == 1 ? Integer.MAX_VALUE : Integer.parseInt(splitPageValue[1]);
            } else {
                throw new InvalidRequestException("Invalid page");
            }

            this.from = from;
            this.to = to;
            this.size = size;
        }

        public int firstRecordNumber() {
            return this.from * this.size;
        }

        public int getNumberOfRecords() {
            // should count before fetching (instead of using MAX_VALUE); and pass an argument to turn on/off it
            int numberOfRecords;

            if (this.to == null) {
                numberOfRecords = this.size;
            } else if (this.to == Integer.MAX_VALUE) {
                numberOfRecords = this.to;
            } else {
                numberOfRecords = (this.to - this.from + 1) * this.size;
            }

            return numberOfRecords;
        }
    }
}
