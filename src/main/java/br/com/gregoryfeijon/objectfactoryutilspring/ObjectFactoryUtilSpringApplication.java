package br.com.gregoryfeijon.objectfactoryutilspring;

import br.com.gregoryfeijon.objectfactoryutilspring.model.Bar;
import br.com.gregoryfeijon.objectfactoryutilspring.model.Foo;
import br.com.gregoryfeijon.objectfactoryutilspring.util.GsonUtil;
import br.com.gregoryfeijon.objectfactoryutilspring.util.ObjectFactoryUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
public class ObjectFactoryUtilSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObjectFactoryUtilSpringApplication.class, args);
	}

	@Bean
	CommandLineRunner runExample() {
		return args -> {
			List<Bar> bars = createBars(Arrays.asList("First bar name", "Second bar name", "Third bar name"));
			copyListsExample(bars);
			copyObjectsExample(bars);
		};
	}

	private static List<Bar> createBars(List<String> barNames) {
		return IntStream.range(1, barNames.size() + 1).boxed()
				.collect(Collectors.toMap(Function.identity(), i -> barNames.get(i - 1))).entrySet().stream()
				.map(i -> new Bar(i.getKey(), i.getValue())).collect(Collectors.toList());
	}

	private static void compareObjects(Object object1, Object object2) {
		compareObjects(object1, object2, "It's the same object!");
	}

	private static void compareObjects(Object object1, Object object2, String message) {
		Assert.isTrue(System.identityHashCode(object1) != System.identityHashCode(object2), message);
	}

	private static void copyListsExample(List<Bar> bars) {
		List<Bar> copyBars = ObjectFactoryUtil.copyAllObjectsFromCollection(bars);
		compareObjects(bars, copyBars, "It's the same collection!");
		Set<Bar> copyFromCopyBars = ObjectFactoryUtil.copyAllObjectsFromCollection(copyBars, HashSet::new);
		compareObjects(copyBars, copyFromCopyBars, "It's the same collection!");
	}

	private static void copyObjectsExample(List<Bar> bars) {
		Bar bar = (Bar) ObjectFactoryUtil.createFromObject(bars.get(0));
		Foo foo1 = new Foo(1, "this is a foo name", bar, bars);
		compareObjects(bar, bars.get(0));
		Foo foo2 = new Foo(foo1);
		compareObjects(foo1, foo2);
		foo2.setFooName(
				"this is a new Foo name. This foo hasn't copy the object ID! See Foo @ObjectConstructor annotation in " +
						"Foo model. Also, has completely new object references!");
		foo2.getBar().setBarName("this is a new BarName");
		compareObjects(foo1.getBars(), foo2.getBars(), "It's the same collection!");
		for (Integer i : IntStream.range(0, foo1.getBars().size()).boxed().collect(Collectors.toList())) {
			compareObjects(foo1.getBars().get(i), foo2.getBars().get(i));
		}
		System.out.println(GsonUtil.getGson().toJson(foo1));
		System.out.println(GsonUtil.getGson().toJson(foo2));
	}
}
