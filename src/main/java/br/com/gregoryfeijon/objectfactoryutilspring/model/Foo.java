package br.com.gregoryfeijon.objectfactoryutilspring.model;

import br.com.gregoryfeijon.objectfactoryutilspring.annotation.ObjectConstructor;
import br.com.gregoryfeijon.objectfactoryutilspring.exception.util.ObjectFactoryUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ObjectConstructor(exclude = { "fooId" })
public class Foo implements Serializable {

	private static final long serialVersionUID = -157292002192952506L;
	
	private long fooId;
	private String fooName;
	private String sameNameAttribute;
	private Bar bar;
	private List<Bar> bars;

	public Foo(Foo foo) {
		ObjectFactoryUtil.createFromObject(foo, this);
	}
	
	public Foo(long fooId, String fooName, String sameNameAttribute, Bar bar, List<Bar> bars) {
		this.fooId = fooId;
		this.fooName = fooName;
		this.sameNameAttribute = sameNameAttribute;
		this.bar = bar;
		this.bars = bars;
	}
}
