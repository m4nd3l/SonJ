package dev.m4nd3l.sonj;

import dev.m4nd3l.sonj.annotations.Expose;
import dev.m4nd3l.sonj.annotations.Hide;
import dev.m4nd3l.sonj.annotations.SonJName;
import dev.m4nd3l.sonj.deserialization.lexer.JsonLexer;
import dev.m4nd3l.sonj.deserialization.parser.JsonParser;
import dev.m4nd3l.sonj.deserialization.parser.ast.JsonArray;
import dev.m4nd3l.sonj.deserialization.parser.ast.JsonElement;
import dev.m4nd3l.sonj.deserialization.parser.ast.JsonObject;
import dev.m4nd3l.sonj.deserialization.parser.ast.JsonValue;
import dev.m4nd3l.sonj.json.JsonMapNode;
import dev.m4nd3l.sonj.utils.FieldMetadata;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.*;

public class JsonDeserializer<T> {
    private Class<T> target;
    private T instance;
    private String source;
    private SonJ sonjInstance;

    private JsonLexer lexer;
    private JsonParser parser;

    public JsonDeserializer(Class<T> target, String source, SonJ sonjInstance) {
        this.sonjInstance = sonjInstance;
        this.source = source;
        this.target = target;
        lexer = new JsonLexer(source);
    }
    public JsonDeserializer(Class<T> target, File source, SonJ sonjInstance) throws IOException {
        this(target, Files.readString(source.toPath()), sonjInstance);
    }

    public T deserialize() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        parser = new JsonParser(lexer.tokenize());
        JsonElement parsedSource = parser.parse();
        instance = (T) deserialize(parsedSource, target);
        return instance;
    }

    private Object deserialize(JsonElement element, Class<?> clazz)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        if (element instanceof JsonValue value) return value.getValue();
        if (element instanceof JsonArray value) return handleArray(value, clazz);
        if (clazz.equals(Map.class)) return handleMap((JsonArray) element);

        JsonObject object = (JsonObject) element;

        if (!sonjInstance.classFieldsAnnotations.containsKey(clazz)) sonjInstance.addAnnotations(clazz);

        Object result = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = getFields(clazz);
        for (Field field : fields) {
            String fieldName = field.getName();
            List<String> aliases = new ArrayList<>(List.of(fieldName));
            FieldMetadata metadata = sonjInstance.classFieldsAnnotations.get(clazz).get(fieldName);

            // Check for field's suitability
            if (!sonjInstance.ignoreHideAnnotation && metadata.has(Hide.class) && metadata.get(Hide.class).deserialize()) continue;
            if (sonjInstance.ignoreStaticFields && metadata.isStatic()) continue;
            if (sonjInstance.acceptTransientKeyword && metadata.isTransient()) continue;
            if (sonjInstance.excludeFieldsWithoutExposeAnnotation && !isExposeForDeserialization(metadata)) continue;

            // Possible names
            if (metadata.has(SonJName.class)) {
                SonJName names = metadata.get(SonJName.class);
                if (names != null) {
                    aliases.add(names.value());
                    aliases.addAll(Arrays.asList(names.alternative()));
                }
            }

            Class<?> type = field.getType();
            JsonElement parsedValue = JsonValue.NULL;
            if (object.hasKey(aliases)) parsedValue = object.get(aliases);
            Object value = convertValue(deserialize(parsedValue, type), type);
            boolean isAccessible = field.canAccess(result);
            if (!isAccessible) field.setAccessible(true);
            field.set(result, value);
            field.setAccessible(isAccessible);
        }

        return result;
    }

    private Object handleArray(JsonArray array, Class<?> clazz)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Object result = Array.newInstance(clazz, array.size());

        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            Object value = deserialize(element, clazz);
            Array.set(result, i, value);
        }

        return result;
    }

    private <K, V> Map<K, V> handleMap(JsonArray nodesArray)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Map<K, V> result = new HashMap<>();

        JsonMapNode<K, V>[] nodes = (JsonMapNode<K, V>[]) handleArray(nodesArray, JsonMapNode.class);
        for (JsonMapNode<K, V> node : nodes) result.put(node.key(), node.value());
        return result;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType.isInstance(value)) {
            return value;
        }

        if (value instanceof Number number) {
            if (targetType == int.class || targetType == Integer.class) return number.intValue();
            if (targetType == long.class || targetType == Long.class) return number.longValue();
            if (targetType == double.class || targetType == Double.class) return number.doubleValue();
            if (targetType == float.class || targetType == Float.class) return number.floatValue();
            if (targetType == short.class || targetType == Short.class) return number.shortValue();
            if (targetType == byte.class || targetType == Byte.class) return number.byteValue();
        }

        if (targetType == boolean.class && value instanceof Boolean) {
            return value;
        }

        return value;
    }

    private Field[] getFields(Class<?> clazz) {
        Class<?> currentClass = clazz;
        List<Field> fields = new ArrayList<>();
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.stream(currentClass.getDeclaredFields()).toList());
            currentClass = currentClass.getSuperclass();
        }
        if (fields.isEmpty()) return new Field[] {};
        return fields.toArray(new Field[0]);
    }

    private boolean isExposeForDeserialization(FieldMetadata metadata) {
        if (!metadata.has(Expose.class)) return false;
        return metadata.get(Expose.class).deserialize();
    }
}
