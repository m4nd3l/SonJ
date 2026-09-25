package dev.m4nd3l.sonj.deserialization.parser.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonObject extends JsonElement {
    private Map<String, JsonElement> elements;
    public JsonObject(Map<String, JsonElement> elements) { this.elements = elements; }
    public JsonObject() { this(new HashMap<>()); }
    public JsonObject put(String key, JsonElement value) { elements.put(key, value); return this; }
    public boolean hasKey(List<String> aliases) {
        for (String key : aliases) if (elements.containsKey(key)) return true;
        return false;
    }
    public JsonElement get(List<String> aliases) {
        Optional<String> existantKey = aliases.stream().filter(key -> elements.containsKey(key)).findFirst();
        if (!existantKey.isPresent()) return JsonValue.NULL;
        JsonElement obtained = elements.getOrDefault(existantKey.get(), JsonValue.NOT_FOUND);
        if (obtained == null) return JsonValue.NULL;
        return obtained;
    }
    public JsonElement get(String key) {
        JsonElement obtained = elements.getOrDefault(key, JsonValue.NOT_FOUND);
        if (obtained == null) return JsonValue.NULL;
        return obtained;
    }
}
