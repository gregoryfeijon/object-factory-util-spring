package br.com.gregoryfeijon.objectfactoryutilspring.model;

import br.com.gregoryfeijon.objectfactoryutilspring.annotation.ObjectConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ObjectConstructor
public class Bar {

	private long barId;
	private String barName;

	public Bar(long barId, String barName) {
		this.barId = barId;
		this.barName = barName;
	}
}
