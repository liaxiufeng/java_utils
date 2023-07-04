package com.liujun.test;

import com.liujun.bean.PersonA;
import com.liujun.bean.PersonB;
import com.liujun.bean.PersonBeanFactory;
import com.liujun.utils.BeanFieldsUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 类说明
 *
 * @author liujun
 * @date 2023/6/30
 */
public class BeanFieldsTest {

    private void log(Object source, Object target) {
        System.out.println(source.toString());
        System.out.println(target.toString());
        System.out.println("=========================================");
    }

    @Test
    public void copyPropertyToProperty() {
        PersonA personA = PersonBeanFactory.personA();

        PersonA emptyPersonA = new PersonA();
        BeanFieldsUtils.copyPropertyToProperty(personA, emptyPersonA, false, null, null, null, false, null);
        log(personA, emptyPersonA);

        PersonB emptyPersonB = new PersonB();
        BeanFieldsUtils.copyPropertyToProperty(personA, emptyPersonB, false, null, null, null, false, null);
        log(personA, emptyPersonB);

        PersonB personB = PersonBeanFactory.personB();
        emptyPersonB = new PersonB();
        BeanFieldsUtils.copyPropertyToProperty(personB, emptyPersonB, false, null, null, null, false, null);
        log(personB, emptyPersonB);

        emptyPersonA = new PersonA();
        BeanFieldsUtils.copyPropertyToProperty(personB, emptyPersonA, false, null, null, null, false, null);
        log(personB, emptyPersonA);

        emptyPersonB = new PersonB();
        BeanFieldsUtils.copyPropertyToProperty(personA, emptyPersonB,
                false,
                new ArrayList<String>() {{
                    add("weight");
                }},
                new ArrayList<String>() {{
                    add("height");
                }},
                new HashMap<String, String>() {{
                    put("name", "name");
                }},
                false,
                (sourceFieldName, targetFieldName) -> {
                    return "birthDate".equals(sourceFieldName) && "birthDay".equals(targetFieldName);
                }
        );
        log(personA, emptyPersonB);

        emptyPersonB = new PersonB();
        BeanFieldsUtils.copyPropertyToProperty(personA, emptyPersonB,
                false,
                new ArrayList<String>() {{
                    add("weight");
                }},
                new ArrayList<String>() {{
                    add("height");
                }},
                new HashMap<String, String>() {{
                    put("name", "name");
                }},
                true,
                (sourceFieldName, targetFieldName) -> "birthDate".equals(sourceFieldName) && "birthDay".equals(targetFieldName)
        );
        log(personA, emptyPersonB);

        emptyPersonB = new PersonB();
        BeanFieldsUtils.copyPropertyToProperty(personA, emptyPersonB,
                false,
                null,
                null,
                new HashMap<String, String>() {{
                    put("birthDate", "birthDay");
                }},
                false,
                (sourceFieldName, targetFieldName) -> "age".equals(sourceFieldName) && "height".equals(targetFieldName)
        );
        log(personA, emptyPersonB);

        emptyPersonB = new PersonB();
        BeanFieldsUtils.copyPropertyToProperty(personA, emptyPersonB,
                true,
                null,
                null,
                new HashMap<String, String>() {{
                    put("birthDate", "birthDay");
                }},
                false,
                (sourceFieldName, targetFieldName) -> "age".equals(sourceFieldName) && "height".equals(targetFieldName)
        );
        log(personA, emptyPersonB);
    }

    @Test
    public void copyListPropertyToProperty() throws InstantiationException, IllegalAccessException {
        ArrayList<PersonA> personAList = new ArrayList<PersonA>() {{
            add(PersonBeanFactory.personA());
            add(PersonBeanFactory.personA2());
        }};
        ArrayList<PersonA> emptypersonAS = new ArrayList<PersonA>() {
        };
        BeanFieldsUtils.copyListPropertyToProperty(personAList, emptypersonAS);
        log(personAList, emptypersonAS);

        ArrayList<PersonB> personBList = new ArrayList<PersonB>() {{
            add(PersonBeanFactory.personB());
            add(PersonBeanFactory.personB2());
        }};
        ArrayList<PersonB> emptypersonBS = new ArrayList<PersonB>() {
        };
        BeanFieldsUtils.copyListPropertyToProperty(personBList, emptypersonBS);
        log(personBList, emptypersonBS);
    }

    @Test
    public void copyPropertyToKey() {
        PersonA personA = PersonBeanFactory.personA();
        Map<String, Object> emptyPersonA = new HashMap<String, Object>() {
        };
        BeanFieldsUtils.copyPropertyToKey(personA, emptyPersonA);
        log(personA, emptyPersonA);

        Map<String, String> emptyPersonA2 = new HashMap<String, String>() {
        };
        BeanFieldsUtils.copyPropertyToKey(personA, emptyPersonA2);
        log(personA, emptyPersonA2);

        PersonB personB = PersonBeanFactory.personB();
        Map<String, Object> emptyPersonB = new HashMap<String, Object>() {
        };
        BeanFieldsUtils.copyPropertyToKey(personB, emptyPersonB);
        log(personB, emptyPersonB);

        Map<String, String> emptyPersonB2 = new HashMap<String, String>() {
        };

        BeanFieldsUtils.copyPropertyToKey(personB, emptyPersonB2);
        log(personB, emptyPersonB2);
    }

    @Test
    public void copyListPropertyToKey() {
        ArrayList<PersonA> personAList = new ArrayList<PersonA>() {{
            add(PersonBeanFactory.personA());
            add(PersonBeanFactory.personA2());
        }};
        ArrayList<Map<String, Object>> emptypersonAS = new ArrayList<Map<String, Object>>() {
        };
        BeanFieldsUtils.copyListPropertyToKey(personAList, emptypersonAS);
        log(personAList, emptypersonAS);

        ArrayList<PersonB> personBList = new ArrayList<PersonB>() {{
            add(PersonBeanFactory.personB());
            add(PersonBeanFactory.personB2());
        }};
        ArrayList<Map<String, Object>> emptypersonBS = new ArrayList<Map<String, Object>>() {
        };
        BeanFieldsUtils.copyListPropertyToKey(personBList, emptypersonBS);
        log(personBList, emptypersonBS);
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
