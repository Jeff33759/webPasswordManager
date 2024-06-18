package raica.pwmanager.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Accessors(chain = true)
@Data
@TableName("t_user")
public class User {

    @TableId(value = "u_id", type = IdType.AUTO)
    private Integer id;

    @TableField("u_name")
    private String name;

    private String email;

    @TableField("main_password")
    private String mainPassword;

    @TableField("is_activated")
    private boolean activated;

    @TableField("mfa_type")
    private int mfaType;

    @TableField("u_create_time")
    private Timestamp createTime;

    @TableField("u_update_time")
    private Timestamp updateTime;

    /**
     * 一個User可以新增多組password。
     * 所以關聯查詢時，一筆User可以抓出多筆password。
     */
    @TableField(exist = false)
    private Set<Password> passwordSet = new HashSet<>();

    /**
     * 一個User可以新增多個Tag。
     * 所以關聯查詢時，一筆User可以抓出多筆Tag。
     */
    @TableField(exist = false)
    private Set<Tag> tagSet = new HashSet<>();

}
