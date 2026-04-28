package dev.m4nd3l.sonj.annotations;

import dev.m4nd3l.sonj.SonJBuilder;

import java.lang.annotation.*;

/**
 * <p>The opposite of {@link Expose}. Explicitly prevents a field from being
 * processed by the SonJ engine.</p>
 * <p>Use this for sensitive data (like tokens or internal IDs) that should
 * never leave the application.</p>
 * <p><b>Example:</b></p>
 * <pre>{@code
 * public class Session {
 *     private String sessionId;
 *     @Hide private String internalHash;
 * }
 * }</pre>
 * <p>internalHash will never appear in JSON unless ignoreHideAnnotation is true</p>
 * @see SonJBuilder#ignoreHideAnnotation()
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Hide {
    /**
     * @return {@code false} to specifically block serialization. Defaults to {@code true}.
     */
    boolean serialize() default true;

    /**
     * @return {@code false} to specifically block deserialization. Defaults to {@code true}.
     */
    boolean deserialize() default true;
}