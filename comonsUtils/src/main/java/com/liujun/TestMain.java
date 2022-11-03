package com.liujun;


import com.liujun.bean.A;
import com.liujun.bean.B;
import com.liujun.bean.ProjectBudget;
import com.liujun.utils.BeanFieldsUtils;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class TestMain {

    public static void main(String[] args) throws ParseException {
        A a = new A("a1", "a2");
        B b = new B("b1", "b2");
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        HashMap<String,String> fieldsMap = new HashMap<>();
        fieldsMap.put("age","password");
        BeanFieldsUtils.copyProperties(a, b, null, fieldsMap,false);
        System.out.println("`````````````````````````````````");
        System.out.println("a = " + a);
        System.out.println("b = " + b);
    }
    @Test
    public void test() throws ParseException {
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
}
