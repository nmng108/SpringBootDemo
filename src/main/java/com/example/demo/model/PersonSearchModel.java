package com.example.demo.model;

import com.example.demo.dto.request.PersonSearchDTO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class PersonSearchModel extends EntitySearchModel<PersonSearchDTO> {
    public PersonSearchModel(PersonSearchDTO searchDTO) {
        super(searchDTO);
//        this.criteria = this.constructsCriteria(searchDTO);
//        this.usingOr = this.constructsMode(searchDTO.getMode());
//        this.sort = this.constructsSort(searchDTO.getSortBy(), searchDTO.getOrder());
//        this.pagination = this.constructsPagination(searchDTO.getPage(), searchDTO.getSize());
    }

    // TODO: refactor this
    @Override
    protected Map<String, Condition> constructsCriteria(PersonSearchDTO searchDTO) {
        Map<String, Condition> stringCriteria = new HashMap<>();
        // map DTO field names to entity's field
        if (searchDTO.getId() != null)
            stringCriteria.put("id", formatCondition("id", searchDTO.getId()));

        if (searchDTO.getName() != null)
            stringCriteria.put("name", formatCondition("name", searchDTO.getName()));
        if (searchDTO.getIdentity() != null)

            stringCriteria.put("identity", formatCondition("identity", searchDTO.getIdentity()));

        if (searchDTO.getAddress() != null)
            stringCriteria.put("address", formatCondition("address", searchDTO.getAddress()));

        if (searchDTO.getBirthDate() != null)
            stringCriteria.put("birthDate", formatCondition("birthDate", searchDTO.getBirthDate()));

        if (searchDTO.getHeight() != null)
            stringCriteria.put("height", formatCondition("height", searchDTO.getHeight()));

        if (searchDTO.getWeight() != null)
            stringCriteria.put("weight", formatCondition("weight", searchDTO.getWeight()));

        return stringCriteria;
    }

    @Override
    protected Expression<?> createExpression(CriteriaBuilder builder, Condition condition) {
        String attributeName = condition.getAttributeName();
        String value = condition.getValue();

        return switch (attributeName) {
            case "id" -> builder.literal(Integer.parseInt(value)); // Expression<Integer>
            case "height", "weight" -> builder.literal(Double.parseDouble(value));  // Expression<Double>
            case "birthDate" -> builder.literal(Date.valueOf(value));
            default -> condition.getOperator().equals("like")
                    ? builder.literal("%".concat(value).concat("%"))
                    : builder.literal(value); // Expression<String>
        };
    }

    @Override
    protected Class<PersonSearchDTO> getSearchDtoClass() {
        return PersonSearchDTO.class;
    }
}
