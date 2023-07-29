package com.example.demo.Model;

import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.Model.DTO.Request.PersonSearchDTO;
import com.example.demo.Model.Entity.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.java.CoercionException;
import org.springframework.data.domain.Sort;

import java.util.*;

@AllArgsConstructor
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

    public record Condition(String propertyName, String operator, String value) {
        public Condition {
            Objects.requireNonNull(propertyName);
            Objects.requireNonNull(operator);
            Objects.requireNonNull(value);
        }
    }

    // should add a regex or sth to specialize validation for each type of data ? DTO's validator did it
    private static Condition formatCondition(String propertyName, String condition) {
        // may put this into an "event" function that will be called after receiving and parsing request to PersonSearchDTO
        if (condition == null) {
            throw new InvalidRequestException("Value of " + propertyName + " cannot be null or blank");
        }

        // may accept 'list' format here

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
        return this.criteria.values().stream().<Predicate>map((condition) -> {
            String propName = condition.propertyName();
            // get the rest string; wrong if the substring [1] exists in the remaining value (may not in this case but be careful)
            String value = condition.value();
            Predicate predicate;

            try {
                // org.springframework.dao.InvalidDataAccessApiUsageException: org.hibernate.query.SemanticException:
                // Could not resolve attribute 'xxx' of 'com.example.demo.Model.Entity.Person'
                predicate = switch (condition.operator()) {
                    case "like" -> builder.like(personRoot.get(propName), "%" + value + "%");
                    case "equal", "eq", "=" -> builder.equal(personRoot.get(propName), value);
                    case "le", "<=" -> builder.le(personRoot.get(propName), Double.parseDouble(value));
                    case "lt", "<" -> builder.lt(personRoot.get(propName), Double.parseDouble(value));
                    case "ge", ">=" -> builder.ge(personRoot.get(propName), Double.parseDouble(value));
                    case "gt", ">" -> builder.gt(personRoot.get(propName), Double.parseDouble(value));
                    default -> throw new RuntimeException("operator is not in the allowed types");
                };
            } catch (NumberFormatException e) {
                throw new InvalidRequestException("Wrong input value");
            } catch (CoercionException e) {
                // happens when casting data type got error (e.g. double -> int, String -> double
                // allow auto-casting from String -> Number but not String -> Date
                throw new InvalidRequestException("Invalid numeric input value");
            } catch (HibernateException e) {
                // exception: Unknown wrap conversion requested: java.lang.String to java.sql.Date
                throw new InvalidRequestException(e.getMessage());
            }

            return predicate;
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
        return this.sort.stream().<Order>map(sortOrder -> sortOrder.isDescending() ?
                builder.desc(personRoot.get(sortOrder.getProperty()))
                :
                builder.asc(personRoot.get(sortOrder.getProperty()))
        ).toList();
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
    }

    private Pagination constructsPagination(String page, Integer size) {
        return page == null
                ? null
                : new Pagination(page, size);
    }
}
