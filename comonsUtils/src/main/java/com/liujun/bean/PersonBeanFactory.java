package com.liujun.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 类说明
 *
 * @author liujun
 * @date 2023/6/30
 */
public class PersonBeanFactory {
    public static String birthStr = "2000-01-01 01:02:03";
    public static Date birth;

    static {
        try {
            birth = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(birthStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static PersonA personA() {
        return new PersonA("迪迦", "1800", "男", "6000", "175", "2000-01-01 01:02:03");
    }

    public static PersonA personA2() {
        return new PersonA("塞罗", "1900", "男", "7000", "183", "2000-01-01 01:02:03");
    }

    public static Map<String, String> personAMap() {
        Map<String, String> person = new HashMap<>();
        person.put("name", "迪迦");
        person.put("f_age", "1800");
        person.put("f_sex", "男");
        person.put("f_weight", "6000");
        person.put("f_height", "175");
        person.put("f_birthDate", "2000-01-01 01:02:03");
        return person;
    }

    public static Map<String, String> personAMap2() {
        Map<String, String> person = new HashMap<>();
        person.put("name", "塞罗");
        person.put("f_age", "1900");
        person.put("f_sex", "男");
        person.put("f_weight", "7000");
        person.put("f_height", "183");
        person.put("f_birthDate", "2000-01-01 01:02:03");
        return person;
    }

    public static PersonB personB() {
        return new PersonB("大古", 18, 60L, 1.75, birth);
    }

    public static PersonB personB2() {
        return new PersonB("路人", 17, 70L, 1.83, birth);
    }

    public static Map<String, Object> personBMap() {
        Map<String, Object> person = new HashMap<>();
        person.put("name", "大古");
        person.put("age", 18);
        person.put("weight", 60L);
        person.put("height", 1.75);
        person.put("birthDay", birth);
        return person;
    }

    public static Map<String, Object> personBMap2() {
        Map<String, Object> person = new HashMap<>();
        person.put("name", "路人");
        person.put("age", 17);
        person.put("weight", 70L);
        person.put("height", 1.83);
        person.put("birthDay", birth);
        return person;
    }
}
