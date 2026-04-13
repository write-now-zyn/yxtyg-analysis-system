package com.jscm.yxtyg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 体验官操作日志实体类
 */
@Data
@TableName("t_experimenter_log")
public class ExperimenterLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作类型：新增、编辑、删除、批量新增、批量删除 */
    private String operationType;

    /** 操作人 */
    private String operator;

    /** 操作时间 */
    private LocalDateTime operationTime;

    /** 体验官ID */
    private Long experimenterId;

    /** 地市 */
    private String city;

    /** 姓名 */
    private String name;

    /** 电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色 */
    private String role;

    /** 备注 */
    private String remark;

    /** 是否接口人 0-否 1-是 */
    private Integer isContact;

    /** 操作详情 */
    private String detail;

}
