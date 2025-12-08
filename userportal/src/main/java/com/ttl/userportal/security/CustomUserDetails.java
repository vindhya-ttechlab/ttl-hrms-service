package com.ttl.userportal.security;

import com.ttl.userportal.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails implementation that extends Spring Security's UserDetails
 * and includes the full Users entity for access to all user information
 */
public class CustomUserDetails implements UserDetails {
    
    private final Users user;
    private final List<GrantedAuthority> authorities;
    
    public CustomUserDetails(Users user) {
        this.user = user;
        this.authorities = new ArrayList<>();
        // Add default authority - you can extend this based on your role system
        this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus().name().equals("Active");
    }
    
    // Getter for the full Users entity
    public Users getUser() {
        return user;
    }
    
    // Convenience methods to access user information
    public Integer getUserId() {
        return user.getId();
    }
    
    public String getEmail() {
        return user.getEmail();
    }
    
    public String getName() {
        return user.getName();
    }
    
    public String getEmpCode() {
        return user.getEmpCode();
    }
    
    public String getDepartment() {
        return user.getDepartment();
    }
    
    public String getPosition() {
        return user.getPosition();
    }
    
    public Integer getManagerId() {
        return user.getManager();
    }
}
