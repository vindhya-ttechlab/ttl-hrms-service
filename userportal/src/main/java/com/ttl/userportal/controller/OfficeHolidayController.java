package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.entity.OfficeHoliday;
import com.ttl.userportal.dto.HolidayDTO;
import com.ttl.userportal.service.OfficeHolidayService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RestController
@RequestMapping("/holidays")
@CrossOrigin(origins = "http://localhost:3000")
public class OfficeHolidayController
{
    @Autowired
    private OfficeHolidayService officeHolidayService;

    @PostMapping("/year-holiday")
    public ResponseEntity<List<HolidayDTO>> getHolidayList(@RequestBody Integer year, @CurrentUser UserDetails userDetails)
    {
        List<HolidayDTO> holidayDTOs = officeHolidayService.getOfficeHolidayBasedOnYear(year,userDetails);
        return ResponseEntity.ok(holidayDTOs);
    }

}
