package raica.pwmanager.validation.annotation;

import raica.pwmanager.validation.validator.OnlyEnglishAndNumbersValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自己做的驗證@，只允許英文和數字。
 */
@Constraint(validatedBy = OnlyEnglishAndNumbersValidator.class) //與哪一個Validator綁定
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE}) //此@用在什麼地方
@Retention(RetentionPolicy.RUNTIME) //此@的生命週期作用於哪個階段
public @interface OnlyEnglishAndNumbers {

    String message() default "Only english and numbers.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
