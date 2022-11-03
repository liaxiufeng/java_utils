package com.liujun.bean;


public class ProjectBudget {
    /**
    * 主键
    */
    private String id;

    /**
    * 外键
    */
    private String refId;

    /**
    * 密级
    */
    private Long mj;

    /**
    * 科目名称
    */
    private String kmmc;

    /**
    * 预算金额
    */
    private Long ysje;

    /**
    * 备注
    */
    private String bz;

    /**
    * 表单数据版本
    */
    private Long formDataRev;

    /**
    * 科目分类
    */
    private String kmfl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public Long getMj() {
        return mj;
    }

    public void setMj(Long mj) {
        this.mj = mj;
    }

    public String getKmmc() {
        return kmmc;
    }

    public void setKmmc(String kmmc) {
        this.kmmc = kmmc;
    }

    public Long getYsje() {
        return ysje;
    }

    public void setYsje(Long ysje) {
        this.ysje = ysje;
    }

    public String getBz() {
        return bz;
    }

    public void setBz(String bz) {
        this.bz = bz;
    }

    public Long getFormDataRev() {
        return formDataRev;
    }

    public void setFormDataRev(Long formDataRev) {
        this.formDataRev = formDataRev;
    }

    public String getKmfl() {
        return kmfl;
    }

    public void setKmfl(String kmfl) {
        this.kmfl = kmfl;
    }
}