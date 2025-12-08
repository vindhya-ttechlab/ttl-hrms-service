package com.ttl.userportal.repository;

import com.ttl.userportal.entity.MenuRoleMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRoleMapRepository extends JpaRepository<MenuRoleMap, Integer> {
    
    List<MenuRoleMap> findByRoleId(Integer roleId);
    
    List<MenuRoleMap> findByMenuId(Integer menuId);
    
    Optional<MenuRoleMap> findByMenuIdAndRoleId(Integer menuId, Integer roleId);
    
    boolean existsByMenuIdAndRoleId(Integer menuId, Integer roleId);
    
    void deleteByMenuIdAndRoleId(Integer menuId, Integer roleId);
    
    @Query("SELECT mrm.menuId FROM MenuRoleMap mrm WHERE mrm.roleId = :roleId")
    List<Integer> findMenuIdsByRoleId(@Param("roleId") Integer roleId);
    
    @Query("SELECT mrm.roleId FROM MenuRoleMap mrm WHERE mrm.menuId = :menuId")
    List<Integer> findRoleIdsByMenuId(@Param("menuId") Integer menuId);
}

