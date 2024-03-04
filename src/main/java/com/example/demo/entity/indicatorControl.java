package com.example.demo.entity;



public class indicatorControl {
    String active;
    String regin;
    String tablename;
    String indicator;
    String comment;
    String account;
    String owned;
    String createtime;
    String updatetime;
    String endtime;

    @Override
    public String toString() {
        return "indicatorControl{" +
                "active='" + active + '\'' +
                ", regin='" + regin + '\'' +
                ", tablename='" + tablename + '\'' +
                ", indicator='" + indicator + '\'' +
                ", comment='" + comment + '\'' +
                ", account='" + account + '\'' +
                ", owned='" + owned + '\'' +
                ", createtime='" + createtime + '\'' +
                ", updatetime='" + updatetime + '\'' +
                ", endtime='" + endtime + '\'' +
                ", myaccount='" + myaccount + '\'' +
                '}';
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getRegin() {
        return regin;
    }

    public void setRegin(String regin) {
        this.regin = regin;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getOwned() {
        return owned;
    }

    public void setOwned(String owned) {
        this.owned = owned;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getMyaccount() {
        return myaccount;
    }

    public void setMyaccount(String myaccount) {
        this.myaccount = myaccount;
    }

    String myaccount;
}
