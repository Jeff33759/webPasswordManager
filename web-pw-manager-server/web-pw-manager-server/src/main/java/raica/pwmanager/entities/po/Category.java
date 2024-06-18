package raica.pwmanager.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_category")
public class Category {

    @TableId(value = "c_id", type = IdType.AUTO)
    private Integer id;

    @TableField("c_name")
    private String name;


}
