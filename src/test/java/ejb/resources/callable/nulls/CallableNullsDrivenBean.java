package ejb.resources.callable.nulls;

import java.util.Properties;

import javax.ejb.MessageDriven;

import tech.lapsa.java.commons.function.MyObjects;
import tech.lapsa.javax.jms.CallableServiceDrivenBean;

@MessageDriven(mappedName = CallableNullsDestination.GENERAL)
public class CallableNullsDrivenBean extends CallableServiceDrivenBean<CallableNullsEntity, CallableNullsResult> {

    public CallableNullsDrivenBean() {
	super(CallableNullsEntity.class);
    }

    @Override
    public CallableNullsResult calling(CallableNullsEntity entity, Properties properties) {
	MyObjects.requireNull(entity);
	return null;
    }
}
