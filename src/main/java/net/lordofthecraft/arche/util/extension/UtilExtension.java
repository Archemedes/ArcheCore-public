package net.lordofthecraft.arche.util.extension;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UtilExtension {
	private UtilExtension() {}
	
	public static <T> T or(T object, T other) {
		if(object == null) return other;
		else return object;
	}
	
	public static <T> void ifElse(Optional<T> optional, Consumer<T> theIf, Runnable theElse){
		if(optional.isPresent()) {
			theIf.accept(optional.get());
		} else {
			theElse.run();
		}
	}
	
	public static <T> Stream<T> stream(T[] array){
		return Stream.of(array);
	}
	
}
