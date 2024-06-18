package raica.pwmanager.validation.validator;

import raica.pwmanager.consts.MFAType;
import raica.pwmanager.validation.annotation.ValidMFATypeNumber;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 自定義的驗證器，只允許匹配MFAType的數字。
 * 不需要特別註冊為Spring Bean，Spring Validation框架會自動掃描所有ConstraintValidator的implementation。
 */
public class MFATypeNumberValidator implements ConstraintValidator<ValidMFATypeNumber, Integer> {

    @Override
    public boolean isValid(Integer typeNum, ConstraintValidatorContext context) {
        if (typeNum == null) {
            return false;
        }

        return MFAType.fromTypeNum(typeNum) != MFAType.UNKNOWN;
    }

}
