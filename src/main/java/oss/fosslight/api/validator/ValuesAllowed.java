package oss.fosslight.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValuesAllowed.Validator.class })
public @interface ValuesAllowed {

    String message() default "Input value='%s'. '%s' field value should be from list of %s";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String propName();

    String[] values();

    class Validator implements ConstraintValidator<ValuesAllowed, String> {
        private String propName;
        private String message;
        private List<String> allowable;

        @Override
        public void initialize(ValuesAllowed requiredIfChecked) {
            this.propName = requiredIfChecked.propName();
            this.message = requiredIfChecked.message();
            this.allowable = Arrays.asList(requiredIfChecked.values());
        }

        public boolean isValid(String value, ConstraintValidatorContext context) {
            Boolean valid = value == null || this.allowable.contains(value);

            if (!valid) {
                context.disableDefaultConstraintViolation();
//                context.buildConstraintViolationWithTemplate(message.concat(this.allowable.toString()))
//                        .addPropertyNode(this.propName).addConstraintViolation();

                context.buildConstraintViolationWithTemplate(
						message.formatted(value, this.propName, this.allowable.toString())).addConstraintViolation();


            }
            return valid;
        }
    }
}