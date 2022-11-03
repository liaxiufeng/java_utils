package com.liujun.bean;

/**
 * 类说明
 *
 * @author liujun
 * @date 2022/10/22
 */
public class A {
    private String name;
    private String age;

    public A() {
    }

    public A(String name, String age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "A{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                '}';
    }
}
