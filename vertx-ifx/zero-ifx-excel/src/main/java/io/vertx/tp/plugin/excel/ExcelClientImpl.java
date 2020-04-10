package io.vertx.tp.plugin.excel;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.tp.error._500ExportingErrorException;
import io.vertx.tp.plugin.excel.atom.ExRecord;
import io.vertx.tp.plugin.excel.atom.ExTable;
import io.vertx.tp.plugin.excel.tool.ExFn;
import io.vertx.up.exception.WebException;
import io.vertx.up.exception.web._500InternalServerException;
import io.vertx.up.fn.Fn;
import io.vertx.up.log.Annal;
import io.vertx.up.unity.Ux;
import io.vertx.up.unity.jq.UxJooq;
import io.vertx.up.util.Ut;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelClientImpl implements ExcelClient {

    private static final Annal LOGGER = Annal.get(ExcelClientImpl.class);

    private transient final Vertx vertx;
    private transient final ExcelHelper helper = ExcelHelper.helper(this.getClass());
    private transient final String temp;

    ExcelClientImpl(final Vertx vertx, final JsonObject config) {
        this.vertx = vertx;
        final String temp = config.getString("temp");
        this.temp = Ut.isNil(temp) ? "/tmp" : temp;
        this.init(config);
    }

    @Override
    public ExcelClient init(final JsonObject config) {
        final JsonArray mapping = config.getJsonArray(MAPPING);
        this.helper.initConnect(mapping);
        LOGGER.debug("[ Έξοδος ] Configuration finished: {0}", Pool.CONNECTS.size());
        if (config.containsKey(ENVIRONMENT)) {
            final JsonArray environments = config.getJsonArray(ENVIRONMENT);
            this.helper.initEnvironment(environments);
            LOGGER.debug("[ Έξοδος ] Configuration environments: {0}", environments.encode());
        }
        return this;
    }

    @Override
    public ExcelClient ingest(final String filename, final Handler<AsyncResult<Set<ExTable>>> handler) {
        handler.handle(Future.succeededFuture(this.ingest(filename)));
        return this;
    }

    @Override
    public Set<ExTable> ingest(final String filename) {
        /* 1. Get Workbook reference */
        final Workbook workbook = this.helper.getWorkbook(filename);
        /* 2. Iterator for Sheet */
        return this.helper.getExTables(workbook);
    }

    @Override
    public ExcelClient ingest(final InputStream in, final boolean isXlsx, final Handler<AsyncResult<Set<ExTable>>> handler) {
        handler.handle(Future.succeededFuture(this.ingest(in, isXlsx)));
        return this;
    }

    @Override
    public Set<ExTable> ingest(final InputStream in, final boolean isXlsx) {
        /* 1. Get Workbook reference */
        final Workbook workbook = this.helper.getWorkbook(in, isXlsx);
        /* 2. Iterator for Sheet */
        return this.helper.getExTables(workbook);
    }

    @Override
    public <T> ExcelClient loading(final String filename, final Handler<AsyncResult<Set<T>>> handler) {
        return this.ingest(filename, process -> handler.handle(this.handleIngested(process)));
    }

    @Override
    @Fluent
    public <T> ExcelClient importTable(final String tableOnly, final String filename, final Handler<AsyncResult<Set<T>>> handler) {
        return this.ingest(filename, processed -> {
            if (processed.succeeded()) {
                /* Filtered valid table here */
                final Set<ExTable> execution = this.getFiltered(processed.result(), tableOnly);
                handler.handle(this.handleIngested(Ux.future(execution)));
            }
        });
    }

    @Override
    @Fluent
    public <T> ExcelClient importTable(final String tableOnly, final InputStream in, final Handler<AsyncResult<Set<T>>> handler) {
        return this.ingest(in, true, processed -> {
            if (processed.succeeded()) {
                /* Filtered valid table here */
                final Set<ExTable> execution = this.getFiltered(processed.result(), tableOnly);
                handler.handle(this.handleIngested(Ux.future(execution)));
            }
        });
    }

    @Override
    public <T> ExcelClient loading(final InputStream in, final boolean isXlsx, final Handler<AsyncResult<Set<T>>> handler) {
        return this.ingest(in, isXlsx, process -> handler.handle(this.handleIngested(process)));
    }

    private <T> Future<Set<T>> handleIngested(final AsyncResult<Set<ExTable>> async) {
        if (async.succeeded()) {
            final Set<ExTable> tables = async.result();
            /* 3. Loading data into the system */
            final Set<T> entitySet = new HashSet<>();
            tables.forEach(table -> this.extract(table).forEach(json -> {
                /* 4. Filters building */
                final T entity = this.saveEntity(json, table);
                if (Objects.nonNull(entity)) {
                    entitySet.add(entity);
                }
            }));
            /* 4. Set<T> handler */
            return Future.succeededFuture(entitySet);
        } else {
            return Future.succeededFuture();
        }
    }

    @Override
    public <T> T saveEntity(final JsonObject data, final ExTable table) {
        T reference = null;
        if (Objects.nonNull(table.getPojo()) && Objects.nonNull(table.getDao())) {
            /*
             * First, find the record by unique filters that defined in business here.
             */
            final JsonObject filters = table.whereUnique(data);
            LOGGER.debug("[ Έξοδος ] Filters: {0}, Table: {1}", filters.encode(), table.getName());
            final T entity = Ux.fromJson(data, table.getPojo(), table.getPojoFile());
            final UxJooq jooq = Ux.Jooq.on(table.getDao());
            if (null != jooq) {
                final String pojoFile = table.getPojoFile();
                if (Ut.notNil(pojoFile)) {
                    jooq.on(pojoFile);
                }
                /*
                 * Unique filter to fetch single record database here.
                 * Such as code + sigma
                 */
                final T queried = jooq.fetchOne(filters);
                if (null == queried) {
                    /*
                     * Here are two situations that we could be careful
                     * 1. Unique Condition in source does not change, do insert here.
                     * 2. Key Condition existing in database, do update here.
                     */
                    final String key = table.whereKey(data);
                    if (Ut.isNil(key)) {
                        /*
                         * No definition of key here, insert directly.
                         */
                        reference = jooq.insert(entity);
                    } else {
                        /*
                         * Double check to avoid issue:
                         * java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'xxx' for key 'PRIMARY'
                         */
                        final T fetched = jooq.findById(key);
                        if (null == fetched) {
                            /*
                             * In this situation, it common workflow to do data loading.
                             */
                            reference = jooq.insert(entity);
                        } else {
                            /*
                             * In this situation, it means the old unique filters have been changed.
                             * Such as:
                             * From
                             * id,      code,      sigma
                             * 001,     AB.CODE,   5sLyA90qSo7
                             *
                             * To
                             * id,      code,      sigma
                             * 001,     AB.CODE1,  5sLyA90qSo7
                             *
                             * Above example could show that primary key has not been modified
                             */
                            reference = jooq.update(entity);
                        }
                    }
                } else {
                    /*
                     * code, sigma did not change and we could identify this record
                     * do update directly to modify old information.
                     */
                    reference = jooq.update(entity);
                }
            }
        }
        return reference;
    }

    @Override
    public ExcelClient exportTable(final String identifier, final JsonArray data, final Handler<AsyncResult<Buffer>> handler) {
        /* 1. Workbook created */
        final XSSFWorkbook workbook = new XSSFWorkbook();
        /* 2. Sheet created */
        final XSSFSheet sheet = workbook.createSheet(identifier);
        /* 3. Row created */
        final List<Integer> sizeList = new ArrayList<>();
        Ut.itJArray(data, JsonArray.class, (rowData, index) -> {
            ExFn.generateData(sheet, index, rowData);
            sizeList.add(rowData.size());
        });
        /* 4. Adjust column width */
        final IntSummaryStatistics statistics = sizeList.stream().mapToInt(Integer::intValue).summaryStatistics();
        final int max = statistics.getMax();
        for (int idx = 0; idx < max; idx++) {
            sheet.autoSizeColumn(idx);
        }
        /* 5. OutputStream */
        Fn.safeJvm(() -> {
            // TODO: Modified in future
            final String filename = identifier + "." + UUID.randomUUID() + ".xlsx";
            final OutputStream out = new FileOutputStream(filename);
            workbook.write(out);
            // InputStream converted
            handler.handle(Ux.future(Ut.ioBuffer(filename)));
        });
        return this;
    }

    @Override
    public Future<Buffer> exportTable(final String identifier, final JsonArray data) {
        final Promise<Buffer> future = Promise.promise();
        this.exportTable(identifier, data, handler -> {
            if (handler.succeeded()) {
                future.complete(handler.result());
            } else {
                final Throwable error = handler.cause();
                if (Objects.nonNull(error)) {
                    final WebException failure = new _500ExportingErrorException(this.getClass(), error.getMessage());
                    future.fail(failure);
                } else {
                    future.fail(new _500InternalServerException(this.getClass(), "Unexpected Error"));
                }
            }
        });
        return future.future();
    }

    private Set<ExTable> getFiltered(final Set<ExTable> processed, final String tableOnly) {
        return processed.stream()
                .filter(table -> tableOnly.equals(table.getName()))
                .collect(Collectors.toSet());
    }

    private List<JsonObject> extract(final ExTable table) {
        /* Records extracting */
        final List<ExRecord> records = table.get();
        LOGGER.info("[ Έξοδος ] Table: {0}, Data Size: {1}", table.getName(), records.size());
        /* Pojo Processing */
        return records.stream().filter(Objects::nonNull)
                .map(ExRecord::toJson)
                .collect(Collectors.toList());
    }
}
