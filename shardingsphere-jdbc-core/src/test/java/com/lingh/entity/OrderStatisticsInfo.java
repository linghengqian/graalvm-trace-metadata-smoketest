package com.lingh.entity;

import java.io.Serializable;
import java.time.LocalDate;

public class OrderStatisticsInfo implements Serializable {

    private static final long serialVersionUID = -1770007969944794302L;

    private Long id;

    private Long userId;

    private LocalDate orderDate;

    private int orderNum;

    public Long getId() {
        return this.id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public LocalDate getOrderDate() {
        return this.orderDate;
    }

    public int getOrderNum() {
        return this.orderNum;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public String toString() {
        return "OrderStatisticsInfo(id=" + this.getId() + ", userId=" + this.getUserId() + ", orderDate=" + this.getOrderDate() + ", orderNum=" + this.getOrderNum() + ")";
    }
}
