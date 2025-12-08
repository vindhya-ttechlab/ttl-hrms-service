package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HolidayDTO {
    private Long id;
    private LocalDate holidayDate;
    private String name;
    private String description;
    private String date; // Formatted date string for frontend

    // Helper method to get reason (for compatibility with frontend)
    public String getReason() {
        return name;
    }

    public void setReason(String reason) {
        this.name = reason;
    }
}
