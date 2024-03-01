package com.example.demo.service.login;

import com.example.demo.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
@Service

public class login_service {
    public Boolean logincheck(User login_user_check, User user_info){
        if(login_user_check.account.equals(user_info.account)&&login_user_check.password.equals(user_info.password)){
            return true;
        }
        else {
            System.out.println("输入信息:"+login_user_check);
            System.out.println("存储信息:"+user_info);
            return false;
        }
    }
}
