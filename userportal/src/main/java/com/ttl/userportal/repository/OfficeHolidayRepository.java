package com.ttl.userportal.repository;

import com.ttl.userportal.entity.OfficeHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.*;
import java.util.Date;
import java.util.List;

public interface OfficeHolidayRepository extends JpaRepository<OfficeHoliday, Long>
{
    @Query("SELECT oh FROM OfficeHoliday oh WHERE YEAR(oh.holidayDate) = :year")
    List<OfficeHoliday> findByYear(@Param("year") int year);


}
