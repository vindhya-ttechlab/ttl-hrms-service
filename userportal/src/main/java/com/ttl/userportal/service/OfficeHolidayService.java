package com.ttl.userportal.service;

import com.ttl.userportal.entity.OfficeHoliday;
import com.ttl.userportal.dto.HolidayDTO;
import com.ttl.userportal.repository.OfficeHolidayRepository;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfficeHolidayService
{
    @Autowired
    private OfficeHolidayRepository officeHolidayRepository;



    public List<HolidayDTO> getOfficeHolidayBasedOnYear(int year, UserDetails userDetails)
    {
        List<OfficeHoliday>holidayList=new ArrayList<>();
        holidayList=officeHolidayRepository.findByYear(year);
//        List<OfficeHoliday> officeHolidays = getOfficeHolidayBasedOnYear(year);

        List<HolidayDTO> holidayDTOs = holidayList.stream()
                .map(holiday -> {
                    HolidayDTO dto = new HolidayDTO();
                    dto.setId(holiday.getId());
                    dto.setHolidayDate(holiday.getHolidayDate());
                    dto.setHolidayDate(holiday.getHolidayDate());
                    dto.setName(holiday.getName());
                    dto.setDescription(holiday.getDescription());
                    dto.setDate(holiday.getHolidayDate() != null ? holiday.getHolidayDate().toString() : null);
                    return dto;
                })
                .collect(Collectors.toList());

        return holidayDTOs;
    }

}
