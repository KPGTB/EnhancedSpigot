package dev.projectenhanced.enhancedspigot.util.rest.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class EnhancedResponse {
    private final int status;
    private final String message;
    private final Map<String, List<String>> headers;

    private final String rawOutput;
    @Setter  private Gson gson = new Gson();

    public String getAsString() {
        return this.rawOutput;
    }
    public JsonObject getAsJson() {
        return this.getAsClass(JsonObject.class);
    }
    public <T> T getAsClass(Class<T> clazz) {
        return gson.fromJson(this.rawOutput, clazz);
    }
}
