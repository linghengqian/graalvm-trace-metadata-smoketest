

package com.lingh.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@ToString
public class OrderStatisticsInfo implements Serializable {
    
    private static final long serialVersionUID = -1770007969944794302L;
    
    private Long id;
    
    private Long userId;
    
    private LocalDate orderDate;
    
    private int orderNum;
}
