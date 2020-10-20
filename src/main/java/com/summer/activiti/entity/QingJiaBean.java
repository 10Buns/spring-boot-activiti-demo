package com.summer.activiti.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: QingJiaBean
 * @description:
 * @author: summer
 * @create: 2020-10-15 14:26
 **/
public class QingJiaBean implements Serializable {

    private static final long serialVersionUID = 5517098709027128149L;

    private Integer id;
    private String reason;//理由
    private String applicant;//申请人
    private Date date;//申请日期

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
