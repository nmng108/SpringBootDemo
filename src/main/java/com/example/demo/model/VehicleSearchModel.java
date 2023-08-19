package com.example.demo.model;

import com.example.demo.dto.request.PersonSearchDTO;
import com.example.demo.dto.request.VehicleSearchDTO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class VehicleSearchModel extends EntitySearchModel<VehicleSearchDTO> {
    public VehicleSearchModel(VehicleSearchDTO searchDTO) {
        super(searchDTO);
//        this.criteria = this.constructsCriteria(searchDTO);
//        this.usingOr = this.constructsMode(searchDTO.getMode());
//        this.sort = this.constructsSort(searchDTO.getSortBy(), searchDTO.getOrder());
//        this.pagination = this.constructsPagination(searchDTO.getPage(), searchDTO.getSize());
    }

    // TODO: refactor this
    @Override
    protected Map<String, Condition> constructsCriteria(VehicleSearchDTO searchDTO) {
        Map<String, Condition> stringCriteria = new HashMap<>();
        // map DTO field names to entity's field
        if (searchDTO.getId() != null)
            stringCriteria.put("id", formatCondition("id", searchDTO.getId()));

        if (searchDTO.getIdNumber() != null)
            stringCriteria.put("idNumber", formatCondition("identificationNumber", searchDTO.getIdNumber()));
//
//        if (searchDTO.getOwnerIdentity() != null)
//            stringCriteria.put("ownerIdentity", formatCondition("ownerIdentity", searchDTO.getOwnerIdentity()));

        if (searchDTO.getType() != null)
            stringCriteria.put("type", formatCondition("type", searchDTO.getType()));

        if (searchDTO.getBrand() != null)
            stringCriteria.put("brand", formatCondition("brand", searchDTO.getBrand()));

        if (searchDTO.getModel() != null)
            stringCriteria.put("model", formatCondition("model", searchDTO.getModel()));

        if (searchDTO.getAcquisitionDate() != null)
            stringCriteria.put("acquisitionDate", formatCondition("acquisitionDate", searchDTO.getAcquisitionDate()));

        return stringCriteria;
    }

    @Override
    protected Expression<?> createExpression(CriteriaBuilder builder, Condition condition) {
        String attributeName = condition.getAttributeName();
        String value = condition.getValue();

        return switch (attributeName) {
            case "id" -> builder.literal(Integer.parseInt(value)); // Expression<Integer>
            case "acquisitionDate" -> builder.literal(Date.valueOf(value));
            default -> condition.getOperator().equals("like")
                    ? builder.literal("%".concat(value).concat("%"))
                    : builder.literal(value); // Expression<String>
        };
    }

    @Override
    protected Class<VehicleSearchDTO> getSearchDtoClass() {
        return VehicleSearchDTO.class;
    }
}
