package br.com.gregoryfeijon.objectfactoryutil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

import br.com.gregoryfeijon.objectfactoryutil.model.Bar;
import br.com.gregoryfeijon.objectfactoryutil.model.Foo;
import br.com.gregoryfeijon.objectfactoryutil.util.GsonUtil;
import br.com.gregoryfeijon.objectfactoryutil.util.ObjectFactoryUtil;

@SpringBootApplication
public class ObjectFactoryUtilApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObjectFactoryUtilApplication.class, args);
		List<Bar> bars = createBars(Arrays.asList("First bar name", "Second bar name", "Third bar name"));
		Bar bar = (Bar) ObjectFactoryUtil.createFromObject(bars.get(0));
		Foo foo1 = new Foo(1, "this is a foo name", bar, bars);
		Assert.isTrue(System.identityHashCode(bar) != System.identityHashCode(bars.get(0)), "It's the same object!");
		Foo foo2 = new Foo(foo1);
		Assert.isTrue(System.identityHashCode(foo1) != System.identityHashCode(foo2), "It's the same object!");
		foo2.setFooName("this is a new Foo name. This foo hasn't copy the ID! See Foo @ObjectConstructor annotation.");
		foo2.getBar().setBarName("this is a new BarName");
		Assert.isTrue(System.identityHashCode(foo1.getBars()) != System.identityHashCode(foo2.getBars()),
				"It's the same collection!");
		for (Integer i : IntStream.range(0, foo1.getBars().size()).boxed().collect(Collectors.toList())) {
			Assert.isTrue(
					System.identityHashCode(foo1.getBars().get(i)) != System.identityHashCode(foo2.getBars().get(i)),
					"It's the same object!");
		}
		System.out.println(GsonUtil.getGson().toJson(foo1));
		System.out.println(GsonUtil.getGson().toJson(foo2));
	}

	private static List<Bar> createBars(List<String> barNames) {
		return IntStream.range(1, barNames.size() + 1).boxed()
				.collect(Collectors.toMap(Function.identity(), i -> barNames.get(i - 1))).entrySet().stream()
				.map(i -> new Bar(i.getKey(), i.getValue())).collect(Collectors.toList());
	}
}
