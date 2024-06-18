CREATE DATABASE IF NOT EXISTS `raica_pw_manager` DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

USE `raica_pw_manager`;

CREATE TABLE IF NOT EXISTS `t_user` (
    `u_id` INT UNSIGNED AUTO_INCREMENT ,
    `u_name` VARCHAR(20) NOT NULL,
    `email` VARCHAR(50) UNIQUE NOT NULL,
    `main_password` VARCHAR(44) NOT NULL COMMENT '玩家登入系統的密碼。明文上限20位，透過AES/ECB/PKCS7/128bits加密成暗文存放',
    `is_activated` BOOLEAN DEFAULT false COMMENT '是否已激活。0=未激活，1=已激活',
    `mfa_type` INT DEFAULT 0 COMMENT '多重身份認證的類型。0=未設置。1=郵箱認證。',
    `u_create_time` Timestamp NOT NULL,
    `u_update_time` Timestamp NOT NULL,
    PRIMARY KEY (`u_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='會員表';


CREATE TABLE IF NOT EXISTS `t_category` (
    `c_id` INT UNSIGNED AUTO_INCREMENT ,
    `c_name` VARCHAR(20) NOT NULL,
    PRIMARY KEY (`c_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='分類表';


CREATE TABLE IF NOT EXISTS `t_password` (
    `p_id` INT UNSIGNED AUTO_INCREMENT ,
    `u_id` INT UNSIGNED NOT NULL,
    `c_id` INT UNSIGNED NOT NULL,
    `p_title` VARCHAR(50) NOT NULL COMMENT '密碼的標題',
    `web_url` VARCHAR(200) DEFAULT '' COMMENT '密碼用於哪個網站',
    `password` VARCHAR(100) NOT NULL COMMENT '所有密碼明文上限20位，透過AES/ECB/PKCS7/128bits加密成暗文存放',
    `dynamic_entries` JSON COMMENT '密碼條目的動態欄位，json範例為: [{"blockName":"區塊名稱，可空","props":[{"title":"屬性標題，可空","content":"屬性內容"},{"title":"屬性標題2，可空","content":"屬性內容2"}]}]',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '備註',
    `p_create_time` TIMESTAMP NOT NULL,
    `p_update_time` TIMESTAMP NOT NULL,
    FOREIGN KEY (`u_id`) REFERENCES `t_user`(u_id),
    FOREIGN KEY (`c_id`) REFERENCES `t_category`(c_id),
    PRIMARY KEY (`p_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='密碼表';


CREATE TABLE IF NOT EXISTS `t_tag` (
    `tag_id` INT UNSIGNED AUTO_INCREMENT ,
    `u_id` INT UNSIGNED NOT NULL,
    `tag_name` VARCHAR(20) NOT NULL,
    FOREIGN KEY(`u_id`) REFERENCES `t_user`(u_id),
    PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='標籤表';


CREATE TABLE IF NOT EXISTS `t_tag_mapping_pw` (
   `tmp_id` INT UNSIGNED AUTO_INCREMENT ,
   `tag_id` INT UNSIGNED NOT NULL,
   `p_id` INT UNSIGNED NOT NULL,
    FOREIGN KEY(`tag_id`) REFERENCES `t_tag`(tag_id),
    FOREIGN KEY(`p_id`) REFERENCES `t_password`(p_id),
    PRIMARY KEY (`tmp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='標籤-密碼映射表';

INSERT INTO `t_category` VALUES
(NULL, '一級機密'),
(NULL, '二級機密');