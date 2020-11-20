package br.com.gregoryfeijon.objectfactoryutil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 09 de março de 2020
 * 
 * @author gregory.feijon
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ObjectConstructor {

	public String[] exclude() default {};
}
