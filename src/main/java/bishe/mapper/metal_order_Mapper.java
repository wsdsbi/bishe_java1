package bishe.mapper;

import bishe.entity.Metal_info;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface metal_order_Mapper {
    boolean create_metal_order(Metal_info metal_info);
    Metal_info[] show_metal_order(Metal_info metal_info);
    boolean insert_face_order(Metal_info metal_info);
}
