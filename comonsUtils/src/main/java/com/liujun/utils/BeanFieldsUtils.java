package com.liujun.utils;

import java.lang.reflect.*;
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

    public interface FieldNameCompareHandler {
        /**
         * @param sourceFieldName 源属性名称或key
         * @param targetFieldName 目标属性名称或key
         * @return 是否是需要赋值的属性或key
         */
        boolean handler(String sourceFieldName, String targetFieldName);
    }

    public interface FieldNameConvertHandler {
        /**
         * 属性名映射处理器
         * lambda表达式: 源对象属性名或key -> 目标对象属性名或key
         *
         * @param sourceFieldName 源对象属性名或key
         * @return 映射后的对象属性名或key
         */
        String handler(String sourceFieldName);
    }

    public static class GenericTypeNotFoundException extends RuntimeException {
        public GenericTypeNotFoundException(String message) {
            super(message);
        }
    }

    private static GenericTypeNotFoundException collectionGenericTypeNotFoundException() {
        return new GenericTypeNotFoundException("无法获取到集合的元素类型，请将没有元素的容器初始化。初始化提示: new Collection<>()更改为new Collection<T>(){}");
    }

    private static GenericTypeNotFoundException mapGenericTypeNotFoundException() {
        return new GenericTypeNotFoundException("无法获取到Map的值类型，请将没有元素的Map对初始化。初始化提示: new Map<>()更改为new Map<K,V>(){}");
    }

    /**
     * 类型转换<br/>
     * 支持可强转化类型之间转换，字符串转基本类型，各种类型转字符串<br/>
     * 当转化失败时，会抛出异常，或返回null<br/>
     *
     * @param source      源对象
     * @param targetClass 目标类型
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 目标对象
     * @todo 考虑支持常见时间格式的字符串转Date类型，并考虑支持自定义时间格式
     */
    public static <S, T> T parse(S source, Class<T> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }
        Class<?> sourceClass = source.getClass();
        //同类型，可强制转换的类型
        if (sourceClass == targetClass || targetClass.isInstance(source)) {
            return (T) source;
        }
        //将非包装类的基本类型转化为包装类
        String targetClassName = targetClass.getName();
        int lastSplitIndex = targetClassName.lastIndexOf(".");
        String targetClassSimpleName = targetClassName.substring(lastSplitIndex + 1);
        switch (targetClassSimpleName) {
            case "byte":
                targetClass = (Class<T>) Byte.class;
                targetClassSimpleName = "Byte";
                break;
            case "int":
                targetClass = (Class<T>) Integer.class;
                targetClassSimpleName = "Integer";
                break;
            case "short":
                targetClass = (Class<T>) Short.class;
                targetClassSimpleName = "Short";
                break;
            case "long":
                targetClass = (Class<T>) Long.class;
                targetClassSimpleName = "Long";
                break;
            case "float":
                targetClass = (Class<T>) Float.class;
                targetClassSimpleName = "Float";
                break;
            case "double":
                targetClass = (Class<T>) Double.class;
                targetClassSimpleName = "Double";
                break;
            case "char":
                targetClass = (Class<T>) Character.class;
                targetClassSimpleName = "Character";
                break;
            case "boolean":
                targetClass = (Class<T>) Boolean.class;
                targetClassSimpleName = "Boolean";
                break;
            default:
                break;
        }
        //? -> ? (可强制转换时)
        if (targetClass.isInstance(source)) {
            return (T) source;
        }
        //? -> string
        if (targetClass.isAssignableFrom(String.class)) {
            if (Date.class.isAssignableFrom(targetClass)) {
                //todo
                //日期格式转换
            }
            return (T) String.valueOf(source);
        }
        //string -> ?
        if (sourceClass.isAssignableFrom(String.class)) {
            String sourceStr = (String) source;
            switch (targetClassSimpleName) {
                case "Byte":
                    return (T) Byte.valueOf(sourceStr);
                case "Integer":
                    return (T) Integer.valueOf(sourceStr);
                case "Short":
                    return (T) Short.valueOf(sourceStr);
                case "Long":
                    return (T) Long.valueOf(sourceStr);
                case "Float":
                    return (T) Float.valueOf(sourceStr);
                case "Double":
                    return (T) Double.valueOf(sourceStr);
                case "Character":
                    return (T) Character.valueOf((sourceStr).charAt(0));
                case "Boolean":
                    String lowerCase = sourceStr.toLowerCase();
                    if ("true".equals(lowerCase) || "false".equals(lowerCase)) {
                        return (T) Boolean.valueOf(lowerCase);
                    }
                    if ("^-?\\d+([.]\\d+)?&".matches(lowerCase)) {
                        return (T) Boolean.valueOf(Double.valueOf(lowerCase) > 0);
                    }
                    return null;
                default:
                    break;
            }
            if (Date.class.isAssignableFrom(targetClass)) {
                //todo
                //日期格式转换
            }
        }
        return null;
    }

    /**
     * 对象的属性值复制到另一个对象
     *
     * @param source        源对象
     * @param target        目标对象
     * @param fieldFieldMap (目标属性名->源属性名)映射map
     * @param <S>           源对象类型
     * @param <T>           目标对象类型
     */
    private static <S, T> void copyPropertyToProperty(S source, T target, Map<Field, Field> fieldFieldMap) {
        if (source == null || target == null || fieldFieldMap == null || fieldFieldMap.isEmpty()) {
            return;
        }
        for (Map.Entry<Field, Field> entry : fieldFieldMap.entrySet()) {
            Field targetField = entry.getKey();
            Field sourceField = entry.getValue();
            try {
                sourceField.setAccessible(true);
                targetField.setAccessible(true);
                Object value = parse(sourceField.get(source), targetField.getType());
                if (value != null) {
                    targetField.set(target, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 对象的属性值复制到map
     *
     * @param source      源对象
     * @param target      目标map
     * @param fieldKeyMap (目标属性名->源属性名)映射map
     * @param <S>         源对象类型
     * @param <T>         目标map值类型
     */
    private static <S, T> void copyPropertyToKey(S source, Map<String, T> target, Map<String, Field> fieldKeyMap) {
        if (source == null || target == null || fieldKeyMap == null || fieldKeyMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Field> entry : fieldKeyMap.entrySet()) {
            String targetKey = entry.getKey();
            Field sourceField = entry.getValue();
            try {
                sourceField.setAccessible(true);
                Class<?> targetValueClass = getGenericType(target, 1);
                Object value = parse(sourceField.get(source), targetValueClass);
                if (value != null) {
                    target.put(targetKey, (T) value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * map的属性值复制到对象的属性值
     *
     * @param source      源map
     * @param target      目标对象
     * @param keyFieldMap (目标属性名->源属性名)映射map
     * @param <S>         源map属性值类型
     * @param <T>         目标对象类型
     */
    private static <S, T> void copyKeyToProperty(Map<String, S> source, T target, Map<Field, String> keyFieldMap) {
        if (source == null || target == null || keyFieldMap == null || keyFieldMap.isEmpty()) {
            return;
        }
        for (Map.Entry<Field, String> entry : keyFieldMap.entrySet()) {
            Field targetField = entry.getKey();
            String sourceKey = entry.getValue();
            try {
                targetField.setAccessible(true);
                Object value = parse(source.get(sourceKey), targetField.getType());
                if (value != null) {
                    targetField.set(target, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * map的属性值复制到map的属性值
     *
     * @param source    源map
     * @param keyKeyMap (目标属性名->源属性名)映射map
     * @param <S>       源map属性值类型
     * @param <T>       目标map属性值类型
     */
    private static <S, T> void copyKeyToKey(Map<String, S> source, Map<String, T> target, Map<String, String> keyKeyMap) {
        if (source == null || target == null || keyKeyMap == null || keyKeyMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : keyKeyMap.entrySet()) {
            String targetKey = entry.getKey();
            String sourceKey = entry.getValue();
            Class<?> targetValueClass;
            try {
                targetValueClass = getGenericType(target, 1);
            } catch (ClassCastException e){
                throw mapGenericTypeNotFoundException();
            }
            Object value = parse(source.get(sourceKey), targetValueClass);
            if (value != null) {
                target.put(targetKey, (T) value);
            }
        }
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

    private static void addFieldNameMap(Map<String, String> fieldNameMap, String sourceFieldName, String targetFieldName, boolean override) {
        if (!fieldNameMap.containsKey(targetFieldName) || override) {
            fieldNameMap.put(targetFieldName, sourceFieldName);
        }
    }

    private static void sourceToTargetFieldsMapConvert(Map<String, String> fieldNameMap, List<String> sourceFieldsFilter, List<String> targetFieldsFilter, Map<String, String> sourceToTargetFieldsMap) {
        //将映射map中的映射关系存入map
        if (sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty()) {
            for (String sourceFieldName : sourceFieldsFilter) {
                if (sourceToTargetFieldsMap.containsKey(sourceFieldName)) {
                    String targetFieldName = sourceToTargetFieldsMap.get(sourceFieldName);
                    if (targetFieldsFilter.contains(targetFieldName)) {
                        addFieldNameMap(fieldNameMap, sourceFieldName, targetFieldName, true);
                    }
                }
            }
        }
    }

    private static void sourceToTargetFieldsMapConvert(Map<String, String> fieldNameMap, List<String> sourceFieldsFilter, Map<String, String> sourceToTargetFieldsMap) {
        //将映射map中的映射关系存入map
        if (sourceToTargetFieldsMap != null && !sourceToTargetFieldsMap.isEmpty()) {
            for (String sourceFieldName : sourceFieldsFilter) {
                if (sourceToTargetFieldsMap.containsKey(sourceFieldName)) {
                    String targetFieldName = sourceToTargetFieldsMap.get(sourceFieldName);
                    addFieldNameMap(fieldNameMap, sourceFieldName, targetFieldName, true);
                }
            }
        }
    }

    private static void outOfMapConvert(Map<String, String> fieldNameMap, List<String> sourceFieldsFilter, List<String> targetFieldsFilter, boolean ignoreSame, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        if (!ignoreOutOfMap) {
            //通过属性映射处理器将剩余符合条件的属性映射关系存入map
            if (fieldNameCompareHandler != null) {
                for (String sourceFieldName : sourceFieldsFilter) {
                    for (String targetFieldName : targetFieldsFilter) {
                        if (fieldNameCompareHandler.handler(sourceFieldName, targetFieldName)) {
                            addFieldNameMap(fieldNameMap, sourceFieldName, targetFieldName, true);
                        }
                    }
                }
            }
            //将剩余的属性名一致的映射关系存入map
            if (!ignoreSame && fieldNameMap.size() < sourceFieldsFilter.size() && fieldNameMap.size() < targetFieldsFilter.size()) {
                for (String sourceFieldName : sourceFieldsFilter) {
                    if (targetFieldsFilter.contains(sourceFieldName)) {
                        addFieldNameMap(fieldNameMap, sourceFieldName, sourceFieldName, false);
                    }
                }
            }
        }
    }

    private static void outOfMapConvert(Map<String, String> fieldNameMap, List<String> sourceFieldsFilter, boolean ignoreSame, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        if (!ignoreOutOfMap) {
            //通过属性映射处理器将剩余符合条件的属性映射关系存入map
            if (fieldNameConvertHandler != null) {
                for (String sourceFieldName : sourceFieldsFilter) {
                    String handlerTargetFieldName = fieldNameConvertHandler.handler(sourceFieldName);
                    addFieldNameMap(fieldNameMap, sourceFieldName, handlerTargetFieldName, true);
                }
            }
            //将剩余的属性名一致的映射关系存入map
            if (!ignoreSame && fieldNameMap.size() < sourceFieldsFilter.size()) {
                for (String sourceFieldName : sourceFieldsFilter) {
                    addFieldNameMap(fieldNameMap, sourceFieldName, sourceFieldName, false);
                }
            }
        }
    }

    private static Map<String, String> filedNameMapConvert(List<String> sourceFields, List<String> targetFields, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        //过滤不需要的属性
        Collection<String> sourceFieldsInMap = sourceToTargetFieldsMap == null ? null : sourceToTargetFieldsMap.keySet();
        Collection<String> targetFieldsInMap = sourceToTargetFieldsMap == null ? null : sourceToTargetFieldsMap.values();
        List<String> sourceFieldsFilter = filedNameFilter(sourceFields, ignoreSourceFields, sourceFieldsInMap, ignoreOutOfMap);
        List<String> targetFieldsFilter = filedNameFilter(targetFields, ignoreTargetFields, targetFieldsInMap, ignoreOutOfMap);
        //开始处理属性映射
        Map<String, String> fieldNameMap = new HashMap<>();
        sourceToTargetFieldsMapConvert(fieldNameMap, sourceFieldsFilter, targetFieldsFilter, sourceToTargetFieldsMap);
        outOfMapConvert(fieldNameMap, sourceFieldsFilter, targetFieldsFilter, ignoreSame, ignoreOutOfMap, fieldNameCompareHandler);
        return fieldNameMap;
    }

    private static Map<String, String> filedNameMapConvert(List<String> sourceFields, boolean ignoreSame, List<String> ignoreSourceFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        //过滤不需要的属性
        Collection<String> sourceFieldsInMap = sourceToTargetFieldsMap == null ? null : sourceToTargetFieldsMap.keySet();
        List<String> sourceFieldsFilter = filedNameFilter(sourceFields, ignoreSourceFields, sourceFieldsInMap, ignoreOutOfMap);
        //开始处理属性映射
        Map<String, String> fieldNameMap = new HashMap<>();
        sourceToTargetFieldsMapConvert(fieldNameMap, sourceFieldsFilter, sourceToTargetFieldsMap);
        outOfMapConvert(fieldNameMap, sourceFieldsFilter, ignoreSame, ignoreOutOfMap, fieldNameConvertHandler);
        return fieldNameMap;
    }

    /**
     * 获取泛型类型
     *
     * @param obj obj 对象
     * @return 泛型类型
     */
    private static <T> Class<?> getGenericType(T obj, int index) {
        if (obj == null) {
            return null;
        }
        Type genericType = obj.getClass().getGenericSuperclass();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] types = pt.getActualTypeArguments();
            return (Class<?>) types[index];
        }
        return null;
    }

    private static <S, T> Map<Field, Field> getFieldMap(Class<S> sourceClass, Class<T> targetClass, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        if (sourceClass == null || targetClass == null) {
            return null;
        }
        Field[] sourceFields = sourceClass.getDeclaredFields();
        sourceFields = Arrays.stream(sourceFields).filter(field -> !Modifier.isStatic(field.getModifiers())).toArray(Field[]::new);
        List<String> sourceFieldNames = new LinkedList<>();
        Map<String, Field> sourceFieldNameMap = new HashMap<>();
        for (Field sourceField : sourceFields) {
            sourceFieldNames.add(sourceField.getName());
            sourceFieldNameMap.put(sourceField.getName(), sourceField);
        }
        Field[] targetFields = targetClass.getDeclaredFields();
        targetFields = Arrays.stream(targetFields).filter(field -> !Modifier.isStatic(field.getModifiers())).toArray(Field[]::new);
        List<String> targetFieldNames = new LinkedList<>();
        Map<String, Field> targetFieldNameMap = new HashMap<>();
        for (Field targetField : targetFields) {
            targetFieldNames.add(targetField.getName());
            targetFieldNameMap.put(targetField.getName(), targetField);
        }
        Map<String, String> fieldNameMap = filedNameMapConvert(sourceFieldNames, targetFieldNames, ignoreSame, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameCompareHandler);
        Map<Field, Field> fieldMap = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldNameMap.entrySet()) {
            String targetFieldName = entry.getKey();
            String sourceFieldName = entry.getValue();
            Field targetField = targetFieldNameMap.get(targetFieldName);
            Field sourceField = sourceFieldNameMap.get(sourceFieldName);
            if (targetField == null && sourceField == null){
                fieldMap.put(targetField, sourceField);
            }
            fieldMap.put(targetField, sourceField);
        }
        return fieldMap;
    }

    private static Map<String, Field> getFieldMap(Class<?> sourceClass, boolean ignoreSame, List<String> ignoreSourceFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        if (sourceClass == null) {
            return null;
        }
        Field[] sourceFields = sourceClass.getDeclaredFields();
        sourceFields = Arrays.stream(sourceFields).filter(field -> !Modifier.isStatic(field.getModifiers())).toArray(Field[]::new);
        List<String> sourceFieldNames = new LinkedList<>();
        Map<String, Field> sourceFieldNameMap = new HashMap<>();
        for (Field sourceField : sourceFields) {
            sourceFieldNames.add(sourceField.getName());
            sourceFieldNameMap.put(sourceField.getName(), sourceField);
        }
        Map<String, String> fieldNameMap = filedNameMapConvert(sourceFieldNames, ignoreSame, ignoreSourceFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameConvertHandler);
        Map<String, Field> fieldMap = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldNameMap.entrySet()) {
            String targetFieldName = entry.getKey();
            String sourceFieldName = entry.getValue();
            fieldMap.put(targetFieldName, sourceFieldNameMap.get(sourceFieldName));
        }
        return fieldMap;
    }

    private static Map<Field, String> getFieldMap(Set<String> sourceFieldNames, Class<?> targetClass, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        if (targetClass == null) {
            return null;
        }
        Field[] targetFields = targetClass.getDeclaredFields();
        targetFields = Arrays.stream(targetFields).filter(field -> !Modifier.isStatic(field.getModifiers())).toArray(Field[]::new);
        List<String> targetFieldNames = new LinkedList<>();
        Map<String, Field> targetFieldNameMap = new HashMap<>();
        for (Field targetField : targetFields) {
            targetFieldNames.add(targetField.getName());
            targetFieldNameMap.put(targetField.getName(), targetField);
        }
        Map<String, String> fieldNameMap = filedNameMapConvert(new ArrayList<>(sourceFieldNames), targetFieldNames, ignoreSame, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameCompareHandler);
        Map<Field, String> fieldMap = new HashMap<>();
        for (Map.Entry<String, String> entry : fieldNameMap.entrySet()) {
            String targetFieldName = entry.getKey();
            String sourceFieldName = entry.getValue();
            fieldMap.put(targetFieldNameMap.get(targetFieldName), sourceFieldName);
        }
        return fieldMap;
    }

    private static Map<String, String> getFieldMap(Set<String> sourceFieldNames, Set<String> targetFieldNames, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        return filedNameMapConvert(new ArrayList<>(sourceFieldNames), new ArrayList<>(targetFieldNames), ignoreSame, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameCompareHandler);
    }

    private static Map<String, String> getFieldMap(Set<String> sourceFieldNames, boolean ignoreSame, List<String> ignoreSourceFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        return filedNameMapConvert(new ArrayList<>(sourceFieldNames), ignoreSame, ignoreSourceFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameConvertHandler);
    }

    public static <S, T> void copyPropertyToProperty(S source, T target, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        if (source == null || target == null) {
            return;
        }
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
        Map<Field, Field> fieldMap = getFieldMap(sourceClass, targetClass, ignoreSame, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameCompareHandler);
        copyPropertyToProperty(source, target, fieldMap);
    }

    public static <S, T> void copyPropertyToProperty(S source, T target) {
        copyPropertyToProperty(source, target, false, null, null, null, false, null);
    }

    public static <S, T> void copyListPropertyToProperty(List<S> source, List<T> target, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) throws InstantiationException, IllegalAccessException {
        if (source == null || target == null || source.isEmpty()) {
            return;
        }
        Class<?> sourceGenericType = getGenericType(source, 0);
        Class<?> targetGenericType;
        try {
            targetGenericType = getGenericType(target, 0);
        } catch (ClassCastException e) {
            throw collectionGenericTypeNotFoundException();
        }
        Map<Field, Field> fieldMap = getFieldMap(sourceGenericType, targetGenericType, ignoreSame, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameCompareHandler);
        for (S sourceItem : source) {
            T targetItem = (T) targetGenericType.newInstance();
            copyPropertyToProperty(sourceItem, targetItem, fieldMap);
            target.add(targetItem);
        }
    }

    public static <S, T> void copyListPropertyToProperty(List<S> source, List<T> target) throws InstantiationException, IllegalAccessException {
        copyListPropertyToProperty(source, target, false, null, null, null, false, null);
    }

    public static <S, T> void copyPropertyToKey(S source, Map<String, T> target, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        if (source == null || target == null) {
            return;
        }
        Class<?> sourceClass = source.getClass();
        Map<String, Field> fieldMap = getFieldMap(sourceClass, ignoreSame, ignoreSourceFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameConvertHandler);
        copyPropertyToKey(source, target, fieldMap);
    }

    public static <S, T> void copyPropertyToKey(S source, Map<String, T> target) {
        copyPropertyToKey(source, target, false, null, null, null, false, null);
    }

    public static <S, T> void copyListPropertyToKey(List<S> source, List<Map<String, T>> target, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        if (source == null || target == null || source.isEmpty()) {
            return;
        }
        Class<?> sourceClass = source.getClass();
        Map<String, Field> fieldMap = getFieldMap(sourceClass, ignoreSame, ignoreSourceFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameConvertHandler);
        for (S sourceItem : source) {
            Map<String, T> targetItem = new HashMap<>();
            copyPropertyToKey(sourceItem, targetItem, fieldMap);
            target.add(targetItem);
        }
    }

    public static <S, T> void copyListPropertyToKey(List<S> source, List<Map<String, T>> target) {
        copyListPropertyToKey(source, target, false, null, null, null, false, null);
    }

    public static <S, T> void copyKeyToProperty(Map<String, S> source, T target, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) {
        if (source == null || source.isEmpty() || target == null) {
            return;
        }
        Class<?> targetClass = target.getClass();
        Map<Field, String> fieldMap = getFieldMap(source.keySet(), targetClass, ignoreSame, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameCompareHandler);
        copyKeyToProperty(source, target, fieldMap);
    }

    public static <S, T> void copyKeyToProperty(Map<String, S> source, T target) {
        copyKeyToProperty(source, target, false, null, null, null, false, null);
    }

    public static <S, T> void copyListKeyToProperty(List<Map<String, S>> source, List<T> target, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameCompareHandler fieldNameCompareHandler) throws InstantiationException, IllegalAccessException {
        if (source == null || source.isEmpty() || target == null) {
            return;
        }
        Map<String, S> sourceItem = source.get(0);
        if (sourceItem == null || sourceItem.isEmpty()) {
            return;
        }
        Class<?> targetClass = target.getClass();
        Map<Field, String> fieldMap = getFieldMap(sourceItem.keySet(), targetClass, ignoreSame, ignoreSourceFields, ignoreTargetFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameCompareHandler);
        for (Map<String, S> sourceTemp : source) {
            T targetItem = (T) targetClass.newInstance();
            copyKeyToProperty(sourceTemp, targetItem, fieldMap);
            target.add(targetItem);
        }
    }

    public static <S, T> void copyListKeyToProperty(List<Map<String, S>> source, List<T> target) throws InstantiationException, IllegalAccessException {
        copyListKeyToProperty(source, target, false, null, null, null, false, null);
    }

    public static <S, T> void copyKeyToKey(Map<String, S> source, Map<String, T> target, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        if (source == null || source.isEmpty() || target == null) {
            return;
        }
        Map<String, String> fieldMap = getFieldMap(source.keySet(), ignoreSame, ignoreSourceFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameConvertHandler);
        copyKeyToKey(source, target, fieldMap);
    }

    public static <S, T> void copyKeyToKey(Map<String, S> source, Map<String, T> target) {
        copyKeyToKey(source, target, false, null, null, null, false, null);
    }

    public static <S, T> void copyListKeyToKey(List<Map<String, S>> source, List<Map<String, T>> target, boolean ignoreSame, List<String> ignoreSourceFields, List<String> ignoreTargetFields, Map<String, String> sourceToTargetFieldsMap, boolean ignoreOutOfMap, FieldNameConvertHandler fieldNameConvertHandler) {
        if (source == null || source.isEmpty() || target == null) {
            return;
        }
        Map<String, S> sourceItem = source.get(0);
        if (sourceItem == null || sourceItem.isEmpty()) {
            return;
        }
        Map<String, String> fieldMap = getFieldMap(sourceItem.keySet(), ignoreSame, ignoreSourceFields, sourceToTargetFieldsMap, ignoreOutOfMap, fieldNameConvertHandler);
        for (Map<String, S> sourceTemp : source) {
            Map<String, T> targetItem = new HashMap<String, T>(){};
            copyKeyToKey(sourceTemp, targetItem, fieldMap);
            target.add(targetItem);
        }
    }

    public static <S, T> void copyListKeyToKey(List<Map<String, S>> source, List<Map<String, T>> target) {
        copyListKeyToKey(source, target, false, null, null, null, false, null);
    }

}
