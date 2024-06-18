package raica.pwmanager.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@TableName("t_tag_mapping_pw")
public class TagMappingPassword {

    @TableId(value = "tmp_id", type = IdType.AUTO)
    private Integer id;

    @TableField("tag_id")
    private int tagId;

    @TableField("p_id")
    private int passwordId;

    /**
     * 一筆t_tag_mapping_pw，映射到一筆password。
     * 所以關聯查詢時，可以用一筆t_tag_mapping_pw找出一筆password。
     */
    @TableField(exist = false)
    private Password password;

}
