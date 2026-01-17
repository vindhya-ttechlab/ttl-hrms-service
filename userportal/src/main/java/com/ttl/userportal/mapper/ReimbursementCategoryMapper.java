package com.ttl.userportal.mapper;

import com.ttl.userportal.dto.ReimbursementCategoryDTO;
import com.ttl.userportal.entity.ReimbursementCategory;
import org.springframework.stereotype.Component;

@Component
public class ReimbursementCategoryMapper {

    public ReimbursementCategory toEntity(ReimbursementCategoryDTO dto){
        if(dto == null)
            return null;

        ReimbursementCategory category = new ReimbursementCategory();
        category.setCategoryId(dto.getCategoryId());
        category.setCategoryName(dto.getCategoryName());
        category.setDescription(dto.getDescription());
        category.setMaxAmount(dto.getMaxAmount());
        category.setRequiresReceipt(dto.getRequiresReceipt() !=null ? dto.getRequiresReceipt() : true);
        category.setRequiresApproval(dto.getRequiresApproval() !=null ? dto.getRequiresApproval() : true);
        category.setIsActive(true);

        return category;
    }
    public ReimbursementCategoryDTO toDto(ReimbursementCategory category) {
        if(category == null)
            return null;

        return new ReimbursementCategoryDTO(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getDescription(),
                category.getMaxAmount(),
                category.getRequiresReceipt(),
                category.getRequiresApproval(),
                category.getIsActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
