package de.gsi.chart.ui.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import de.gsi.chart.utils.FXUtils;

/**
 * Simple JUnit 5 extension to ensure that {@code @Test} statements are executed in the JavaFX UI thread.
 * This is (strictly) necessary when testing setter and/or getter methods of JavaFX classes (ie. Node derived, properties etc).
 * <p>
 * Use the
 * <ul>
 * <li> {@code @ExtendWith(avaFxInterceptor.class) } if all @Test, or
 * <li> {@code @ExtendWith(SelectiveJavaFxInterceptor.class) } if only @Test + @TestFx annotated tests
 * </ul>
 * should be executed within the JavaFX UI thread.
 *
 * @author rstein
 */
public class JavaFXInterceptorUtils {
    /**
	 * Simple JUnit 5 extension to ensure that {@code @Test} statements are executed in the JavaFX UI thread.
	 * This is (strictly) necessary when testing setter and/or getter methods of JavaFX classes (ie. Node derived, properties etc).
	 * <p>
	 * Example usage:
	 * <pre><code>
	 * @ExtendWith(ApplicationExtension.class)
	 * @ExtendWith(JavaFxInterceptor.class)
	 * public class SquareButtonTest {
	 *     @Start
	 *     public void start(Stage stage) {
	 *         // usual FX initialisation
	 *         // ...
	 *     }
	 *
	 *    @TestFx // note: this is equivalent to {@code @Test} when using {@code @ExtendWith(JavaFxInterceptor.class)}
	 *    public void testJavaFxThreadSafety() {
	 *        // verifies that this test is indeed executed in the JavaFX thread
	 *        assertTrue(Platform.isFxApplicationThread());
	 *
	 *        // perform the regular JavaFX thread safe assertion tests
	 *        // ...
	 *    }
	 *
	 *    @Test // also executed in JavaFX thread, for different behaviour use:  {@code @ExtendWith(SelectiveJavaFxInterceptor.class)}
	 *    public void testNonJavaFx() {
	 *        // verifies that this test is also executed in the JavaFX thread
	 *        assertTrue(Platform.isFxApplicationThread());
	 *
	 *        // perform regular assertion tests within the JavaFX thread
	 *        // ...
	 *    }
	 * }
	 *
	 * </code></pre>
	 *
	 * @author rstein
	 */
    public static class JavaFxInterceptor implements InvocationInterceptor {
        @Override
        public void interceptTestMethod(final Invocation<Void> invocation,
                final ReflectiveInvocationContext<Method> invocationContext,
                final ExtensionContext extensionContext) throws Throwable {
            final AtomicReference<Throwable> throwable = new AtomicReference<>();

            // N.B. explicit run and wait since the test should only continue
            // if the previous JavaFX access as been finished.
            FXUtils.runAndWait(() -> {
                try {
                    // executes function after @Test
                    invocation.proceed();
                } catch (final Throwable t) {
                    throwable.set(t);
                }
            });
            final Throwable t = throwable.get();
            if (t != null) {
                throw t;
            }
        }
    }

    /**
	 * Simple JUnit 5 extension to ensure that {@code @Test} statements are executed in the JavaFX UI thread.
	 * This is (strictly) necessary when testing setter and/or getter methods of JavaFX classes (ie. Node derived, properties etc).
	 * <p>
	 * Example usage:
	 * <pre><code>
	 * @ExtendWith(ApplicationExtension.class)
	 * @ExtendWith(SelectiveJavaFxInterceptor.class)
	 * public class SquareButtonTest {
	 *     @Start
	 *     public void start(Stage stage) {
	 *         // usual FX initialisation
	 *         // ...
	 *     }
	 *
	 *    @TestFx // forces execution in JavaFX thread
	 *    public void testJavaFxThreadSafety() {
	 *        // verifies that this test is indeed executed in the JavaFX thread
	 *        assertTrue(Platform.isFxApplicationThread());
	 *
	 *        // perform the regular JavaFX thread safe assertion tests
	 *        // ...
	 *    }
	 *
	 *    @Test // explicitly not executed in JavaFX thread; for different behaviour use:  {@code @ExtendWith(JavaFxInterceptor.class)}
	 *    public void testNonJavaFx() {
	 *        // verifies that this test is not executed within the JavaFX thread
	 *        assertFalse(Platform.isFxApplicationThread());
	 *
	 *        // perform the regular non-JavaFX thread-related assertion tests
	 *        // ...
	 *    }
	 * }
	 *
	 * </code></pre>
	 *
	 * @author rstein
	 */
    public static class SelectiveJavaFxInterceptor implements InvocationInterceptor {
        @Override
        public void interceptTestMethod(final Invocation<Void> invocation,
                final ReflectiveInvocationContext<Method> invocationContext,
                final ExtensionContext extensionContext) throws Throwable {
            final AtomicReference<Throwable> throwable = new AtomicReference<>();

            boolean isFxAnnotation = false;
            final Optional<AnnotatedElement> element = extensionContext.getElement();
            if (element.isPresent()) {
                for (final Annotation annotation : element.get().getAnnotations()) {
                    if (annotation.annotationType().equals(TestFx.class)) {
                        isFxAnnotation = true;
                    }
                }
            }

            final Runnable testToBeExecuted = () -> {
                try {
                    // executes function after @Test
                    invocation.proceed();
                } catch (final Throwable t) {
                    throwable.set(t);
                }
            };

            // N.B. explicit run and wait since the test should only continue
            // if the previous JavaFX access as been finished.
            if (isFxAnnotation) {
                FXUtils.runAndWait(testToBeExecuted);
            } else {
                testToBeExecuted.run();
            }
            final Throwable t = throwable.get();
            if (t != null) {
                throw t;
            }
        }
    }
}
