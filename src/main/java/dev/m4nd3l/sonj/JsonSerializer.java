package dev.m4nd3l.sonj;

import dev.m4nd3l.sonj.annotations.Expose;
import dev.m4nd3l.sonj.annotations.Hide;
import dev.m4nd3l.sonj.annotations.SonJName;
import dev.m4nd3l.sonj.exceptions.CircularReferenceException;
import dev.m4nd3l.sonj.json.JsonMapNode;
import dev.m4nd3l.sonj.serialization.builders.Builder;
import dev.m4nd3l.sonj.serialization.builders.JsonBuilder;
import dev.m4nd3l.sonj.serialization.builders.JsonFileBuilder;
import dev.m4nd3l.sonj.serialization.symbols.JsonMainSymbol;
import dev.m4nd3l.sonj.settings.NumberDecimals;
import dev.m4nd3l.sonj.utils.FieldMetadata;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.util.*;

public class JsonSerializer {
    private Object source;
    private Set<Object> visited;
    private Builder builder;
    private SonJ sonjInstance;

    public JsonSerializer(Object source, File file, SonJ sonjInstance) throws IOException {
        this.builder = file == null ? new JsonBuilder(sonjInstance.style) : new JsonFileBuilder(sonjInstance.style, file);
        this.source = source;
        this.visited = Collections.newSetFromMap(new IdentityHashMap<>());
        this.sonjInstance = sonjInstance;
    }

    public JsonSerializer(Object source, SonJ sonjInstance) throws IOException {
        this(source, null, sonjInstance);
    }

    public void serialize() throws IOException, CircularReferenceException {
        try {
            toJson(source, true);
            builder.flush();
        } finally { builder.close(); }
    }

    @Override
    public String toString() { return builder.toString(); }

    public void toJson(Object source, boolean appendIndent)
            throws CircularReferenceException, IOException {
        if (source == null) {
            builder.append(JsonMainSymbol.NULL, appendIndent);
            return;
        }

        if (visited.contains(source))
            if (sonjInstance.makeNullIfCircularReference) {
                builder.append(JsonMainSymbol.NULL, appendIndent);
                return;
            }
            else throw new CircularReferenceException(source);

        if (!isPrimitive(source)) visited.add(source);

        Class<?> clazz = source.getClass();

        if (clazz.isArray()) {
            handleArray(source, appendIndent, false);
            return;
        }

        switch (source) {
            case String text          -> builder.appendJsonString(text, appendIndent);
            case Character character  -> builder.appendJsonString(String.valueOf(character), appendIndent);
            case Boolean bool         -> builder.append(bool, appendIndent);
            case Date date            -> builder.appendJsonString(sonjInstance.dateFormat.format(date), appendIndent);
            case UUID uuid            -> builder.appendJsonString(uuid.toString(), appendIndent);
            case Number num           -> {
                switch (num) {
                    case Long   longNum  -> builder.append(longNum, appendIndent);
                    case Short shortNum  -> builder.append(shortNum, appendIndent);
                    case Integer integer -> builder.append(integer, appendIndent);
                    case Float fl        -> builder.append(fl, appendIndent);
                    case Double dl       -> builder.append(sonjInstance.decimals == NumberDecimals.DOUBLE ? dl : dl.floatValue(), appendIndent);
                    default              -> builder.append(num, appendIndent);
                }
            }
            case Enum<?> enumSource   -> builder.appendJsonString(enumSource.name(), appendIndent);
            case Collection<?> list   -> handleArray(list.toArray(), appendIndent, false);
            case Map<?, ?> map        -> handleMap(map);
            case Iterable<?> ignored  -> handleArray(source, appendIndent, false);
            default -> handleObject(source, appendIndent);
        }
        builder.flush();
    }

    private void handleMap(Object mapObj)
            throws CircularReferenceException, IOException {
        Map<?, ?> map = (Map<?, ?>) mapObj;
        List<JsonMapNode<?, ?>> nodes = new ArrayList<>();
        map.forEach((key, value) -> nodes.add(new JsonMapNode<>(key, value)));
        handleArray(nodes.toArray(), true, true);
    }

    private void handleArray(Object array, boolean indent, boolean forMap)
            throws CircularReferenceException, IOException {
        builder.append(JsonMainSymbol.OPEN_BRACKET, !forMap && indent).newLine();
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object arrayItem = Array.get(array, i);
            toJson(arrayItem, true);
            if (i < length - 1) builder.append(JsonMainSymbol.COMMA).newLine();
            else builder.newLine();
        }
        builder.append(JsonMainSymbol.CLOSED_BRACKET, true);
    }

    private void handleObject(Object source, boolean appendIndent)
            throws CircularReferenceException, IOException {
        if (source == null) {
            builder.append(JsonMainSymbol.NULL, appendIndent).newLine();
            return;
        }

        if (!sonjInstance.classFieldsAnnotations.containsKey(source.getClass())) sonjInstance.addAnnotations(source.getClass());

        builder.append(JsonMainSymbol.OPEN_CURLY_BRACKET, appendIndent).newLine();
        Field[] fields = getFields(source);
        boolean first = true;
        for (Field field : fields) {

            String fieldName = field.getName();
            FieldMetadata metadata = sonjInstance.classFieldsAnnotations.get(source.getClass()).get(fieldName);

            // Check if suitable for serialization
            if (sonjInstance.excludeFieldsWithoutExposeAnnotation && !isExposeForSerialization(metadata)) continue;
            if (!sonjInstance.ignoreHideAnnotation && metadata.has(Hide.class) && metadata.get(Hide.class).serialize()) continue;
            if (!sonjInstance.acceptTransientKeyword && metadata.isTransient()) continue;
            if (sonjInstance.ignoreStaticFields && metadata.isStatic()) continue;

            if (metadata.isPrivate() && sonjInstance.ignorePrivateFields && !isExposeForSerialization(metadata)) continue;

            try { field.setAccessible(true); } catch (Exception ignored) { continue; }

            Object value;
            try { value = field.get(source); } catch (Exception ignored) { continue; }

            if (!sonjInstance.serializeNulls && value == null && !isExposeForSerialization(metadata)) continue;

            if (!first) builder.append(JsonMainSymbol.COMMA).newLine();

            // Serialized name searching
            String serializedName = field.getName();

            if (!sonjInstance.ignoreSerializedNameAnnotation && metadata.has(SonJName.class))
                serializedName = metadata.get(SonJName.class).value();

            builder.appendIndent().appendJsonString(serializedName).append(JsonMainSymbol.COLON);

            toJson(value, false);

            field.setAccessible(false);
            first = false;
        }

        builder.newLine();
        builder.append(JsonMainSymbol.CLOSED_CURLY_BRACKET, true);
    }

    private Field[] getFields(Object source) {
        Class<?> currentClass = source.getClass();
        List<Field> fields = new ArrayList<>();
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.stream(currentClass.getDeclaredFields()).toList());
            currentClass = currentClass.getSuperclass();
        }
        if (fields.isEmpty()) return new Field[] {};
        return fields.toArray(new Field[0]);
    }

    private boolean isExposeForSerialization(FieldMetadata metadata) {
        if (!metadata.has(Expose.class)) return false;
        if (!metadata.get(Expose.class).serialize()) return false;
        return true;
    }

    private boolean isPrimitive(Object source) {
        if (source.getClass().isPrimitive()) return true;
        return switch (source) {
            case String ignored -> true;
            case Number ignored -> true;
            case Boolean ignored -> true;
            case Class<?> ignored -> true;
            case Enum<?> ignored -> true;
            case Buffer ignored -> true;
            case Runnable ignored -> true;
            default -> false;
        };
    }
}
