package br.com.gregoryfeijon.objectfactoryutil.model;

import br.com.gregoryfeijon.objectfactoryutil.annotation.ObjectConstructor;
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
