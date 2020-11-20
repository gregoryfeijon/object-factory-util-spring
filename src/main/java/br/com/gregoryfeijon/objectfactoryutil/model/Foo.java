package br.com.gregoryfeijon.objectfactoryutil.model;

import java.util.List;

import br.com.gregoryfeijon.objectfactoryutil.annotation.ObjectConstructor;
import br.com.gregoryfeijon.objectfactoryutil.util.ObjectFactoryUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ObjectConstructor
public class Foo {

	private long fooId;
	private String fooName;
	private Bar bar;
	private List<Bar> bars;

	public Foo() {}

	public Foo(Foo foo) {
		ObjectFactoryUtil.createFromObject(foo, this);
	}
}
