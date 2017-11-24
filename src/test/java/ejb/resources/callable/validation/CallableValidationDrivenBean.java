package ejb.resources.callable.validation;

import java.util.Properties;

import javax.ejb.MessageDriven;

import tech.lapsa.javax.jms.ObjectFunctionDrivenBean;

@MessageDriven(mappedName = CallableValidationDestination.JNDI_NAME)
public class CallableValidationDrivenBean extends ObjectFunctionDrivenBean<CallableValidationEntity, CallableValidationResult> {

    public CallableValidationDrivenBean() {
	super(CallableValidationEntity.class);
    }

    @Override
    protected CallableValidationResult apply(CallableValidationEntity callableValidationEntity, Properties properties) {
	return new CallableValidationResult(callableValidationEntity);
    }

}
