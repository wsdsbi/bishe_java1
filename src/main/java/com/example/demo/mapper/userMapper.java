package com.example.demo.mapper;



import com.example.demo.entity.User;
import com.example.demo.entity.clom;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface userMapper {
    public User get_user(String name);
    public List<clom> get_tab_clom(String table);
    public List<clom> get_tab_info(String regin);
}
