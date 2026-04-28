package dev.m4nd3l.sonj.annotations;

import java.lang.annotation.*;

/**
 * <p>Indicates that a field should be included in the JSON serialization or deserialization process.</p>
 * <p>This is particularly useful when the {@code SonJ} instance is configured to
 * {@code excludeFieldsWithoutExposeAnnotation}. It also acts as an override to include
 * private fields even if {@code ignorePrivateFields} is enabled.</p>
 * <p><b>Example:</b></p>
 * <pre>{@code
 * public class User {
 *     @Expose private String username;
 *     private String password;
 * }
 * }</pre>
 * <p>username will be always serialized</p>
 * <p>password will be ignored if excludeFieldsWithoutExpose is true</p>
 * @see dev.m4nd3l.sonj.SonJBuilder#excludeFieldsWithoutExposeAnnotation()
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Expose {
    /**
     * @return {@code true} if the field should be serialized to JSON. Defaults to {@code true}.
     */
    boolean serialize() default true;

    /**
     * @return {@code true} if the field should be populated during deserialization. Defaults to {@code true}.
     */
    boolean deserialize() default true;
}