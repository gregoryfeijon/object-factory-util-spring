package br.com.gregoryfeijon.objectfactoryutilspring.model;

import java.util.List;

import br.com.gregoryfeijon.objectfactoryutilspring.annotation.ObjectConstructor;
import br.com.gregoryfeijon.objectfactoryutilspring.util.ObjectFactoryUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ObjectConstructor(exclude = { "fooId" })
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
