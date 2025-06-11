package dev.projectenhanced.enhancedspigot.util.rest.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.Builder;
import lombok.Singular;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Builder
public class EnhancedRequest {
    private String url;
    @Builder.Default private HttpMethod method = HttpMethod.GET;

    @Singular private Map<String, String> headers;
    @Singular("cookie") private List<HttpCookie> cookies;
    private String auth;

    @Builder.Default private int connectTimeout = 5000;
    @Builder.Default private int readTimeout = 5000;

    private Body body;

    public EnhancedResponse send() throws IOException {
        this.headers = new HashMap<>(this.headers);

        URL url = new URL(this.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        TryCatchUtil.tryRun(
                () -> connection.setRequestMethod(this.method.name()),
                (e) -> {
                    if(this.method == HttpMethod.PATCH) {
                        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                        TryCatchUtil.tryRun(() -> connection.setRequestMethod("POST"));
                        System.out.println("This JDK version doesn't support PATCH requests! Using hacky thing to make it work, but it may not :c");
                        return;
                    }
                    e.printStackTrace();
                }
        );

        if(this.method != HttpMethod.GET) {
            this.headers.putIfAbsent("Content-Type", "application/json");
            this.headers.putIfAbsent("Accept", "application/json");
        }

        if(!this.cookies.isEmpty()) {
            CookieManager cookieManager = new CookieManager();
            this.cookies.forEach(cookie -> cookieManager.getCookieStore().add(null,cookie));
            this.headers.putIfAbsent("Cookie", cookieManager.getCookieStore().getCookies()
                    .stream()
                    .map(HttpCookie::toString)
                    .collect(Collectors.joining(";"))
            );
        }

        if(this.auth != null) {
            this.headers.putIfAbsent("Authorization", this.auth);
        }

        this.headers.forEach(connection::setRequestProperty);
        connection.setConnectTimeout(this.connectTimeout);
        connection.setReadTimeout(this.readTimeout);

        if(this.method != HttpMethod.GET && this.body != null) {
            connection.setDoOutput(true);
            try(OutputStream out = connection.getOutputStream()) {
                byte[] input = this.body.build().getBytes(StandardCharsets.UTF_8);
                out.write(input,0,input.length);
            }
        }

        int status = connection.getResponseCode();
        String message = connection.getResponseMessage();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                status > 299 ? connection.getErrorStream() : connection.getInputStream(),
                StandardCharsets.UTF_8
        ));

        StringBuilder responseBuilder = new StringBuilder();
        String responseLine;
        while((responseLine = reader.readLine()) != null) {
            responseBuilder.append(responseLine.trim());
        }

        reader.close();

        Map<String,List<String>> responseHeaders = connection.getHeaderFields();

        connection.disconnect();
        return new EnhancedResponse(status,message,responseHeaders,responseBuilder.toString());
    }

    public CompletableFuture<EnhancedResponse> sendAsync() {
        return CompletableFuture.supplyAsync(() -> TryCatchUtil.tryAndReturn(this::send));
    }

    public static class Body {
        private final JsonObject body;
        private Gson gson;

        protected Body(JsonObject body) {
            this.body = body;
            this.gson = new Gson();
        }
        public static Body builder() {
            return new Body(new JsonObject());
        }

        public Body gson(Gson gson) {
            this.gson = gson;
            return this;
        }

        public Body set(String key, String value) {
            this.body.addProperty(key,value);
            return this;
        }
        public Body set(String key, Number value) {
            this.body.addProperty(key,value);
            return this;
        }
        public Body set(String key, char value) {
            this.body.addProperty(key,value);
            return this;
        }
        public Body set(String key, boolean value) {
            this.body.addProperty(key,value);
            return this;
        }
        public Body set(String key, Object value) {
            this.body.add(key, this.gson.toJsonTree(value));
            return this;
        }

        public Body setStrings(String key, Collection<String> value) {
            JsonArray array = new JsonArray();
            value.forEach(array::add);
            this.body.add(key,array);
            return this;
        }
        public Body setNumbers(String key, Collection<Number> value) {
            JsonArray array = new JsonArray();
            value.forEach(array::add);
            this.body.add(key,array);
            return this;
        }
        public Body setCharacters(String key, Collection<Character> value) {
            JsonArray array = new JsonArray();
            value.forEach(array::add);
            this.body.add(key,array);
            return this;
        }
        public Body setBooleans(String key, Collection<Boolean> value) {
            JsonArray array = new JsonArray();
            value.forEach(array::add);
            this.body.add(key,array);
            return this;
        }
        public Body setObjects(String key, Collection<Object> value) {
            JsonArray array = new JsonArray();
            value.forEach(element -> array.add(this.gson.toJsonTree(element)));
            this.body.add(key,array);
            return this;
        }
        public Body addBuilders(String key, Collection<Body> value) {
            JsonArray array = new JsonArray();
            value.forEach(element -> array.add(element.body));
            this.body.add(key,array);
            return this;
        }

        public Body set(String key, Body builder) {
            this.body.add(key,builder.body);
            return this;
        }

        public String build() {
            return this.gson.toJson(this.body);
        }
    }
}
