package com.liujun;


import com.liujun.bean.A;
import com.liujun.bean.B;
import com.liujun.bean.ProjectBudget;
import com.liujun.utils.BeanFieldsUtils;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.*;

public class TestMain {
    private int a = 1;

    public static void main(String[] args) {
        A a = new A("a1", "a2");
        B b = new B("b1", "b2");
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        HashMap<String,String> fieldsMap = new HashMap<>();
        fieldsMap.put("age","password");
        BeanFieldsUtils.copyProperties(a, b, null, null, fieldsMap, false, null);
        System.out.println("`````````````````````````````````");
        System.out.println("a = " + a);
        System.out.println("b = " + b);
    }
    @Test
    public void test() {
        ProjectBudget budget = new ProjectBudget();
        budget.setKmmc("科目名称");
        budget.setKmfl("科目分类");
        budget.setMj(1L);
        budget.setRefId("refId");
        budget.setBz("备注");
        budget.setYsje(100L);
        //budget.setId("id");
        budget.setFormDataRev(1L);
        Map<String, Object> stringObjectMap = BeanFieldsUtils.copyModelToMap(budget);
        System.out.println("stringObjectMap = " + stringObjectMap);
    }
    @Test
    public void test2(){
        List<TestMain> list = new ArrayList<TestMain>(){};
        System.out.println("list = " + ((Class<?>)((ParameterizedType)list.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getDeclaredFields().length);
    }
    //输出26个小写字母
    @Test
    public void test3(){
        System.out.println("123123123123".substring(0,2));
        System.out.println("123123123123".substring(1));
    }
}
