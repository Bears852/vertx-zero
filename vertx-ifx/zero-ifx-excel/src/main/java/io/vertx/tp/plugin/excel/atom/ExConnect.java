package io.vertx.tp.plugin.excel.atom;

import com.fasterxml.jackson.databind.JsonArrayDeserializer;
import com.fasterxml.jackson.databind.JsonArraySerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.vertx.core.json.JsonArray;

import java.io.Serializable;

/**
 * Connect configuration data to
 * Dao / Pojo class
 */
public class ExConnect implements Serializable {

    private transient String table;
    private transient Class<?> pojo;
    private transient Class<?> dao;
    private transient String pojoFile;

    @JsonSerialize(using = JsonArraySerializer.class)
    @JsonDeserialize(using = JsonArrayDeserializer.class)
    private transient JsonArray unique;
    private transient String key;

    public String getTable() {
        return this.table;
    }

    public void setTable(final String table) {
        this.table = table;
    }

    public Class<?> getPojo() {
        return this.pojo;
    }

    public void setPojo(final Class<?> pojo) {
        this.pojo = pojo;
    }

    public Class<?> getDao() {
        return this.dao;
    }

    public void setDao(final Class<?> dao) {
        this.dao = dao;
    }

    public String getPojoFile() { return pojoFile; }

    public void setPojoFile(String pojoFile) { this.pojoFile = pojoFile; }

    public JsonArray getUnique() {
        return this.unique;
    }

    public void setUnique(final JsonArray unique) {
        this.unique = unique;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "ExConnect{" +
                "table='" + table + '\'' +
                ", pojo=" + pojo +
                ", dao=" + dao +
                ", pojoFile='" + pojoFile + '\'' +
                ", unique=" + unique +
                ", key='" + key + '\'' +
                '}';
    }
}
