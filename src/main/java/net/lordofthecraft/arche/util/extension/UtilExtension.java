package net.lordofthecraft.arche.util.extension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

public class UtilExtension {
	private UtilExtension() {}
	
	public static <T> T or(T object, T other) {
		if(object == null) return other;
		else return object;
	}
	
	public static <T> Optional<T> optional(T object){
		return Optional.ofNullable(object);
	}
 	
	public static <T, E extends T> void ifElse(Optional<E> optional, Consumer<E> theIf, Runnable theElse){
		if(optional.isPresent()) {
			theIf.accept(optional.get());
		} else {
			theElse.run();
		}
	}
	
	public static <T> Stream<T> stream(T[] array){
		return Stream.of(array);
	}
	
	public static boolean eq(Object o1, Object o2) {
		return ObjectUtils.equals(o1, o2);
	}
	
	public static String join(String separator, Collection<?> parts) {
		return join(separator, parts.toArray());
	}
	
	public static String join(String separator, Object... parts) {
		return StringUtils.join(parts, separator);
	}
	
	public static <T> T last(List<T> list) {
		return list.get(list.size() - 1);
	}
	
	public static <T> T last(T[] array) {
		return array[array.length - 1];
	}
	
}
