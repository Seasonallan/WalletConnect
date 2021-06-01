package com.season.myapplication.entity;

public class JsonRpcModels {

    static String JSONRPC_VERSION = "2.0";

    public static class JsonRpcRequest<T> {
        public long id;
        public String jsonrpc = JSONRPC_VERSION;
        public Enums.WCMethod method;
        public T params;
    }

    public static class JsonRpcResponse<T> {
        public long id;
        public String jsonrpc = JSONRPC_VERSION;
        public T result;
    }

    public static class JsonRpcErrorResponse {
        public long id;
        public String jsonrpc = JSONRPC_VERSION;
        public JsonRpcError error;
    }


    public static class JsonRpcError {
        public int code;
        public String message;

        public static JsonRpcError serverError(String message) {
            return messageCode(message, -32000);
        }

        public static JsonRpcError invalidParams(String message) {
            return messageCode(message, -32602);
        }

        public static JsonRpcError invalidRequest(String message) {
            return messageCode(message, -32600);
        }

        public static JsonRpcError parseError(String message) {
            return messageCode(message, -32700);
        }

        public static JsonRpcError methodNotFound(String message) {
            return messageCode(message, -32601);
        }

        static JsonRpcError messageCode(String message, int code) {
            JsonRpcError jsonRpcError = new JsonRpcError();
            jsonRpcError.message = message;
            jsonRpcError.code = code;
            return jsonRpcError;
        }


    }


}
