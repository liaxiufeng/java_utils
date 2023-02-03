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

    private static List<String> getFieldNames(Collection<Field> fields){
        return fields.stream().map(Field::getName).collect(Collectors.toList());
    }

    /**
     * 属性映射处理器
     * @param sourceFields 源对象属性名
     * @param targetFields 目标对象属性名
     * @param ignoreSourceFields 忽略源对象的属性
     * @param ignoreTargetFields 忽略目标对象的属性
     * @param sourceToTargetFieldsMap 源对象属性名或map的key -> 映射后的对象属性名或map的key
     * @param ignoreOutOfMap 是否忽略map中没有的key
     * @param fieldNameMapperHandler 属性映射处理器
     * @return (源属性名->目标属性名)映射map
     */
    private static Map<String,String> fieldConvert(List<String> sourceFields, List<String> targetFields, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap,FieldNameMapperHandler fieldNameMapperHandler){
        Stream<String> sourceFieldsStream = sourceFields.stream();
        Stream<String> targetFieldsStream = targetFields.stream();
        if (ignoreSourceFields != null && !ignoreSourceFields.isEmpty()) {
            sourceFieldsStream = sourceFieldsStream.filter(fieldName -> !ignoreSourceFields.contains(fieldName));
        }
        if (ignoreTargetFields != null && !ignoreTargetFields.isEmpty()) {
            targetFieldsStream = targetFieldsStream.filter(fieldName -> !ignoreTargetFields.contains(fieldName));
        }
        if (ignoreOutOfMap && sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty()) {
            sourceFieldsStream = sourceFieldsStream.filter(sourceToTargetFieldsMap::containsKey);
            targetFieldsStream = targetFieldsStream.filter(sourceToTargetFieldsMap::containsValue);
        }
        List<String> sourceFieldsFilter = sourceFieldsStream.collect(Collectors.toList());
        List<String> targetFieldsFilter = targetFieldsStream.collect(Collectors.toList());

        //将映射map中的映射关系存入map
        Map<String, String> fieldNameMap = new HashMap<>();
        if (sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty()){
            for (String sourceFieldName : sourceFieldsFilter) {
                if (sourceToTargetFieldsMap.containsKey(sourceFieldName)){
                    for (String targetFieldName:targetFieldsFilter){
                        if (sourceToTargetFieldsMap.containsValue(targetFieldName)){
                            fieldNameMap.put(sourceFieldName,targetFieldName);
                        }
                    }
                }
            }
        }


        if (!ignoreOutOfMap){
            //通过属性映射处理器将剩余符合条件的属性映射关系存入map
            if ( fieldNameMapperHandler != null){
                for (String sourceFieldName : sourceFieldsFilter) {
//                    if (fieldNameMap.containsKey(sourceFieldName)){
//                        continue;
//                    }
                    String mapperTargetName = fieldNameMapperHandler.handler(sourceFieldName);
                    //不允许多个源属性同时赋值到一个目标属性
                    if (fieldNameMap.containsValue(mapperTargetName)){
                        continue;
                    }
                    for (String targetFieldName:targetFieldsFilter){
                        if (mapperTargetName.equals(targetFieldName)){
                            fieldNameMap.put(sourceFieldName,targetFieldName);
                        }
                    }
                }
            }

            //将剩余的属性属性名一致的映射关系存入map
            for (String sourceFieldName : sourceFieldsFilter) {
                for (String targetFieldName:targetFieldsFilter){
                    //不允许多个源属性同时赋值到一个目标属性
                    if (sourceFieldName.equals(targetFieldName) && !fieldNameMap.containsValue(targetFieldName)){
                        fieldNameMap.put(sourceFieldName,targetFieldName);
                    }
                }
            }
        }
        return fieldNameMap;
    }

    private static Map<String,String> fieldConvert(List<String> sourcesFieldNames, List<String> ignoreSourceFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameMapperHandler commonHandler){
        Stream<String> sourceFieldsStream = sourcesFieldNames.stream();
        if (ignoreSourceFields != null && !ignoreSourceFields.isEmpty()) {
            sourceFieldsStream = sourceFieldsStream.filter(fieldName -> !ignoreSourceFields.contains(fieldName));
        }
        if (ignoreOutOfMap && sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty()) {
            sourceFieldsStream = sourceFieldsStream.filter(sourceToTargetFieldsMap::containsKey);
        }
        List<String> sourceFields = sourceFieldsStream.collect(Collectors.toList());
        //将映射map中的映射关系存入map
        Map<String, String> fieldsToMap = new HashMap<>();
        for (String sourceFieldName : sourceFields) {
            if (sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty() && sourceToTargetFieldsMap.containsKey(sourceFieldName)) {
                String targetFieldName = sourceToTargetFieldsMap.get(sourceFieldName);
                if (!fieldsToMap.containsValue(targetFieldName)){
                    fieldsToMap.put(sourceFieldName, targetFieldName);
                }
            }
        }
        if (!ignoreOutOfMap){
            //通过属性映射处理器将剩余符合条件的属性映射关系存入map
            if (commonHandler != null) {
                for (String sourceFieldName : sourceFields) {
                    String targetFieldName = commonHandler.handler(sourceFieldName);
                    if (!fieldsToMap.containsValue(targetFieldName)){
                        fieldsToMap.put(sourceFieldName, targetFieldName);
                    }
                }
            }

            //将剩余的属性按属性名一致原则存入map
            for (String sourceFieldName : sourceFields) {
                if (!fieldsToMap.containsKey(sourceFieldName)){
                    fieldsToMap.put(sourceFieldName, sourceFieldName);
                }
            }
        }
        return fieldsToMap;
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
    public static <S, T> void copyProperties(S source, T target, List<String> ignoreSourceField, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameMapperHandler fieldNameMapperHandler) {
        if (source == null || target == null) {
            return;
        }
        List<Field> sourceFields = Arrays.asList(source.getClass().getDeclaredFields());
        List<Field> targetFields = Arrays.asList(target.getClass().getDeclaredFields());
        Map<String, String> fieldNameMap = fieldConvert(getFieldNames(sourceFields), getFieldNames(targetFields), null, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameMapperHandler);
        copyProperties(source, target, fieldNameMap);
    }

    /**
     * 复制对象的属性值
     * @param source 源对象
     * @param target 目标对象
     * @param fieldNameMap (源属性名->目标属性名)映射map
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     */
    private static <S, T> void copyProperties(S source, T target, Map<String, String> fieldNameMap) {
        if (source == null || target == null || fieldNameMap == null || fieldNameMap.isEmpty()) {
            return;
        }
        Field[] sourceFields = source.getClass().getDeclaredFields();
        Field[] targetFields = target.getClass().getDeclaredFields();
        for (Field targetField : targetFields) {
            String targetFieldName = targetField.getName();
            for (Field sourceField : sourceFields) {
                String sourceFieldName = sourceField.getName();
                if (fieldNameMap.containsKey(sourceFieldName) && targetFieldName.equals(fieldNameMap.get(sourceFieldName))) {
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
    public static <S, T> List<T> copyPropertiesByList(List<S> sources, Class<T> targetClass, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameMapperHandler fieldNameMapperHandler) throws InstantiationException, IllegalAccessException {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        Class<?> sourceClass = sources.get(0).getClass();
        List<String> sourceFieldNames = getFieldNames(Arrays.asList(sourceClass.getDeclaredFields()));
        List<String> targetFieldNames = getFieldNames(Arrays.asList(targetClass.getDeclaredFields()));
        Map<String, String> fieldNameMap = fieldConvert(sourceFieldNames, targetFieldNames, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameMapperHandler);

        List<T> targetList = new LinkedList<>();
        for (S s : sources) {
            T t = targetClass.newInstance();
            for (String sourceFiledName:fieldNameMap.keySet()){
                try {
                    Field sourceField = sourceClass.getField(sourceFiledName);
                    Field targetField = targetClass.getField(fieldNameMap.get(sourceFiledName));
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    targetField.set(t,sourceField.get(s));
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
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
        copyProperties(source, target, null, null, null, false, null);
    }

    /**
     * 复制多个对象相同属性名的值
     *
     * @param sources     源对象列表
     * @param targetClass 目标对象的类
     */
    public static <S, T> List<T> copyPropertiesByList(List<S> sources, Class<T> targetClass) throws IllegalAccessException, InstantiationException {
        return copyPropertiesByList(sources, targetClass, null, null, null, false, null);
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
        Object sourceFirst = sources.get(0);
        if (sourceFirst == null) {
            return null;
        }
        Class<?> sourceClass = sourceFirst.getClass();
        Map<String, String> fieldMapkeyMap = fieldConvert(getFieldNames(Arrays.asList(sourceClass.getDeclaredFields())), ignoreSourceFields, propertiesToMap, ignoreOutOfMap, commonHandler);

        List<Map<String, Object>> maps = new LinkedList<>();
        try {
            //将属性名转化为属性对象
            List<Field> sourceFields = new LinkedList<>();
            for (String s : fieldMapkeyMap.keySet()) {
                Field sourceClassField = sourceClass.getField(s);
                sourceFields.add(sourceClassField);
            }
            //遍历源对象列表，赋值到map列表中
            for (S sourceObj : sources) {
                Map<String, Object> map = new HashMap<>();
                for (Field field : sourceFields) {
                    field.setAccessible(true);
                    map.put(fieldMapkeyMap.get(field.getName()), field.get(sourceObj));
                }
                maps.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        Class<?> sourceClass = source.getClass();
        List<String> sourceFieldNames = getFieldNames(Arrays.asList(sourceClass.getDeclaredFields()));
        Map<String, String> fieldMapkeyMap = fieldConvert(sourceFieldNames, null, propertiesToMap, ignoreOutOfMap, commonHandler);
        Map<String, Object> map = new HashMap<>();
        for (String sourceFieldName : fieldMapkeyMap.keySet()) {
            try {
                Field sourceClassField = sourceClass.getField(sourceFieldName);
                sourceClassField.setAccessible(true);
                map.put(sourceFieldName, sourceClassField.get(source));
            } catch (Exception e) {
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
        LinkedList<String> sourceKeys = new LinkedList<>(source.keySet());
        Class<?> targetClass = target.getClass();
        List<String> targetFieldNames = Arrays.stream(targetClass.getFields()).map(Field::getName).collect(Collectors.toList());
        Map<String, String> mapkeyFieldMap = fieldConvert(sourceKeys, targetFieldNames, null, null, mapToProperties, ignoreOutOfMap, commonHandler);
        for (String sourceKey:mapkeyFieldMap.keySet()){
            try {
                String targetFieldName = mapkeyFieldMap.get(sourceKey);
                Field targetClassField = targetClass.getField(targetFieldName);
                targetClassField.setAccessible(true);
                targetClassField.set(target,source.get(sourceKey));
            } catch (Exception e) {
                throw new RuntimeException(e);
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
