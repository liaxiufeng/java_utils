package com.liujun.bean;

/**
 * 类说明
 *
 * @author liujun
 * @date 2022/10/22
 */
public class B {
    private String name;
    private String password;

    public B() {
    }

    public B(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "B{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
