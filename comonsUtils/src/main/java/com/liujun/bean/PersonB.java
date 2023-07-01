package com.liujun.bean;

import lombok.*;

import java.util.Date;

/**
 * 类说明
 *
 * @author liujun
 * @date 2023/6/30
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PersonB {

    private static final long serialVersionUID = 2L;

    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    private int age;

    /**
     * 体重
     */
    private Long weight;

    /**
     * 身高
     */
    private double height;

    /**
     * 生日
     */
    private Date birthDay;

}