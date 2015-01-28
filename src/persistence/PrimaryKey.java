package persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify the primary key of a Java Bean.
 * The names of the primary key properties are passed as a String.
 * Here is a simple example:
 *     PrimaryKey("id")
 *     public class Customer {
 *         private int    id;
 *     }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PrimaryKey {
	String value();
}
