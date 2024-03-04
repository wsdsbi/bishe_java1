package com.example.demo.controller.login;

import com.example.demo.entity.indicatorControl;
import com.example.demo.service.indicator.indicator_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class indicatorController {
    @Autowired
    indicator_service indicator_service;

    @PostMapping("/insert_indicator_control")
    public boolean getAll(@RequestBody indicatorControl indicator_control){
         return indicator_service.insert_indicator_control(indicator_control);
    }

    @PostMapping("/get_indicator")
    public List<indicatorControl> get_indicator(@RequestBody indicatorControl indicator_control){
        return indicator_service.get_indicator(indicator_control);
    }

    @PostMapping("/update_indicator_control")
    public boolean update_indicator_control(@RequestBody indicatorControl indicator_control){
        return indicator_service.update_indicator_control(indicator_control);
    }
}
