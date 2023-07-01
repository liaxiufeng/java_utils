package com.liujun.test;

import com.liujun.bean.PersonA;
import com.liujun.bean.PersonB;
import com.liujun.bean.PersonBeanFactory;
import com.liujun.utils.BeanFieldsUtils;
import org.junit.Test;

/**
 * 类说明
 *
 * @author liujun
 * @date 2023/6/30
 */
public class BeanFieldsTest {

    private void log(Object source, Object target){
        System.out.println(source.toString());
        System.out.println(target.toString());
        System.out.println("=========================================");
    }

    @Test
    public void copyPropertyToProperty() {
        PersonA personA = PersonBeanFactory.personA();

        PersonA emptyPersonA = new PersonA();
        BeanFieldsUtils.copyPropertyToProperty(personA, emptyPersonA, null, null, null, false, null);
        log(personA, emptyPersonA);

        PersonB emptyPersonB = new PersonB();
        BeanFieldsUtils.copyPropertyToProperty(personA, emptyPersonB, null, null, null, false, null);
        log(personA, emptyPersonB);

        PersonB personB = PersonBeanFactory.personB();
        emptyPersonB = new PersonB();
        BeanFieldsUtils.copyPropertyToProperty(personB, emptyPersonB, null, null, null, false, null);
        log(personB, emptyPersonB);

        emptyPersonA = new PersonA();
        BeanFieldsUtils.copyPropertyToProperty(personB, emptyPersonA, null, null, null, false, null);
        log(personB, emptyPersonA);
    }

    public void copyListPropertyToProperty() {
    }

    public void copyPropertyToKey() {
    }

    public void copyListPropertyToKey() {
    }

    public void copyKeyToProperty() {
    }

    public void copyListKeyToProperty() {
    }

    public void copyKeyToKey() {
    }

    public void copyListKeyToKey() {
    }

}
