package dev.m4nd3l.sonj;

import dev.m4nd3l.sonj.annotations.Expose;
import dev.m4nd3l.sonj.exceptions.CircularReferenceException;
import dev.m4nd3l.sonj.settings.NumberDecimals;
import dev.m4nd3l.sonj.settings.style.Style;
import dev.m4nd3l.sonj.utils.FieldMetadata;
import dev.m4nd3l.sonj.utils.Version;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SonJ {
    public static Version VERSION = new Version(1, 0, 0);
    public static SonJ defaultSonJ = new SonJBuilder()
            .setAmericanDateFormat()
            .create();

    protected boolean
            ignoreSerializedNameAnnotation,
            ignoreHideAnnotation,
            makeNullIfCircularReference,
            acceptTransientKeyword,
            ignorePrivateFields,
            ignoreStaticFields,
            excludeFieldsWithoutExposeAnnotation,
            serializeNulls;
    protected Style style;
    protected SimpleDateFormat dateFormat;
    protected NumberDecimals decimals;
    protected Map<Class<?>, Map<String, FieldMetadata>> classFieldsAnnotations;

    public SonJ(boolean ignoreSerializedNameAnnotation, boolean ignoreHideAnnotation, boolean makeNullIfCircularReference,
                boolean acceptTransientFields, boolean ignorePrivateFields, boolean ignoreStaticFields,
                boolean excludeFieldsWithoutExposeAnnotation,
                boolean serializeNulls, Style style, SimpleDateFormat dateFormat, NumberDecimals decimals) {
        this.ignoreSerializedNameAnnotation = ignoreSerializedNameAnnotation;
        this.ignoreHideAnnotation = ignoreHideAnnotation;
        this.makeNullIfCircularReference = makeNullIfCircularReference;
        this.acceptTransientKeyword = acceptTransientFields;
        this.ignorePrivateFields = ignorePrivateFields;
        this.ignoreStaticFields = ignoreStaticFields;
        this.excludeFieldsWithoutExposeAnnotation = excludeFieldsWithoutExposeAnnotation;
        this.serializeNulls = serializeNulls;
        this.style = style;
        this.dateFormat = dateFormat;
        this.decimals = decimals;

        this.classFieldsAnnotations = new HashMap<>();
    }

    public static SonJBuilder builder() { return new SonJBuilder(); }

    public String serialize(Object source) throws CircularReferenceException, IOException {
        if (source == null) return "null";
        JsonSerializer serializer = new JsonSerializer(source, this);
        serializer.serialize();
        return serializer.toString();
    }

    public File serialize(Object source, File file)
            throws CircularReferenceException, IOException {
        JsonSerializer serializer = new JsonSerializer(source, file, this);
        serializer.serialize();
        return file;
    }

    public <T> T deserialize(String source, Class<T> target)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(target, source, this);
        return deserializer.deserialize();
    }

    public <T> T deserialize(File source, Class<T> target)
            throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(target, source, this);
        return deserializer.deserialize();
    }

    protected void addAnnotations(Class<?> clazz) {
        if (classFieldsAnnotations.containsKey(clazz)) return;
        Map<String, FieldMetadata> classFieldMetadata = new HashMap<>();
        Class<?> currentClass = clazz;

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
        classFieldsAnnotations.put(clazz, classFieldMetadata);
    }
}
