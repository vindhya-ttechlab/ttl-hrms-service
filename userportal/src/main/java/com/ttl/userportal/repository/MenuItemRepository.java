package com.ttl.userportal.repository;

import com.ttl.userportal.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    
    Optional<MenuItem> findByPath(String path);
    
    List<MenuItem> findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrder();
    
    List<MenuItem> findByParentIdAndIsActiveTrueOrderByDisplayOrder(Integer parentId);
    
    List<MenuItem> findByIsActiveTrueOrderByDisplayOrder();
    
    List<MenuItem> findByParentId(Integer parentId);
}

