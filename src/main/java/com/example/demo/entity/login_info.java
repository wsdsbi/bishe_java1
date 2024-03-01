package com.example.demo.entity;



public class login_info {
    public static void main(String[] args) {
        String s="{\"regin\":\"order\",\"table\":\"dwd_log_detail_d\"}";
        String[] split = s.split("\"");
        System.out.println(split);
        String regin = split[3];
        String table = split[7];
        System.out.println(regin+"     "+table);
    }
}
