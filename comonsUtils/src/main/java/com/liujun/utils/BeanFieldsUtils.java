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

    interface FieldNameCompareHandler {
        /**
         * @param sourceFieldName 源属性名称或key
         * @param targetFieldName 目标属性名称或key
         * @return 是否是需要赋值的属性或key
         */
        boolean handler(String sourceFieldName, String targetFieldName);
    }

    interface FieldNameConvertHandler {
        /**
         * 属性名映射处理器
         * lambda表达式: 源对象属性名或key -> 目标对象属性名或key
         *
         * @param sourceFieldName 源对象属性名或key
         * @return 映射后的对象属性名或key
         */
        String handler(String sourceFieldName);
    }

    /**
     * 对象的属性值复制到另一个对象
     *
     * @param source        源对象
     * @param target        目标对象
     * @param fieldFieldMap (源属性名->目标属性名)映射map
     * @param <S>           源对象类型
     * @param <T>           目标对象类型
     * @return 目标对象
     */
    private static <S, T> T copyPropertyToProperty(S source, T target, Map<Field, Field> fieldFieldMap) {
        if (source == null || target == null || fieldFieldMap == null || fieldFieldMap.isEmpty()) {
            return target;
        }
        for (Map.Entry<Field, Field> entry : fieldFieldMap.entrySet()) {
            Field sourceField = entry.getKey();
            Field targetField = entry.getValue();
            try {
                sourceField.setAccessible(true);
                targetField.setAccessible(true);
                targetField.set(target, sourceField.get(source));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return target;
    }

    /**
     * 对象的属性值复制到map
     *
     * @param source      源对象
     * @param target      目标map
     * @param fieldKeyMap (源属性名->目标属性名)映射map
     * @param <S>         源对象类型
     * @param <T>         目标map值类型
     * @return 目标map
     */
    private static <S, T> Map<String, T> copyPropertyToKey(S source, Map<String, T> target, Map<Field, String> fieldKeyMap) {
        if (source == null || target == null || fieldKeyMap == null || fieldKeyMap.isEmpty()) {
            return target;
        }
        for (Map.Entry<Field, String> entry : fieldKeyMap.entrySet()) {
            Field sourceField = entry.getKey();
            String targetKey = entry.getValue();
            try {
                sourceField.setAccessible(true);
                target.put(targetKey, (T) sourceField.get(source));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return target;
    }

    /**
     * map的属性值复制到对象的属性值
     *
     * @param source      源map
     * @param target      目标对象
     * @param keyFieldMap (源属性名->目标属性名)映射map
     * @param <S>         源map属性值类型
     * @param <T>         目标对象类型
     * @return 目标对象
     */
    private static <S, T> T copyKeyToProperty(Map<String, S> source, T target, Map<String, Field> keyFieldMap) {
        if (source == null || target == null || keyFieldMap == null || keyFieldMap.isEmpty()) {
            return target;
        }
        for (Map.Entry<String, Field> entry : keyFieldMap.entrySet()) {
            String sourceKey = entry.getKey();
            Field targetField = entry.getValue();
            try {
                targetField.setAccessible(true);
                targetField.set(target, source.get(sourceKey));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return target;
    }

    /**
     * map的属性值复制到map的属性值
     *
     * @param source    源map
     * @param keyKeyMap (源属性名->目标属性名)映射map
     * @param <S>       源map属性值类型
     * @param <T>       目标map属性值类型
     * @return 目标map
     */
    private static <S, T> Map<String, T> copyKeyToKey(Map<String, S> source, Map<String, T> target, Map<String, String> keyKeyMap) {
        if (source == null || target == null || keyKeyMap == null || keyKeyMap.isEmpty()) {
            return target;
        }
        for (Map.Entry<String, String> entry : keyKeyMap.entrySet()) {
            String sourceKey = entry.getKey();
            String targetKey = entry.getValue();
            target.put(targetKey, (T) source.get(sourceKey));
        }
        return target;
    }

    private static List<String> filedNameFilter(List<String> allFields, List<String> ignoreFields, Collection<String> fieldsInMap, boolean ignoreOutOfMap) {
        Stream<String> fieldsStream = allFields.stream();
        if (ignoreFields != null && !ignoreFields.isEmpty()) {
            fieldsStream = fieldsStream.filter(fieldName -> !ignoreFields.contains(fieldName));
        }
        if (ignoreOutOfMap && fieldsInMap != null && !fieldsInMap.isEmpty()) {
            fieldsStream = fieldsStream.filter(fieldsInMap::contains);
        }
        return fieldsStream.collect(Collectors.toList());
    }

    private static void sourceToTargetFieldsMapConvert(Map<String, String> fieldNameMap, List<String> sourceFieldsFilter, List<String> targetFieldsFilter, Map<String, String> sourceToTargetFieldsMap) {
        //将映射map中的映射关系存入map
        if (sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty()) {
            for (String sourceFieldName : sourceFieldsFilter) {
                if (sourceToTargetFieldsMap.containsKey(sourceFieldName)) {
                    String targetFieldName = sourceToTargetFieldsMap.get(sourceFieldName);
                    //不允许多个源属性同时赋值到一个目标属性
                    if (!fieldNameMap.containsValue(targetFieldName) && targetFieldsFilter.contains(targetFieldName)) {
                        fieldNameMap.put(sourceFieldName, targetFieldName);
                    }
                }
            }
        }
    }

    private static void outOfMapConvert(Map<String, String> fieldNameMap, List<String> sourceFieldsFilter, List<String> targetFieldsFilter, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        if (!ignoreOutOfMap) {
            //通过属性映射处理器将剩余符合条件的属性映射关系存入map
            if (fieldNameCompareHandler != null) {
                for (String sourceFieldName : sourceFieldsFilter) {
                    //不允许多个源属性同时赋值到一个目标属性
                    List<String> targetFieldsNames = targetFieldsFilter.stream().filter(targetFieldName -> !fieldNameMap.containsValue(targetFieldName)).collect(Collectors.toList());
                    for (String targetFieldName : targetFieldsNames) {
                        if (fieldNameCompareHandler.handler(sourceFieldName, targetFieldName)) {
                            fieldNameMap.put(sourceFieldName, targetFieldName);
                        }
                    }
                }
            }
            //将剩余的属性属性名一致的映射关系存入map
            if (fieldNameMap.size() < sourceFieldsFilter.size() && fieldNameMap.size() < targetFieldsFilter.size()) {
                for (String sourceFieldName : sourceFieldsFilter) {
                    for (String targetFieldName : targetFieldsFilter) {
                        //不允许多个源属性同时赋值到一个目标属性
                        if (sourceFieldName.equals(targetFieldName) && !fieldNameMap.containsValue(targetFieldName)) {
                            fieldNameMap.put(sourceFieldName, targetFieldName);
                        }
                    }
                }
            }
        }
    }

    private static void outOfMapConvert(Map<String, String> fieldNameMap, List<String> sourceFieldsFilter, List<String> targetFieldsFilter, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        if (!ignoreOutOfMap) {
            //通过属性映射处理器将剩余符合条件的属性映射关系存入map
            if (fieldNameConvertHandler != null) {
                for (String sourceFieldName : sourceFieldsFilter) {
                    //不允许多个源属性同时赋值到一个目标属性
                    List<String> targetFieldsNames = targetFieldsFilter.stream().filter(targetFieldName -> !fieldNameMap.containsValue(targetFieldName)).collect(Collectors.toList());
                    String handlerTargetFieldName = fieldNameConvertHandler.handler(sourceFieldName);
                    if (targetFieldsNames.contains(handlerTargetFieldName)) {
                        fieldNameMap.put(sourceFieldName, handlerTargetFieldName);
                    }
                }
            }
            //将剩余的属性属性名一致的映射关系存入map
            if (fieldNameMap.size() < sourceFieldsFilter.size() && fieldNameMap.size() < targetFieldsFilter.size()) {
                for (String sourceFieldName : sourceFieldsFilter) {
                    for (String targetFieldName : targetFieldsFilter) {
                        //不允许多个源属性同时赋值到一个目标属性
                        if (sourceFieldName.equals(targetFieldName) && !fieldNameMap.containsValue(targetFieldName)) {
                            fieldNameMap.put(sourceFieldName, targetFieldName);
                        }
                    }
                }
            }
        }
    }

    private static Map<String, String> filedNameMapConvert(List<String> sourceFields, List<String> targetFields, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        //过滤不需要的属性
        Collection<String> sourceFieldsInMap = sourceToTargetFieldsMap == null ? null : sourceToTargetFieldsMap.keySet();
        Collection<String> targetFieldsInMap = sourceToTargetFieldsMap == null ? null : sourceToTargetFieldsMap.values();
        List<String> sourceFieldsFilter = filedNameFilter(sourceFields, ignoreSourceFields, sourceFieldsInMap, ignoreOutOfMap);
        List<String> targetFieldsFilter = filedNameFilter(targetFields, ignoreTargetFields, targetFieldsInMap, ignoreOutOfMap);
        //开始处理属性映射
        Map<String, String> fieldNameMap = new HashMap<>();
        sourceToTargetFieldsMapConvert(fieldNameMap, sourceFieldsFilter, targetFieldsFilter, sourceToTargetFieldsMap);
        outOfMapConvert(fieldNameMap, sourceFieldsFilter, targetFieldsFilter, ignoreOutOfMap, fieldNameCompareHandler);
        return fieldNameMap;
    }


    public static <S, T> void copyPropertyToProperty(S source, T target, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        if (source == null || target == null) {
            return;
        }
        Field[] sourceFields = source.getClass().getDeclaredFields();
        List<String> sourceFieldNames = new LinkedList<>();
        Map<String, Field> sourceFieldNameMap = new HashMap<>();
        for (Field sourceField : sourceFields) {
            sourceFieldNames.add(sourceField.getName());
            sourceFieldNameMap.put(sourceField.getName(), sourceField);
        }
        Field[] targetFields = target.getClass().getDeclaredFields();
        List<String> targetFieldNames = new LinkedList<>();
        Map<String, Field> targetFieldNameMap = new HashMap<>();
        for (Field targetField : targetFields) {
            targetFieldNames.add(targetField.getName());
            targetFieldNameMap.put(targetField.getName(), targetField);
        }
        Map<String, String> fieldNameMap = filedNameMapConvert(sourceFieldNames, targetFieldNames, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameCompareHandler);
        Map<Field, Field> fieldMap = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldNameMap.entrySet()) {
            String sourceFieldName = entry.getKey();
            String targetFieldName = entry.getValue();
            fieldMap.put(sourceFieldNameMap.get(sourceFieldName), targetFieldNameMap.get(targetFieldName));
        }
        copyPropertyToProperty(source, target, fieldMap);
    }


}
