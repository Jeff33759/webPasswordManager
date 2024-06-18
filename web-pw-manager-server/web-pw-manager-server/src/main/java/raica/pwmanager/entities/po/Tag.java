package raica.pwmanager.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Accessors(chain = true)
@Data
@TableName("t_tag")
public class Tag {

    @TableId(value = "tag_id", type = IdType.AUTO)
    private Integer id;

    @TableField("u_id")
    private int userId;

    @TableField("tag_name")
    private String name;

    /**
     * 一筆tag可以對應到多筆"tag-password映射資料"。
     * 所以關聯查詢時，一筆tag可以抓出多筆tag-password映射，tag-password映射可以再映射回對應的password，進而達成"用一筆tag找出底下對應的所以password"。
     * 順便一提，一筆password可以被標上多個tag。
     */
    @TableField(exist = false)
    private Set<TagMappingPassword> tagMappingPasswordSet = new HashSet<>();

}
