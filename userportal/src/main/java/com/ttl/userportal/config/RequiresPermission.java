package com.ttl.userportal.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark controller methods that require specific permissions
 * Usage: @RequiresPermission(permission = "WRITE", resource = "LEAVE_TYPE")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String permission(); // READ, WRITE, VIEW
    String resource();   // LEAVE_TYPE, LEAVE, EMPLOYEE, etc.
}

