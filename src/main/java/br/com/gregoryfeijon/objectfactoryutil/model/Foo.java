package br.com.gregoryfeijon.objectfactoryutil.model;

import java.util.List;

import br.com.gregoryfeijon.objectfactoryutil.annotation.ObjectConstructor;
import br.com.gregoryfeijon.objectfactoryutil.util.ObjectFactoryUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ObjectConstructor(exclude = { "fooId" })
@NoArgsConstructor
public class Foo {

	private long fooId;
	private String fooName;
	private Bar bar;
	private List<Bar> bars;

	public Foo(Foo foo) {
		ObjectFactoryUtil.createFromObject(foo, this);
	}
	
	public Foo(long fooId, String fooName, Bar bar, List<Bar> bars) {
		this.fooId = fooId;
		this.fooName = fooName;
		this.bar = bar;
		this.bars = bars;
	}
}
