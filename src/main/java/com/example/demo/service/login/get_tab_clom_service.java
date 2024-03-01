package com.example.demo.service.login;

import com.example.demo.entity.User;
import com.example.demo.entity.clom;
import com.example.demo.mapper.userMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service


public class get_tab_clom_service {
    @Autowired
    userMapper userMapper;

    public List<clom> get_tab_clom(String regin,String table){

        return userMapper.get_tab_clom(table);
    }
    public List<clom> get_tab_info(String regin,String table){

        return userMapper.get_tab_info(regin);
    }
}
