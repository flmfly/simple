package simple.core.annotation;

public interface AnnotationHandler<A, V> {
	V handle(A a);
}
