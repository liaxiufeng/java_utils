package com.liujun.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 类说明
 *
 * @author 刘军
 * @date 2022/10/22
 */
public class BeanFieldsUtils {

    /**
     * 复制对象的属性值
     *
     * @param source                  源对象
     * @param target                  目标对象
     * @param ignoreTargetFields      忽略目标对象的属性
     * @param sourceToTargetFieldsMap 源对象的属性与目标对象的属性的映射关系
     * @param ignoreOutOfMap          是否忽略映射关系之外的属性
     */
    public static void copyProperties(Object source, Object target, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap) {
        if (source == null || target == null) {
            return;
        }
        Field[] sourceFieldsAll = source.getClass().getDeclaredFields();
        Field[] targetFieldsAll = target.getClass().getDeclaredFields();
        Stream<Field> sourceFieldsStream = Arrays.stream(sourceFieldsAll);
        Stream<Field> targetFieldsStream = Arrays.stream(targetFieldsAll);
        if (ignoreTargetFields != null && !ignoreTargetFields.isEmpty()) {
            targetFieldsStream = targetFieldsStream.filter(field -> !ignoreTargetFields.contains(field.getName()));
        }
        if (ignoreOutOfMap && sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty()) {
            sourceFieldsStream = sourceFieldsStream.filter(field -> sourceToTargetFieldsMap.containsKey(field.getName()));
            targetFieldsStream = targetFieldsStream.filter(field -> sourceToTargetFieldsMap.containsValue(field.getName()));
        }
        List<Field> sourceFields = sourceFieldsStream.collect(Collectors.toList());
        List<Field> targetFields = targetFieldsStream.collect(Collectors.toList());
        for (Field targetField : targetFields) {
            String targetFieldName = targetField.getName();
            for (Field sourceField : sourceFields) {
                String sourceFieldName = sourceField.getName();
                if ((sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty() && targetFieldName.equals(sourceToTargetFieldsMap.get(sourceFieldName))) || targetFieldName.equals(sourceFieldName)) {
                    try {
                        targetField.setAccessible(true);
                        sourceField.setAccessible(true);
                        targetField.set(target, sourceField.get(source));
                        break;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }


    /**
     * 复制对象相同属性名的值
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, null, null, false);
    }

    interface FieldNameMapperHandler {
        String handler(String fieldName);
    }

    /**
     * 将对象属性按指定规制映射map
     *
     * @param source             源对象
     * @param ignoreSourceFields 忽略源对象的属性
     * @param propertiesToMap    映射关系(源对象的属性名=>map的key)
     * @param ignoreOutOfMap     是否忽略映射关系之外的源对象的属性
     * @param commonHandler      处理器(源对象的属性名=>map的key)
     */
    public static Map<String, Object> copyPropertiesToMap(Object source, List<String> ignoreSourceFields, Map<String, String> propertiesToMap, boolean ignoreOutOfMap, FieldNameMapperHandler commonHandler) {
        if (source == null) {
            return null;
        }
        Field[] sourceFieldsAll = source.getClass().getDeclaredFields();
        Stream<Field> sourceFieldsStream = Arrays.stream(sourceFieldsAll);
        if (ignoreSourceFields != null && !ignoreSourceFields.isEmpty()) {
            sourceFieldsStream = sourceFieldsStream.filter(field -> !ignoreSourceFields.contains(field.getName()));
        }
        if (ignoreOutOfMap && propertiesToMap != null && !propertiesToMap.isEmpty()) {
            sourceFieldsStream = sourceFieldsStream.filter(field -> propertiesToMap.containsKey(field.getName()));
        }
        List<Field> sourceFields = sourceFieldsStream.collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        for (Field sourceField : sourceFields) {
            String sourceFieldName = sourceField.getName();
            String mapKey = sourceFieldName;
            if (propertiesToMap != null && !propertiesToMap.isEmpty() && propertiesToMap.containsKey(sourceFieldName)) {
                mapKey = propertiesToMap.get(sourceFieldName);
            } else if (commonHandler != null) {
                mapKey = commonHandler.handler(sourceFieldName);
            }
            try {
                sourceField.setAccessible(true);
                map.put(mapKey, sourceField.get(source));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static Map<String, Object> copyPropertiesToMap(Object source, List<String> ignoreFields, FieldNameMapperHandler commonHandler) {
        return copyPropertiesToMap(source, ignoreFields, null, false, commonHandler);
    }

    public static Map<String, Object> copyPropertiesToMap(Object source) {
        return copyPropertiesToMap(source, null, null, false, null);
    }

    /**
     * 将map的值复制到对象的属性
     *
     * @param source          源map
     * @param target          目标对象
     * @param ignoreMapKey    忽略map的key
     * @param mapToProperties 映射关系(map的key=>目标对象的属性名)
     * @param ignoreOutOfMap  是否忽略映射关系之外的map的key
     * @param commonHandler   处理器(map的key=>目标对象的属性名)
     */
    public static void copyMapToProperties(Map<String, Object> source, Object target, List<String> ignoreMapKey, Map<String, String> mapToProperties, boolean ignoreOutOfMap, FieldNameMapperHandler commonHandler) {
        if (source == null || target == null) {
            return;
        }
        Stream<String> sourceKeyStream = source.keySet().stream();
        if (ignoreMapKey != null && !ignoreMapKey.isEmpty()) {
            sourceKeyStream = sourceKeyStream.filter(key -> !ignoreMapKey.contains(key));
        }
        if (ignoreOutOfMap && mapToProperties != null && !mapToProperties.isEmpty()) {
            sourceKeyStream = sourceKeyStream.filter(mapToProperties::containsKey);
        }
        Field[] targetFieldsAll = target.getClass().getDeclaredFields();
        for (Field targetField : targetFieldsAll) {
            String targetFieldName = targetField.getName();
            for (String sourceKey : sourceKeyStream.collect(Collectors.toList())) {
                if (
                        (commonHandler != null && commonHandler.handler(sourceKey).equals(targetFieldName)) ||
                                (mapToProperties != null && !mapToProperties.isEmpty() && targetFieldName.equals(mapToProperties.get(sourceKey)))
                                || targetFieldName.equals(sourceKey)
                ) {
                    try {
                        targetField.setAccessible(true);
                        targetField.set(target, source.get(sourceKey));
                        break;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 将model对象转化为外部表的map
     */
    public static Map<String, Object> copyModelToMap(Object model) {
        Map<String, String> propertiesToMap = new HashMap<>();
        propertiesToMap.put("id", "id_");
        propertiesToMap.put("refId", "ref_id_");
        return copyPropertiesToMap(model, Arrays.asList("formDataRev","serialVersionUID"), propertiesToMap, false, name -> String.format("f_%s", name).toUpperCase());
    }

    /**
     * 将外部表的map转化为model
     */
    public static void copyMapToModel(Map<String, Object> map, Object model) {
        Map<String, String> mapToProperties = new HashMap<>();
        mapToProperties.put("id_", "id");
        mapToProperties.put("ref_id_", "refId");
        copyMapToProperties(map, model, null, mapToProperties, false, name -> name.substring(2));
    }
}
