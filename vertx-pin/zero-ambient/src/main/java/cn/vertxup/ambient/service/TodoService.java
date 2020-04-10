package cn.vertxup.ambient.service;

import cn.vertxup.ambient.domain.tables.daos.XTodoDao;
import cn.vertxup.ambient.domain.tables.pojos.XTodo;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.tp.ambient.cv.AtMsg;
import io.vertx.tp.ambient.cv.em.TodoStatus;
import io.vertx.tp.ambient.init.AtPin;
import io.vertx.tp.ambient.refine.At;
import io.vertx.tp.ke.cv.KeField;
import io.vertx.tp.ke.refine.Ke;
import io.vertx.tp.optic.business.ExTodo;
import io.vertx.up.log.Annal;
import io.vertx.up.unity.Ux;
import io.vertx.up.util.Ut;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TodoService implements TodoStub {
    private static final Annal LOGGER = Annal.get(TodoService.class);

    @Override
    public Future<JsonObject> createTodo(final String type, final JsonObject data) {
        /*
         * Get by type
         */
        final JsonObject defaultTodo = AtPin.getTodo(type);
        final JsonObject inputData = data.copy();
        if (Objects.nonNull(defaultTodo)) {
            /*
             * Expression for defaultTodo
             */
            final JsonObject params = data.copy();
            final String name = Ut.fromExpression(defaultTodo.getString(KeField.NAME), params);
            final String code = Ut.fromExpression(defaultTodo.getString(KeField.CODE), params);
            final String url = Ut.fromExpression(defaultTodo.getString("todoUrl"), params);
            inputData.mergeIn(defaultTodo);
            inputData.put(KeField.NAME, name);
            inputData.put(KeField.CODE, code);
            inputData.put("todoUrl", url);
        }
        final XTodo todo = Ut.deserialize(inputData, XTodo.class);
        return Ux.Jooq.on(XTodoDao.class)
                .insertAsync(todo)
                .compose(Ux::fnJObject);
    }

    @Override
    public Future<JsonArray> fetchTodos(final String sigma, final String type, final JsonArray statues) {
        final JsonObject filters = new JsonObject();
        filters.put("sigma", sigma);
        if (Objects.nonNull(type)) {
            filters.put("type", type);
        }
        filters.put("status,i", statues);
        return Ux.Jooq.on(XTodoDao.class).fetchAndAsync(filters).compose(Ux::fnJArray);
    }

    @Override
    public Future<JsonArray> fetchTodos(final String sigma, final JsonArray types, final JsonArray statues) {
        final JsonObject filters = this.toFilters(sigma, types, statues);
        return Ux.Jooq.on(XTodoDao.class).fetchAndAsync(filters).compose(Ux::fnJArray);
    }

    @Override
    public Future<JsonArray> fetchTodos(final String sigma, final JsonArray types, final JsonArray statues, final JsonArray codes) {
        final JsonObject filters = this.toFilters(sigma, types, statues);
        filters.put("code,i", codes);
        return Ux.Jooq.on(XTodoDao.class).fetchAndAsync(filters).compose(Ux::fnJArray);
    }

    private JsonObject toFilters(final String sigma, final JsonArray types, final JsonArray statues) {
        final JsonObject filters = new JsonObject();
        filters.put("sigma", sigma);
        if (Objects.nonNull(types)) {
            filters.put("type,i", types);
        }
        filters.put("status,i", statues);
        return filters;
    }

    @Override
    public Future<JsonArray> updateStatus(final Set<String> keys, final JsonObject params) {
        return Ux.Jooq.on(XTodoDao.class)
                .<XTodo>fetchInAsync(KeField.KEY, Ut.toJArray(keys))
                .compose(Ux::fnJArray)
                .compose(Ut.ifNil(JsonArray::new, (todoArray) -> {
                    /*
                     * Update status of XTodo
                     */
                    List<XTodo> todoList = Ut.deserialize(todoArray, new TypeReference<List<XTodo>>() {
                    });
                    {
                        /*
                         * XTodo Auditor setting
                         */
                        todoList = todoList.stream().map(todo -> this.combineTodo(todo, params))
                                .collect(Collectors.toList());
                    }
                    return Ux.Jooq.on(XTodoDao.class)
                            .updateAsync(todoList)
                            .compose(Ux::fnJArray);
                }));
    }

    @Override
    public Future<JsonObject> updateStatus(final String key, final JsonObject params) {
        return Ux.Jooq.on(XTodoDao.class)
                .<XTodo>findByIdAsync(key)
                .compose(Ux::fnJObject)
                .compose(Ut.ifJNil((todoJson) -> {
                    /*
                     * Update status of XTodo
                     */
                    XTodo todo = Ut.deserialize(todoJson, XTodo.class);
                    {
                        todo = this.combineTodo(todo, params);
                    }
                    return Ux.Jooq.on(XTodoDao.class)
                            .updateAsync(todo)
                            .compose(Ux::fnJObject);
                }));
    }

    private XTodo combineTodo(final XTodo todo, final JsonObject params) {
        if (Objects.isNull(todo)) {
            return null;
        } else {
            /*
             * XTodo Auditor setting
             */
            final String userId = params.getString(KeField.USER_ID);
            if (Ut.notNil(userId)) {
                todo.setUpdatedBy(userId);
                todo.setUpdatedAt(LocalDateTime.now());
                /*
                 * XTodo once for `createdBy`
                 */
                if (Objects.isNull(todo.getCreatedBy())) {
                    todo.setCreatedBy(userId);
                }
            }
            /*
             * Status
             */
            if (params.containsKey(KeField.STATUS)) {
                final String status = params.getString(KeField.STATUS);
                todo.setStatus(status);
                if (TodoStatus.FINISHED.name().equals(status)) {
                    /*
                     * It means that XTodo has been updated by user
                     */
                    todo.setFinishedBy(todo.getUpdatedBy());
                }
            }
            return todo;
        }
    }

    @Override
    public Future<JsonObject> fetchTodo(final String key) {
        return Ux.Jooq.on(XTodoDao.class)
                .<XTodo>findByIdAsync(key)
                .compose(Ux::fnJObject)
                .compose(Ut.ifNil(JsonObject::new, (todo) -> Ke.channel(ExTodo.class, () -> todo, channel -> {
                    At.infoInit(LOGGER, AtMsg.CHANNEL_TODO, channel.getClass().getName());
                    /*
                     * X_TODO channel and data merged.
                     */
                    final JsonObject params = Ut.elementSubset(todo,
                            KeField.MODEL_ID, KeField.MODEL_CATEGORY, KeField.MODEL_KEY, KeField.SIGMA);
                    return channel.fetchAsync(key, params)
                            .compose(Ut.ifMerge(todo));
                })));
    }
}
