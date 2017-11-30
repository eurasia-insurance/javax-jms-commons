package ejb.resources.callable.validation;

import java.util.Properties;

import javax.ejb.MessageDriven;

import tech.lapsa.javax.jms.service.JmsCallableServiceDrivenBean;
import tech.lapsa.javax.jms.service.JmsSkipValidation;

@MessageDriven(mappedName = CallableValidationDestination.SKIPPED_VALIDATION)
@JmsSkipValidation
public class CallableValidationDrivenBean_SkipValidation
	extends JmsCallableServiceDrivenBean<CallableValidationEntity, CallableValidationResult> {

    public CallableValidationDrivenBean_SkipValidation() {
	super(CallableValidationEntity.class);
    }

    @Override
    public CallableValidationResult calling(CallableValidationEntity callableValidationEntity, Properties properties) {
	return new CallableValidationResult(callableValidationEntity);
    }

}
