package br.com.gregoryfeijon.objectfactoryutilspring.model;

import br.com.gregoryfeijon.objectfactoryutilspring.annotation.ObjectConstructor;
import br.com.gregoryfeijon.objectfactoryutilspring.model.enums.EnumBar;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ObjectConstructor
public class Bar implements Serializable {

	private static final long serialVersionUID = 4203736872250042337L;
	
	private long barId;
	private EnumBar enumBar;
	private String sameNameAttribute;
	private String barName;
}
