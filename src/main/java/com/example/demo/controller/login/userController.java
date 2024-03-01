package com.example.demo.controller.login;

import com.example.demo.entity.User;
import com.example.demo.entity.clom;
import com.example.demo.mapper.userMapper;
import com.example.demo.service.login.get_tab_clom_service;
import com.example.demo.service.login.login_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
public class userController {
    @Autowired
    userMapper userMapper;
    @Autowired
    login_service login_service;
     @Autowired
    get_tab_clom_service get_tab_clom_service;
    @PostMapping("/login")
    public boolean getAll(@RequestBody User login_user_check){
        System.out.println(login_user_check);
        if(login_user_check.account==null || login_user_check.password==null){
            return  false;
        }
        User user_info = userMapper.get_user(login_user_check.account);
        if(user_info.account==null){
            return false;
        }
        return login_service.logincheck(login_user_check,user_info);
    }
    @PostMapping("/get_tab_clom")
    public List<clom>  get_tab_clom(@RequestBody String info){
        System.out.println(info);
        String[] split = info.split("\"");
        String regin = split[3];
        String table = split[7];
        System.out.println(regin+"     "+table);
        List<clom> tab_clom = get_tab_clom_service.get_tab_clom(regin, table);
        System.out.println(tab_clom);
        return tab_clom;
    }

    @PostMapping("/get_tab_info")
    public List<clom>  get_tab_info(@RequestBody String info){
        System.out.println(info);
        String[] split = info.split("\"");
        String regin = split[3];
        String table = split[7];
        System.out.println(regin+"     "+table);
        List<clom> tab_clom = get_tab_clom_service.get_tab_info(regin, table);
        System.out.println(tab_clom);
        return tab_clom;
    }
}
