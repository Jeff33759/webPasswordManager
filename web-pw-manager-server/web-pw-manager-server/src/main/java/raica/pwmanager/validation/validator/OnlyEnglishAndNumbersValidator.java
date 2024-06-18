package raica.pwmanager.validation.validator;

import raica.pwmanager.validation.annotation.OnlyEnglishAndNumbers;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 自定義的驗證器，只允許英文和數字。
 * 不需要特別註冊為Spring Bean，Spring Validation框架會自動掃描所有ConstraintValidator的implementation。
 */
public class OnlyEnglishAndNumbersValidator implements ConstraintValidator<OnlyEnglishAndNumbers, String> {

    private static final String PATTERN = "^[A-Za-z0-9]+$";


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null視為合法。空或者null的驗證交給別的@去做
        }

        return value.matches(PATTERN);
    }

}
