package dev.m4nd3l.sonj;

import dev.m4nd3l.sonj.annotations.Expose;
import dev.m4nd3l.sonj.annotations.Hide;
import dev.m4nd3l.sonj.annotations.SonJName;
import dev.m4nd3l.sonj.exceptions.CircularReferenceException;
import dev.m4nd3l.sonj.json.JsonBuilder;
import dev.m4nd3l.sonj.json.JsonMainSymbol;
import dev.m4nd3l.sonj.json.JsonMapNode;
import dev.m4nd3l.sonj.settings.NumberDecimals;
import dev.m4nd3l.sonj.settings.style.Style;
import dev.m4nd3l.sonj.utils.FieldMetadata;
import dev.m4nd3l.sonj.utils.Version;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class SonJ {
    public static Version VERSION = new Version(1, 0, 0);
    public static SonJ defaultSonJ = new SonJBuilder()
            .setAmericanDateFormat()
            .create();

    private boolean
            ignoreSerializedNameAnnotation,
            ignoreHideAnnotation,
            makeNullIfCircularReference,
            acceptTransientKeyword,
            ignorePrivateFields,
            excludeFieldsWithoutExposeAnnotation,
            serializeNulls;
    private Style style;
    private SimpleDateFormat dateFormat;
    private NumberDecimals decimals;
    private Map<Class<?>, Map<String, FieldMetadata>> classFieldsAnnotations;

    public SonJ(boolean ignoreSerializedNameAnnotation, boolean ignoreHideAnnotation, boolean makeNullIfCircularReference,
                boolean acceptTransientKeyword, boolean ignorePrivateFields, boolean excludeFieldsWithoutExposeAnnotation,
                boolean serializeNulls, Style style, SimpleDateFormat dateFormat, NumberDecimals decimals) {
        this.ignoreSerializedNameAnnotation = ignoreSerializedNameAnnotation;
        this.ignoreHideAnnotation = ignoreHideAnnotation;
        this.makeNullIfCircularReference = makeNullIfCircularReference;
        this.acceptTransientKeyword = acceptTransientKeyword;
        this.ignorePrivateFields = ignorePrivateFields;
        this.excludeFieldsWithoutExposeAnnotation = excludeFieldsWithoutExposeAnnotation;
        this.serializeNulls = serializeNulls;
        this.style = style;
        this.dateFormat = dateFormat;
        this.decimals = decimals;

        this.classFieldsAnnotations = new HashMap<>();
    }

    public String serialize(Object source)
            throws CircularReferenceException {
        if (source == null) return "null";
        var start = System.currentTimeMillis();
        JsonBuilder builder = new JsonBuilder(style);
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        toJson(source, builder, visited, true);
        System.out.println(System.currentTimeMillis() - start);
        return builder.toString();
    }

    public <T> T deserialize(String source, Class<T> clazz) {
        return null;
    }
    
    private void toJson(Object source, JsonBuilder builder, Set<Object> visited, boolean appendIndent)
            throws CircularReferenceException {
        if (source == null) {
            builder.append("null", appendIndent);
            return;
        }

        if (visited.contains(source))
            if (makeNullIfCircularReference) {
                builder.append("null", appendIndent);
                return;
            }
            else throw new CircularReferenceException(source);

        visited.add(source);

        Class<?> clazz = source.getClass();

        if (clazz.isArray()) {
            handleArray(source, builder, visited, appendIndent);
            return;
        }

        switch (source) {
            case String text          -> builder.appendJsonString(text, appendIndent);
            case Character character  -> builder.appendJsonString(String.valueOf(character), appendIndent);
            case Boolean bool         -> builder.append(bool, appendIndent);
            case Date date            -> builder.appendJsonString(dateFormat.format(date), appendIndent);
            case UUID uuid            -> builder.appendJsonString(uuid.toString(), appendIndent);
            case Number num           -> {
                switch (num) {
                    case Integer integer -> builder.append(integer, appendIndent);
                    case Float fl        -> builder.append(fl, appendIndent);
                    case Double dl       -> builder.append(decimals == NumberDecimals.DOUBLE ? dl : dl.floatValue(), appendIndent);
                    default              -> builder.append(num, appendIndent);
                }
            }
            case Collection<?> list   -> handleArray(list.toArray(), builder, visited, appendIndent);
            case Iterable<?> iterable -> handleArray(source, builder, visited, appendIndent);
            case Map<?, ?> map        -> handleMap(map, builder, visited);
            default -> handleObject(source, builder, visited, appendIndent);
        }

        visited.remove(source);
    }

    private void handleMap(Object mapObj, JsonBuilder builder, Set<Object> visited) throws CircularReferenceException {
        Map<?, ?> map = (Map<?, ?>) mapObj;
        List<JsonMapNode<?, ?>> nodes = new ArrayList<>();
        map.forEach((key, value) -> nodes.add(new JsonMapNode<>(key, value)));
        handleArray(nodes.toArray(), builder, visited, true);
    }

    private void handleArray(Object array, JsonBuilder builder, Set<Object> visited, boolean indent) throws CircularReferenceException {
        builder.append(JsonMainSymbol.OPEN_BRACKET, indent).newLine();
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object arrayItem = Array.get(array, i);
            toJson(arrayItem, builder, visited, true);
            if (i < length - 1) builder.append(JsonMainSymbol.COMMA).newLine();
            else builder.newLine();
        }
        builder.append(JsonMainSymbol.CLOSED_BRACKET, true);
    }

    private void handleObject(Object source, JsonBuilder builder, Set<Object> visited, boolean appendIndent)
            throws CircularReferenceException {
        if (source == null) {
            builder.append(JsonMainSymbol.NULL, appendIndent).newLine();
            return;
        }
        builder.append(JsonMainSymbol.OPEN_CURLY_BRACKET, appendIndent).newLine();
        Field[] fields = getFields(source);
        boolean first = true;
        for (Field field : fields) {

            // Add class metadata to classFieldsAnnotations
            if (!classFieldsAnnotations.containsKey(source.getClass())) addAnnotations(source);

            String fieldName = field.getName();
            FieldMetadata metadata = classFieldsAnnotations.get(source.getClass()).get(fieldName);

            // Check if suitable for serialization
            if (excludeFieldsWithoutExposeAnnotation && !isExposeForSerialization(metadata)) continue;
            if (!ignoreHideAnnotation && metadata.has(Hide.class)) continue;
            if (acceptTransientKeyword && metadata.isTransient()) continue;

            if (metadata.isPrivate() && ignorePrivateFields && !isExposeForSerialization(metadata)) continue;

            field.setAccessible(true);

            Object value;
            try { value = field.get(source); } catch (Exception e) {
                System.out.println(e);
                continue; }

            if (!serializeNulls && value == null && !isExposeForSerialization(metadata)) continue;

            if (!first) builder.append(JsonMainSymbol.COMMA).newLine();

            // Serialized name searching
            String serializedName = field.getName();

            if (!ignoreSerializedNameAnnotation && metadata.has(SonJName.class))
                serializedName = metadata.get(SonJName.class).value();

            builder.appendIndent().appendJsonString(serializedName).append(JsonMainSymbol.COLON);

            toJson(value, builder, visited, false);

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

    private void addAnnotations(Object source) {
        Map<String, FieldMetadata> classFieldMetadata = new HashMap<>();
        Class<?> currentClass = source.getClass();

        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (!classFieldMetadata.containsKey(field.getName())) {
                    String fieldName = field.getName();
                    Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();
                    List<AccessFlag> accessFlags = field.accessFlags().stream().toList();
                    for (Annotation annotation : field.getAnnotations()) annotations.put(annotation.annotationType(), annotation);
                    classFieldMetadata.put(fieldName, new FieldMetadata(fieldName, annotations, accessFlags));
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        classFieldsAnnotations.put(source.getClass(), classFieldMetadata);
    }

    private boolean isExposeForSerialization(FieldMetadata metadata) {
        if (!metadata.has(Expose.class)) return false;
        if (!metadata.get(Expose.class).serialize()) return false;
        return true;
    }

    private boolean isExposeForDeserialization(FieldMetadata metadata) {
        if (!metadata.has(Expose.class)) return false;
        if (!metadata.get(Expose.class).deserialize()) return false;
        return true;
    }
}
