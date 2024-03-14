package bishe.controller;

import bishe.entity.Metal_info;
import bishe.service.metal_order_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class metal_order_Controller {
    @Autowired
    metal_order_Service metal_order_service;


    @PostMapping("/create_metal_order")
    public boolean create_metal_order(@RequestBody Metal_info metal_info){
        System.out.println(metal_info);
        return metal_order_service.create_metal_order(metal_info);
    }

    @PostMapping("/show_metal_order")
    public Metal_info[] show_metal_order(@RequestBody Metal_info metal_info){
        return metal_order_service.show_metal_order(metal_info);
    }

    @PostMapping("/insert_face_order")
    public boolean insert_face_order(@RequestBody Metal_info metal_info){
        System.out.println(metal_info);
        return metal_order_service.insert_face_order(metal_info);
    }
}
