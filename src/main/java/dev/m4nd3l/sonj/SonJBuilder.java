package dev.m4nd3l.sonj;

import dev.m4nd3l.sonj.settings.NumberDecimals;
import dev.m4nd3l.sonj.settings.style.Compact;
import dev.m4nd3l.sonj.settings.style.Pretty;
import dev.m4nd3l.sonj.settings.style.Style;
import java.text.SimpleDateFormat;

/**
 * <p>Builder for {@link SonJ} instances. This class provides a fluent API to
 * configure how Java objects are converted into JSON strings.</p>
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * SonJ sonj = new SonJBuilder()
 *     .setPrettyStyle()
 *     .ignoreNullFields()
 *     .create();
 * }</pre>
 * @author m4nd3l
 */
public class SonJBuilder {
    private boolean ignoreSerializedNameAnnotation = false;
    private boolean ignoreHideAnnotation = false;
    private boolean acceptTransientKeyword = true;
    private boolean excludeFieldsWithoutExposeAnnotation = false;
    private boolean makeNullIfCircularReference = false;
    private boolean ignorePrivateFields = false;
    private boolean serializeNulls = false;
    private Style style = new Compact();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private NumberDecimals decimals = NumberDecimals.DOUBLE;

    /**
     * Sets a custom date format for {@link java.util.Date} fields.
     * @param dateFormat A {@link SimpleDateFormat} compatible string.
     * @return This builder for chaining.
     */
    public SonJBuilder setDateFormat(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
        return this;
    }

    /**
     * <p>Sets the date format to <b>MM-dd-yyyy</b>.</p>
     * <b>Example:</b> {@code new Date()} becomes {@code "04-27-2026"}
     * @return This builder for chaining.
     */
    public SonJBuilder setAmericanDateFormat() {
        this.dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        return this;
    }

    /**
     * <p>Sets the date format to <b>dd-MM-yyyy</b>.</p>
     * <b>Example:</b> {@code new Date()} becomes {@code "27-04-2026"}
     * @return This builder for chaining.
     */
    public SonJBuilder setEuropeanDateFormat() {
        this.dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return this;
    }

    /**
     * <p>Configures the output to use a custom style, created by you or other developers.</p>
     * @param custom A {@link Style} style instance, to manage how the final json will look.
     * @return This builder for chaining.
     */
    public SonJBuilder setStyle(Style custom) {
        this.style = custom;
        return this;
    }

    /**
     * <p>Configures the output to use a "Pretty" style with indentation.</p>
     * <b>Example:</b>
     * <pre>{@code
     * {
     *     "id": 1,
     *     "name": "John"
     * }
     * }</pre>
     * @return This builder for chaining.
     */
    public SonJBuilder setPrettyStyle() {
        this.style = new Pretty();
        return this;
    }

    /**
     * <p>Configures the output to be a single-line string with no spaces.</p>
     * <b>Example:</b> {@code {"id":1,"name":"John"}}
     * @return This builder for chaining.
     */
    public SonJBuilder setCompactStyle() {
        this.style = new Compact();
        return this;
    }

    /**
     * <p>Disables the {@code @SonJName} annotation. Fields will use their Java variable names.</p>
     * <b>Example:</b>
     * <pre>{@code
     * @SonJName("user_id") int id = 10;
     * // Default:  {"user_id": 10}
     * // Enabled:  {"id": 10}
     * }</pre>
     * @return This builder for chaining.
     */
    public SonJBuilder ignoreSerializedNameAnnotation() {
        this.ignoreSerializedNameAnnotation = true;
        return this;
    }

    /**
     * <p>Only fields explicitly marked with {@code @Expose} will be included in the JSON.</p>
     * <b>Example:</b>
     * <pre>{@code
     * @Expose String name = "Alex";
     * String secret = "1234";
     * // Result: {"name": "Alex"}
     * }</pre>
     * @return This builder for chaining.
     */
    public SonJBuilder excludeFieldsWithoutExposeAnnotation() {
        this.excludeFieldsWithoutExposeAnnotation = true;
        return this;
    }

    /**
     * <p>Excludes {@code private} fields from serialization unless they have an {@code @Expose} annotation.</p>
     * <b>Example:</b>
     * <pre>{@code
     * private String email = "a@b.com";         // Ignored
     * @Expose private String city = "Rome";     // Included
     * public String name = "Mario";             // Included
     * }</pre>
     * @return This builder for chaining.
     */
    public SonJBuilder ignorePrivateFields() {
        this.ignorePrivateFields = true;
        return this;
    }

    /**
     * <p>Serializes fields with {@code null} values.</p>
     * <b>Example:</b>
     * <pre>{@code
     * String name = "Luigi";
     * String middleName = null;
     * @Expose String lastName = null;
     * // Default: {"name": "Luigi", "lastName": null} -> In this case lastName // is serialized because of the {@code @Expose} annotation
     * // Enabled: {"name": "Luigi", "middleName": null, "lastName": null}
     * }</pre>
     * @return This builder for chaining.
     */
    public SonJBuilder serializeNullFields() {
        this.serializeNulls = true;
        return this;
    }

    /**
     * <p>Ignores the {@link dev.m4nd3l.sonj.annotations.Hide} annotation.</p>
     * <b>Example:</b>
     * <pre>{@code
     * @Hide String name = "Luigi";
     * String middleName = "Rossi";
     * // Default: {"middleName": "Rossi"}
     * // Enabled: {"name": "Luigi", "middleName": "Rossi"}
     * }</pre>
     * @return This builder for chaining.
     */
    public SonJBuilder ignoreHideAnnotation() {
        this.ignoreHideAnnotation = true;
        return this;
    }

    /**
     * <p>Ignores the {@code transient} keyword.</p>
     * <b>Example:</b>
     * <pre>{@code
     * transient String company_name = "Microsoft";
     * String manager_name = "John";
     * // Default: {"manager_name": "John"}
     * // Enabled: {"company_name": "Microsoft", "manager_name": "John"}
     * }</pre>
     * @return This builder for chaining.
     */
    public SonJBuilder ignoreTransientKeyword() {
        this.acceptTransientKeyword = false;
        return this;
    }

    /**
     * <p>When a circular reference is detected (an object pointing back to itself or one of its parents),
     * the engine will set that specific occurrence to null instead of throwing an exception.</p>
     * * <b>Example:</b>
     * <pre>{@code
     * class Node {
     *     @Expose String name;
     *     @Expose Node next;
     * }
     * Node root = new Node("A");
     * root.next = root; // Infinite loop!
     * // Result with makeNullIfCircularReference(true):
     * // {"name": "A", "next": null}
     * }</pre>
     * * @return This builder for chaining.
     */
    public SonJBuilder makeNullIfCircularReference() {
        this.makeNullIfCircularReference = true;
        return this;
    }

    /**
     * Creates the final {@link SonJ} instance.
     * @return A configured SonJ serializer.
     */
    public SonJ create() {
        return new SonJ(
                ignoreSerializedNameAnnotation,
                ignoreHideAnnotation,
                makeNullIfCircularReference,
                acceptTransientKeyword,
                ignorePrivateFields,
                excludeFieldsWithoutExposeAnnotation,
                serializeNulls,
                style,
                dateFormat,
                decimals
        );
    }
}