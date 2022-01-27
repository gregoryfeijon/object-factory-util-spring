package br.com.gregoryfeijon.objectfactoryutilspring;

import br.com.gregoryfeijon.objectfactoryutilspring.model.Bar;
import br.com.gregoryfeijon.objectfactoryutilspring.model.Foo;
import br.com.gregoryfeijon.objectfactoryutilspring.exception.util.GsonUtil;
import br.com.gregoryfeijon.objectfactoryutilspring.exception.util.ObjectFactoryUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import java.util.*;
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
            copyDifferentType(bars);
        };
    }

    private static List<Bar> createBars(List<String> barNames) {
        return IntStream.range(1, barNames.size() + 1).boxed()
                .collect(Collectors.toMap(Function.identity(), i -> barNames.get(i - 1))).entrySet().stream()
                .map(i -> new Bar(i.getKey(), i.getValue(), "SAME NAME")).collect(Collectors.toList());
    }

    private static void copyListsExample(List<Bar> bars) {
        List<Bar> copyBars = ObjectFactoryUtil.copyAllObjectsFromCollection(bars);
        compareObjects(bars, copyBars, "It's the same collection!");
        Set<Bar> copyFromCopyBars = ObjectFactoryUtil.copyAllObjectsFromCollection(copyBars, HashSet::new);
        compareObjects(copyBars, copyFromCopyBars, "It's the same collection!");
        List<Foo> foosFromBars = ObjectFactoryUtil.copyAllObjectsFromCollection(copyBars, ArrayList::new, Foo.class);
        compareObjects(copyBars, copyFromCopyBars, "It's the same collection!");
        Assert.isTrue(foosFromBars.get(0).getSameNameAttribute().equals(copyBars.get(0).getSameNameAttribute()), "Its not equal!");
        System.out.println(GsonUtil.getGson().toJson(foosFromBars));
        System.out.println(GsonUtil.getGson().toJson(copyBars));
    }

    private static void copyObjectsExample(List<Bar> bars) {
        Bar bar = ObjectFactoryUtil.createFromObject(bars.get(0));
        Foo foo1 = new Foo(1, "this is a foo name", "SAME NAME", bar, bars);
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

    private void copyDifferentType(List<Bar> bars) {
        Foo foo = ObjectFactoryUtil.createFromObject(bars.get(0), Foo.class);
        compareObjects(bars.get(0), foo);
        Assert.isTrue(bars.get(0).getSameNameAttribute().equals(foo.getSameNameAttribute()), "It's not equal!");
        List<Foo> foos = ObjectFactoryUtil.copyAllObjectsFromCollection(bars, Foo.class);
        compareObjects(bars, foos);
        for (Integer i : IntStream.range(0, foos.size()).boxed().collect(Collectors.toList())) {
            compareObjects(bars.get(i), foos.get(i));
        }
        Assert.isTrue(foos.get(0).getSameNameAttribute().equals(bars.get(0).getSameNameAttribute()), "Its not equal!");
        System.out.println(GsonUtil.getGson().toJson(bars));
        System.out.println(GsonUtil.getGson().toJson(foos));
    }

    private static void compareObjects(Object object1, Object object2) {
        compareObjects(object1, object2, "It's the same object!");
    }

    private static void compareObjects(Object object1, Object object2, String message) {
        Assert.isTrue(System.identityHashCode(object1) != System.identityHashCode(object2), message);
    }
}
