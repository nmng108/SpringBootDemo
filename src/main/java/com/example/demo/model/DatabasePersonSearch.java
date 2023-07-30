package com.example.demo.model;

import com.example.demo.exception.InvalidRequestException;
import com.example.demo.model.dto.request.PersonSearchDTO;
import com.example.demo.model.entity.Person;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DatabasePersonSearch {
    Map<String, Condition> criteria;
    boolean usingOr;
    Sort sort;
    Pagination pagination;

    public DatabasePersonSearch(PersonSearchDTO personSearchDTO) {
        this.criteria = this.constructsCriteria(personSearchDTO);
        this.usingOr = this.constructsMode(personSearchDTO.getMode());
        this.sort = this.constructsSort(personSearchDTO.getSortBy(), personSearchDTO.getOrder());
        this.pagination = this.constructsPagination(personSearchDTO.getPage(), personSearchDTO.getSize());
    }

    // should add a regex or sth to specialize validation for each type of data ? DTO's validator did it
    private static Condition formatCondition(String propertyName, String condition) {
        // may put this into an "event" function that will be called after receiving and parsing request to PersonSearchDTO
        if (propertyName == null) {
            throw new RuntimeException("Property name is not found");
        }

        if (condition == null) {
            throw new InvalidRequestException("Value of " + propertyName + " cannot be null or blank");
        }

        // may accept 'list' format here

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
        for (String operator : PersonSearchDTO.OPERATORS) {
            if (condition.startsWith(operator)) {
//                String value = condition.substring(operator.concat(" ").length());
                String value = condition.replaceFirst(operator.concat(" "), "");

                return new Condition(propertyName, operator, value);
            }
        }

        throw new InvalidRequestException("Invalid operator of " + propertyName);
    }

    private Map<String, Condition> constructsCriteria(PersonSearchDTO personSearchDTO) {
        Map<String, Condition> stringCriteria = new HashMap<>();
        // map DTO field names to entity's field names
        if (personSearchDTO.getId() != null)
            stringCriteria.put("id", formatCondition("id", personSearchDTO.getId()));
        if (personSearchDTO.getName() != null)
            stringCriteria.put("name", formatCondition("name", personSearchDTO.getName()));
        if (personSearchDTO.getIdentity() != null)
            stringCriteria.put("identity", formatCondition("identity", personSearchDTO.getIdentity()));
        if (personSearchDTO.getAddress() != null)
            stringCriteria.put("address", formatCondition("address", personSearchDTO.getAddress()));
        if (personSearchDTO.getBirthDate() != null)
            stringCriteria.put("birthDate", formatCondition("birthDate", personSearchDTO.getBirthDate()));
        if (personSearchDTO.getHeight() != null)
            stringCriteria.put("height", formatCondition("height", personSearchDTO.getHeight()));
        if (personSearchDTO.getWeight() != null)
            stringCriteria.put("weight", formatCondition("weight", personSearchDTO.getWeight()));

        return stringCriteria;
    }

    public List<Predicate> criteriaToPredicates(CriteriaBuilder builder, Root<Person> personRoot) {
        return this.criteria.values().stream().map((condition) -> {
            String propName = condition.getPropertyName();
            String value = condition.getValue();

            // catch exception from conversing user's String input to Date or Number
            try {
                // assign an appropriate Expression<T> object to condition.expressionValue
                if (condition.getExpressionValue() == null) {
                    condition.setExpressionValue(switch (propName) {
                        case "id" -> builder.literal(Integer.parseInt(value)); // Expression<Integer>
                        case "height", "weight" -> builder.literal(Double.parseDouble(value));  // Expression<Double>
                        case "birthDate" -> builder.literal(Date.valueOf(value));
                        default -> condition.getOperator().equals("like")
                                ? builder.literal("%".concat(value).concat("%"))
                                : builder.literal(value); // Expression<String>
                    });
                }
            } catch (NumberFormatException e) {
                throw new InvalidRequestException(e.getMessage());
            }

            return switch (condition.getOperator()) {
                case "like" ->
                        builder.like(personRoot.get(propName), condition.getExpressionValue()); // allow some other data types and serve as "equal"
                case "equal", "eq", "=" -> builder.equal(personRoot.get(propName), condition.getExpressionValue());
                case "le", "<=" -> builder.lessThanOrEqualTo(personRoot.get(propName), condition.getExpressionValue());
                case "lt", "<" -> builder.lessThan(personRoot.get(propName), condition.getExpressionValue());
                case "ge", ">=" ->
                        builder.greaterThanOrEqualTo(personRoot.get(propName), condition.getExpressionValue());
                case "gt", ">" -> builder.greaterThan(personRoot.get(propName), condition.getExpressionValue());
                default -> throw new RuntimeException("operator is not in the allowed types");
            };
        }).toList();
    }

    /**
     * Use 'and' in conditional expression if there's no criterion passed by user
     *
     * @param mode String = PersonSearchDTO.OR_SEARCH or PersonSearchDTO.AND_SEARCH
     * @return boolean; true if using 'or' and there's at least 1 criterion, and false inversely.
     */
    private boolean constructsMode(String mode) {
        // may throw InvalidRequestException if mode is specified but there's no criterion
        return mode != null && mode.equals(PersonSearchDTO.OR_SEARCH)
                && !this.criteria.isEmpty();
    }

    /**
     * If needed, 'sortBy' always is specified first. When it's done, order may be specified or not (asc by default)
     *
     * @param sortBy String, an allowed arbitrary field name
     * @param order  String
     * @return org.springframework.data.domain.Sort
     */
    private Sort constructsSort(String sortBy, String order) {
        if (order != null && sortBy == null) {
            throw new InvalidRequestException("\"sortBy\" must be specified along with \"order\"");
        }

        if (sortBy != null && Arrays.stream(PersonSearchDTO.class.getDeclaredFields())
                .noneMatch(field -> field.getName().equals(sortBy))) {
            throw new InvalidRequestException("\"" + sortBy + "\"" + " is not a valid field");
        }

        Sort sort;

        if (order != null) {
            sort = switch (order) {
                case PersonSearchDTO.ASCENDING -> Sort.by(Sort.Direction.ASC, sortBy);
                case PersonSearchDTO.DESCENDING -> Sort.by(Sort.Direction.DESC, sortBy);
                default -> throw new RuntimeException("Invalid order argument");
            };
        } else if (sortBy != null) {
            sort = Sort.by(Sort.Direction.ASC, sortBy);
        } else {
            sort = Sort.unsorted(); // ???
        }

        return sort;
    }

    public List<Order> sortToCriteriaOrders(CriteriaBuilder builder, Root<Person> personRoot) {
        return this.sort.stream().map(sortOrder -> sortOrder.isDescending() ?
                builder.desc(personRoot.get(sortOrder.getProperty()))
                :
                builder.asc(personRoot.get(sortOrder.getProperty()))
        ).toList();
    }

    private Pagination constructsPagination(String page, Integer size) {
        return page == null ? null : new Pagination(page, size);
    }

    @Data
    public static class Condition {
        private String propertyName;
        private String operator;
        private String value;
        private Expression expressionValue;

        public Condition(String propertyName, String operator, String value) {
            this.propertyName = propertyName;
            this.operator = operator;
            this.value = value;
            this.expressionValue = null;
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
