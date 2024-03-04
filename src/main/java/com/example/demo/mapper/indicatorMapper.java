package com.example.demo.mapper;




import com.example.demo.entity.indicatorControl;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface indicatorMapper {
    public Boolean insert_indicator_control(indicatorControl indicatorControl);
    public List<indicatorControl> get_indicator(indicatorControl indicatorControl);
    public Boolean update_indicator_control(indicatorControl indicatorControl);
}
