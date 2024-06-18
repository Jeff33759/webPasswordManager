package raica.pwmanager.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Accessors(chain = true)
@Data
@TableName("t_password")
public class Password {

    @TableId(value = "p_id", type = IdType.AUTO)
    private Integer id;

    @TableField("u_id")
    private int userId;

    @TableField("c_id")
    private int categoryId;

    @TableField("p_title")
    private String title;

    @TableField("web_url")
    private String webUrl;

    private String password;

    @TableField(value = "dynamic_entries", typeHandler = JacksonTypeHandler.class)
    private JsonNode dynamicEntries;

    private String remark;

    @TableField("p_create_time")
    private Timestamp createTime;

    @TableField("p_update_time")
    private Timestamp updateTime;

    /**
     * 一筆password可以被設置多個tag。
     * 所以關聯查詢時，一筆password可以抓出多筆tag。
     */
    @TableField(exist = false)
    private Set<Tag> tagSet = new HashSet<>();

}
