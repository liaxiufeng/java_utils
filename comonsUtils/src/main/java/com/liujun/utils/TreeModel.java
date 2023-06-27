package com.liujun.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 树结构化通用父类<实体类,主键类>
 *
 * @author liujun
 * @date 2023/6/21
 */
public abstract class TreeModel<T,P> {

    /**
     * 判断是否为子元素
     * @param parentPrimaryKey 父主键
     * @param entity 元素
     */
    public abstract boolean isChild(P parentPrimaryKey, T entity);

    /**
     * 获取所有元素
     */
    public abstract List<T> getAllEntity();

    /**
     * 获取根元素
     */
    public abstract T getRootEntity();

    /**
     * 删除主键为pk的数据
     * @param primaryKeys 主键
     */
    public abstract void removeEntities(P[] primaryKeys);

    /**
     * 获取节点
     * @param entity 元素
     */
    public abstract JSONObject getNode(T entity);

    /**
     * 获取元素主键
     * @param entity 元素
     * @return 主键
     */
    public abstract P getPrimaryKey(T entity);

    private void getTree(P parentPrimaryKey, JSONObject parentNode, List<T> allEntities) {
        if(allEntities != null && !allEntities.isEmpty()) {
            JSONArray childrenNodes = new JSONArray();
            allEntities.stream().filter(entity -> isChild(parentPrimaryKey, entity)).forEach(entity -> {
                P pk = getPrimaryKey(entity);
                JSONObject childNode = getNode(entity);
                childrenNodes.add(childNode);
                //递归找孙节点
                this.getTree(pk, childNode, allEntities);
            });
            parentNode.put("children", childrenNodes);
        }
    }

    public JSONObject getTree() {
        //获取全部数据
        List<T> allEntities = getAllEntity();
        T rootEntity = getRootEntity();
        JSONObject rootNode = getNode(rootEntity);
        //生成树
        this.getTree(getPrimaryKey(rootEntity), rootNode, allEntities);
        return rootNode;
    }

    public Integer removeEntityAndChildren(P primaryKey) {
        List<T> allEntities = getAllEntity();
        Set<P> needRemove = new HashSet<>();
        needRemove.add(primaryKey);
        findRemoveChildren(primaryKey, allEntities, needRemove);
        if (!needRemove.isEmpty()) {
            removeEntities(needRemove.toArray((P[]) Array.newInstance(primaryKey.getClass(), 0)));
        }
        return needRemove.size();
    }

    private void findRemoveChildren(P primaryKey, List<T> allEntities, Set<P> needRemove) {
        if (allEntities != null && !allEntities.isEmpty()) {
            allEntities.stream().filter(entity -> isChild(primaryKey, entity)).forEach(entity -> {
                P childPk = getPrimaryKey(entity);
                needRemove.add(childPk);
                findRemoveChildren(childPk, allEntities, needRemove);
            });
        }
    }

}
