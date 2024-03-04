package com.example.demo.service.indicator;

import com.example.demo.entity.indicatorControl;
import com.example.demo.mapper.indicatorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class indicator_service {
    @Autowired
    indicatorMapper indicatorMapper;

    public Boolean insert_indicator_control(indicatorControl indicatorControl) {
        return indicatorMapper.insert_indicator_control(indicatorControl);
    }
    public List<indicatorControl> get_indicator(indicatorControl indicatorControl) {
        System.out.println(indicatorControl);
        System.out.println(indicatorMapper.get_indicator(indicatorControl)+"inditor");
        return indicatorMapper.get_indicator(indicatorControl);
    }

    public Boolean update_indicator_control(indicatorControl indicatorControl) {
        return indicatorMapper.update_indicator_control(indicatorControl);
    }
}
