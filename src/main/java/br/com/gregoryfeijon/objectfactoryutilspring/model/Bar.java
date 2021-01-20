package br.com.gregoryfeijon.objectfactoryutilspring.model;

import br.com.gregoryfeijon.objectfactoryutilspring.annotation.ObjectConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ObjectConstructor
@NoArgsConstructor
public class Bar {

	private long barId;
	private String barName;

	public Bar(long barId, String barName) {
		this.barId = barId;
		this.barName = barName;
	}
}
