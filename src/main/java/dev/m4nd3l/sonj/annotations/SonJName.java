package dev.m4nd3l.sonj.annotations;

import dev.m4nd3l.sonj.SonJBuilder;

import java.lang.annotation.*;

/**
 * <p>Configures a custom name for a field when it is serialized or deserialized.</p>
 * <p>This allows your Java field names to remain camelCase while your JSON keys follow
 * snake_case or any other naming convention.</p>
 * <p><b>Example:</b></p>
 * <pre>{@code
 * public class Product {
 *     @SonJName("product_id")
 *     private int id;
 * }
 * }</pre>
 * <p>Serializes to: {"product_id": ...} if ignoreSerializedNameAnnotation is false, otherwise it serializes to: {"id": ...}</p>
 * @see SonJBuilder#ignoreSerializedNameAnnotation()
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SonJName {
    /**
     * @return The primary name to use for this field in JSON.
     */
    String value();

    /**
     * <p>Provides alternative names that the deserializer should look for if the
     * primary {@link #value()} is not found in the JSON source.</p>
     * <b>Example:</b>
     * <pre>{@code
     * @SonJName(value = "name", alternative = {"user_name", "full_name"})
     *     private String name;
     * }</pre>
     * @return An array of fallback names for deserialization.
     */
    String[] alternative() default {};
}