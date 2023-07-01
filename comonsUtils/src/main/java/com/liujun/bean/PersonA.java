package com.liujun.bean;

import lombok.*;

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
public class PersonA {

    private static final long serialVersionUID = 1L;

    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    private String age;

    /**
     * 性别
     */
    private String sex;

    /**
     * 体重
     */
    private String weight;

    /**
     * 身高
     */
    private String height;

    /**
     * 生日
     */
    private String birthDate;

}

