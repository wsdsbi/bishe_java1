package bishe.service;

import bishe.entity.Metal_info;
import bishe.mapper.metal_order_Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class metal_order_Service {
    @Autowired
    metal_order_Mapper metal_order_mapper;
    public boolean create_metal_order(Metal_info metal_info){
       return metal_order_mapper.create_metal_order(metal_info);
    }

    public Metal_info[] show_metal_order(Metal_info metal_info){
        return metal_order_mapper.show_metal_order(metal_info);
    }

    public boolean insert_face_order(Metal_info metal_info){
        return metal_order_mapper.insert_face_order(metal_info);
    }
}
