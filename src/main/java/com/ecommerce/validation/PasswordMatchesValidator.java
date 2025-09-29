package com.ecommerce.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    private String passwordFieldName;
    private String confirmPasswordFieldName;

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        this.passwordFieldName = constraintAnnotation.passwordFieldName();
        this.confirmPasswordFieldName = constraintAnnotation.confirmPasswordFieldName();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object passwordValue = new BeanWrapperImpl(value).getPropertyValue(passwordFieldName);
        Object confirmPasswordValue = new BeanWrapperImpl(value).getPropertyValue(confirmPasswordFieldName);

        boolean isValid = passwordValue != null && passwordValue.equals(confirmPasswordValue);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(confirmPasswordFieldName).addConstraintViolation();
        }
        return isValid;
    }
}
