package io.vertx.tp.ambient.cv;

public interface AtMsg {

    String SOURCE = "Get data source from appId = {0}";

    String INIT_APP = "XApp initializing with: {0}";

    String INIT_SOURCE = "XSource initializing with: {0}";

    String INIT_DATUM = "Data Loading: {0}";

    String INIT_DATUM_EACH = "Data Loading: filename = {0}";

    String INIT_DATABASE = "Database initialization: {0}";

    String INIT_DB_RT = "Workflow for database: {0}";

    String FILE_UPLOAD = "Upload parameters: {0}";

    String FILE_DOWNLOAD = "Download: key = {0}";

    String UNITY_APP = "{0} Application have been initialized successfully!";

    String UNITY_SOURCE = "{0} Data source have been initialized successfully!";

    String CHANNEL_TODO = "Todo channel selected: {0}";
}
