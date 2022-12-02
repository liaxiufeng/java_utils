package com.liujun.utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 属性复制工具类
 *
 * @author 刘军
 * @date 2022/10/22
 */
public class BeanFieldsUtils {


    /**
     * 属性映射处理器
     * lambda表达式: 源对象属性名或map的key -> 映射后的对象属性名或map的key
     */
    interface FieldNameMapperHandler {
        String handler(String fieldName);
    }

    /**
     * 复制对象的属性值
     *
     * @param source                  源对象
     * @param target                  目标对象
     * @param ignoreTargetFields      忽略目标对象的属性
     * @param sourceToTargetFieldsMap 源对象的属性与目标对象的属性的映射关系
     * @param ignoreOutOfMap          是否忽略映射关系之外的属性
     */
    public static <S, T> void copyProperties(S source, T target, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap) {
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
     * 复制多个对象的属性值
     *
     * @param sources                 源对象列表
     * @param targetClass             目标对象的类
     * @param ignoreTargetFields      忽略目标对象的属性
     * @param sourceToTargetFieldsMap 源对象的属性与目标对象的属性的映射关系
     * @param ignoreOutOfMap          是否忽略映射关系之外的属性
     */
    public static <S, T> List<T> copyPropertiesByList(List<S> sources, Class<T> targetClass, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap) throws InstantiationException, IllegalAccessException {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        Class<?> sourceClass = sources.get(0).getClass();
        Field[] sourceFieldsAll = sourceClass.getDeclaredFields();
        Field[] targetFieldsAll = targetClass.getDeclaredFields();
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
        Map<Field, Field> fieldMap = new HashMap<>();
        for (Field targetField : targetFields) {
            String targetFieldName = targetField.getName();
            for (Field sourceField : sourceFields) {
                String sourceFieldName = sourceField.getName();
                if ((sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty() && targetFieldName.equals(sourceToTargetFieldsMap.get(sourceFieldName))) || targetFieldName.equals(sourceFieldName)) {
                    fieldMap.put(sourceField, targetField);
                }
            }
        }
        List<T> targetList = new LinkedList<>();
        for (S s : sources) {
            T t = targetClass.newInstance();
            for (Map.Entry<Field, Field> fieldFieldEntry : fieldMap.entrySet()) {
                Field sourceField = fieldFieldEntry.getKey();
                Field targetField = fieldFieldEntry.getValue();
                try {
                    targetField.setAccessible(true);
                    sourceField.setAccessible(true);
                    targetField.set(t, sourceField.get(s));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            targetList.add(t);
        }
        return targetList;
    }

    /**
     * 复制对象相同属性名的值
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static <S, T> void copyProperties(S source, T target) {
        copyProperties(source, target, null, null, false);
    }

    /**
     * 复制多个对象相同属性名的值
     *
     * @param sources     源对象列表
     * @param targetClass 目标对象的类
     */
    public static <S, T> List<T> copyPropertiesByList(List<S> sources, Class<T> targetClass) throws IllegalAccessException, InstantiationException {
        return copyPropertiesByList(sources, targetClass, null, null, false);
    }


    /**
     * 将多个对象（按照相同的映射关系）复制到map列表中
     *
     * @param sources            源对象列表
     * @param ignoreSourceFields 忽略源对象的属性
     * @param propertiesToMap    映射关系
     * @param ignoreOutOfMap     是否忽略映射关系之外的属性
     * @param commonHandler      公共映射处理器
     */
    public static <S> List<Map<String, Object>> copyPropertiesToMapByList(List<S> sources, List<String> ignoreSourceFields, Map<String, String> propertiesToMap, boolean ignoreOutOfMap, FieldNameMapperHandler commonHandler) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        Object source = sources.get(0);
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
        Map<Field, String> fieldsToMap = new HashMap<>();
        for (Field sourceField : sourceFields) {
            String sourceFieldName = sourceField.getName();
            String mapKey = sourceFieldName;
            if (propertiesToMap != null && !propertiesToMap.isEmpty() && propertiesToMap.containsKey(sourceFieldName)) {
                mapKey = propertiesToMap.get(sourceFieldName);
            } else if (commonHandler != null) {
                mapKey = commonHandler.handler(sourceFieldName);
            }
            sourceField.setAccessible(true);
            fieldsToMap.put(sourceField, mapKey);
        }
        return copyPropertiesToMapByList(sources, fieldsToMap);
    }

    /**
     * 将多个对象复制到map列表中
     *
     * @param sources         源对象
     * @param propertiesToMap 属性与map的key的映射关系
     */
    private static <S> List<Map<String, Object>> copyPropertiesToMapByList(List<S> sources, Map<Field, String> propertiesToMap) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> maps = new LinkedList<>();
        //遍历propertiesToMap
        for (S source : sources) {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<Field, String> entry : propertiesToMap.entrySet()) {
                Field field = entry.getKey();
                String mapKey = entry.getValue();
                field.setAccessible(true);
                try {
                    map.put(mapKey, field.get(source));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            maps.add(map);
        }
        return maps;
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
    public static <S> Map<String, Object> copyPropertiesToMap(S source, List<String> ignoreSourceFields, Map<String, String> propertiesToMap, boolean ignoreOutOfMap, FieldNameMapperHandler commonHandler) {
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

    /**
     * 将对象属性按指定规制映射map
     *
     * @param source        源对象
     * @param ignoreFields  忽略源对象的属性
     * @param commonHandler 处理器(源对象的属性名=>map的key)
     */
    public static <S> Map<String, Object> copyPropertiesToMap(S source, List<String> ignoreFields, FieldNameMapperHandler commonHandler) {
        return copyPropertiesToMap(source, ignoreFields, null, false, commonHandler);
    }

    /**
     * 将对象的属性映射到map
     */
    public static <S> Map<String, Object> copyPropertiesToMap(S source) {
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
    public static <T> void copyMapToProperties(Map<String, Object> source, T target, List<String> ignoreMapKey, Map<String, String> mapToProperties, boolean ignoreOutOfMap, FieldNameMapperHandler commonHandler) {
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
     * 将map的值复制到对象的属性
     *
     * @param sourceMapList    源map的列表
     * @param modelTargetClass 目标对象的class
     * @param ignoreMapKey     忽略map的key
     * @param mapToProperties  映射关系(map的key=>目标对象的属性名)
     * @param ignoreOutOfMap   是否忽略映射关系之外的map的key
     * @param commonHandler    处理器(map的key=>目标对象的属性名)
     */
    public static <T> List<T> copyMapToPropertiesByList(List<Map<String, Object>> sourceMapList, Class<T> modelTargetClass, List<String> ignoreMapKey, Map<String, String> mapToProperties, boolean ignoreOutOfMap, FieldNameMapperHandler commonHandler) {
        if (sourceMapList == null || sourceMapList.isEmpty()) {
            return null;
        }
        if (modelTargetClass == null) {
            return null;
        }
        Stream<String> sourceKeyStream = sourceMapList.get(0).keySet().stream();
        if (ignoreMapKey != null && !ignoreMapKey.isEmpty()) {
            sourceKeyStream = sourceKeyStream.filter(key -> !ignoreMapKey.contains(key));
        }
        if (ignoreOutOfMap && mapToProperties != null && !mapToProperties.isEmpty()) {
            sourceKeyStream = sourceKeyStream.filter(mapToProperties::containsKey);
        }
        Field[] targetFieldsAll = modelTargetClass.getDeclaredFields();
        List<String> sourcesFieldNamesAll = sourceKeyStream.collect(Collectors.toList());
        Map<String, Field> keyToFieldMap = new HashMap<>();
        for (Field targetField : targetFieldsAll) {
            String targetFieldName = targetField.getName();
            for (String sourceKey : sourcesFieldNamesAll) {
                if (
                        (commonHandler != null && commonHandler.handler(sourceKey).equals(targetFieldName)) ||
                                (mapToProperties != null && !mapToProperties.isEmpty() && targetFieldName.equals(mapToProperties.get(sourceKey)))
                                || targetFieldName.equals(sourceKey)
                ) {
                    keyToFieldMap.put(sourceKey, targetField);
                }
            }
        }
        List<T> targetResults = new ArrayList<>();
        for (Map<String, Object> source : sourceMapList) {
            T target = null;
            try {
                target = modelTargetClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (target == null) {
                continue;
            }
            for (String sourceKey : sourcesFieldNamesAll) {
                Field targetField = keyToFieldMap.get(sourceKey);
                if (targetField == null) {
                    continue;
                }
                try {
                    targetField.setAccessible(true);
                    Class<?> type = targetField.getType();
                    //基础类型
                    Object sourceValueObj = source.get(sourceKey);
                    if (sourceValueObj == null){
                        continue;
                    }
                    String sourceValueStr = sourceValueObj.toString();
                    if (sourceValueStr.length() == 0){
                        continue;
                    }
                    if (type == Boolean.class){
                        targetField.set(target, Boolean.parseBoolean(sourceValueStr));
                    }
                    else if (type == Character.class){
                        targetField.set(target, sourceValueStr.charAt(0));
                    }
                    else if (type == Byte.class){
                        targetField.set(target, Byte.parseByte(sourceValueStr));
                    }
                    else if (type == Short.class){
                        targetField.set(target, Short.parseShort(sourceValueStr));
                    }
                    else if (type == Integer.class){
                        targetField.set(target, Integer.parseInt(sourceValueStr));
                    }
                    else if (type == Long.class){
                        targetField.set(target, Long.parseLong(sourceValueStr));
                    }
                    else if (type == Float.class){
                        targetField.set(target, Float.parseFloat(sourceValueStr));
                    }
                    else if (type == Double.class){
                        targetField.set(target, Double.parseDouble(sourceValueStr));
                    }
                    else if (type == Void.class){
                        targetField.set(target, null);
                    }
                    //引用类型
                    else {
                        targetField.set(target, type.cast(sourceValueObj));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            targetResults.add(target);
        }
        return targetResults;
    }

    /**
     * 将model对象转化为外部表的map
     */
    public static <S> Map<String, Object> copyModelToMap(S model) {
        Map<String, String> propertiesToMap = new HashMap<>();
        propertiesToMap.put("id", "id_");
        propertiesToMap.put("refId", "ref_id_");
        return copyPropertiesToMap(model, Arrays.asList("formDataRev", "serialVersionUID"), propertiesToMap, false, name -> String.format("f_%s", name).toUpperCase());
    }

    /**
     * 将外部表的map转化为model
     */
    public static <T> void copyMapToModel(Map<String, Object> map, T model) {
        Map<String, String> mapToProperties = new HashMap<>();
        mapToProperties.put("id_", "id");
        mapToProperties.put("ref_id_", "refId");
        copyMapToProperties(map, model, null, mapToProperties, false, name -> name.substring(2));
    }

    /**
     * 将多个外部表的map转化为model
     */
    public static <T> List<T> copyMapToModelByList(List<Map<String, Object>> maps, Class<T> modelTargetClass) {
        Map<String, String> mapToProperties = new HashMap<>();
        mapToProperties.put("id_", "id");
        mapToProperties.put("ref_id_", "refId");
        return copyMapToPropertiesByList(maps, modelTargetClass, null, mapToProperties, false, name -> name.substring(2).toLowerCase());
    }

    /**
     * 将多个model对象数组转化为多个外部表的map数组
     */
    public static <S> List<Map<String, Object>> copyModelToMapByList(List<S> models) {
        Map<String, String> propertiesToMap = new HashMap<>();
        propertiesToMap.put("id", "id_");
        propertiesToMap.put("refId", "ref_id_");
        return copyPropertiesToMapByList(models, Arrays.asList("formDataRev", "serialVersionUID"), propertiesToMap, false, name -> String.format("f_%s", name).toUpperCase());
    }
}
