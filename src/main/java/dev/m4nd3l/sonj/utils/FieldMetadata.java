package dev.m4nd3l.sonj.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessFlag;
import java.util.List;
import java.util.Map;

public record FieldMetadata(String name, Map<Class<? extends Annotation>, Annotation> annotations, List<AccessFlag> accessFlags) {
    public <T extends Annotation> T get(Class<T> clazz) {
        Annotation annotation = annotations.get(clazz);
        return (annotation == null) ? null : clazz.cast(annotation);
    }

    public boolean has(Class<? extends Annotation> clazz) { return annotations.containsKey(clazz); }

    public boolean isPrivate() { return accessFlags.contains(AccessFlag.PRIVATE); }
    public boolean isTransient() { return accessFlags.contains(AccessFlag.TRANSIENT); }
}
