package br.com.gregoryfeijon.objectfactoryutilspring.model;

import br.com.gregoryfeijon.objectfactoryutilspring.annotation.ObjectConstructor;
import br.com.gregoryfeijon.objectfactoryutilspring.util.ObjectFactoryUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
