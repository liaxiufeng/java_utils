package com.liujun.test;

import com.liujun.bean.PersonA;
import com.liujun.bean.PersonB;
import com.liujun.bean.PersonBeanFactory;
import com.liujun.utils.BeanFieldsUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liujun.bean.PersonBeanFactory.personA;

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
        PersonA personA = personA();

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
    public void copyListPropertyToProperty() {
        ArrayList<PersonA> personAList = new ArrayList<PersonA>() {{
            add(personA());
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
        PersonA personA = personA();
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
            add(personA());
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

    @Test
    public void copyKeyToProperty() {
        Map<String, String> personAMap1 = PersonBeanFactory.personAMap();
        PersonA personA = personA();
        BeanFieldsUtils.copyKeyToProperty(personAMap1, personA);
        log(personAMap1, personA);

        Map<String, Object> personBMap = PersonBeanFactory.personBMap();
        PersonB personB = new PersonB();
        BeanFieldsUtils.copyKeyToProperty(personBMap, personB);
        log(personBMap, personB);
    }

    @Test
    public void copyListKeyToProperty() {
        List<Map<String, String>> personAMapList = new ArrayList<Map<String, String>>() {{
            add(PersonBeanFactory.personAMap());
            add(PersonBeanFactory.personAMap2());
        }};
        List<PersonA> personAList = new ArrayList<PersonA>() {
        };
        BeanFieldsUtils.copyListKeyToProperty(personAMapList, personAList);
        log(personAMapList, personAList);

        List<Map<String, Object>> personBMapList = new ArrayList<Map<String, Object>>() {{
            add(PersonBeanFactory.personBMap());
            add(PersonBeanFactory.personBMap2());
        }};
        List<PersonB> personBList = new ArrayList<PersonB>() {
        };
        BeanFieldsUtils.copyListKeyToProperty(personBMapList, personBList);
        log(personBMapList, personBList);
    }

    @Test
    public void copyKeyToKey() {
        Map<String, String> personAMap = PersonBeanFactory.personAMap();
        Map<String, String> personA = new HashMap<String, String>() {
        };
        BeanFieldsUtils.copyKeyToKey(personAMap, personA);
        log(personAMap, personA);

        Map<String, Object> personA2 = new HashMap<String, Object>() {
        };
        BeanFieldsUtils.copyKeyToKey(personAMap, personA2);
        log(personAMap, personA2);

        Map<String, Object> personBMap = PersonBeanFactory.personBMap();
        Map<String, Object> personB = new HashMap<String, Object>() {
        };
        BeanFieldsUtils.copyKeyToKey(personBMap, personB);
        log(personBMap, personB);

        Map<String, String> personB2 = new HashMap<String, String>() {
        };
        BeanFieldsUtils.copyKeyToKey(personBMap, personB2);
        log(personBMap, personB2);
    }

    @Test
    public void copyListKeyToKey() {
        List<Map<String, String>> personAMapList = new ArrayList<Map<String, String>>() {{
            add(PersonBeanFactory.personAMap());
            add(PersonBeanFactory.personAMap2());
        }};
        List<Map<String, Object>> personAList = new ArrayList<Map<String, Object>>() {
        };
        BeanFieldsUtils.copyListKeyToKey(personAMapList, personAList);
        log(personAMapList, personAList);

        List<Map<String, Object>> personBMapList = new ArrayList<Map<String, Object>>() {{
            add(PersonBeanFactory.personBMap());
            add(PersonBeanFactory.personBMap2());
        }};
        List<Map<String, Object>> personBList = new ArrayList<Map<String, Object>>() {
        };
        BeanFieldsUtils.copyListKeyToKey(personBMapList, personBList);
        log(personBMapList, personBList);
    }

}
