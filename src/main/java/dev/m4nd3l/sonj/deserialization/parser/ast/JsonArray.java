package dev.m4nd3l.sonj.deserialization.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class JsonArray extends JsonElement {
    private List<JsonElement> elements;
    public JsonArray(List<JsonElement> elements) { this.elements = elements; }
    public JsonArray() { this(new ArrayList<>()); }
    public int size() { return elements.size(); }
    public JsonArray put(JsonElement element) { elements.add(element); return this; }
    public JsonElement get(int index) {
        if (index < 0 || index >= elements.size()) return JsonValue.NOT_FOUND;
        JsonElement obtained = elements.get(index);
        return (obtained == null) ? JsonValue.NULL : obtained;
    }
}
