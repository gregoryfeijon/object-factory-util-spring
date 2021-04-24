package br.com.gregoryfeijon.objectfactoryutilspring.model;

import java.io.Serializable;

import br.com.gregoryfeijon.objectfactoryutilspring.annotation.ObjectConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ObjectConstructor
public class Bar implements Serializable {

	private static final long serialVersionUID = 4203736872250042337L;
	
	private long barId;
	private String sameNameAttribute;
	private String barName;

	public Bar(long barId, String barName, String sameNameAttribute) {
		this.barId = barId;
		this.barName = barName;
		this.sameNameAttribute = sameNameAttribute;
	}
}
