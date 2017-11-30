package tech.lapsa.javax.jms.client.beans;

import java.io.Serializable;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.jms.Destination;

import tech.lapsa.java.commons.function.MyExceptions;
import tech.lapsa.java.commons.naming.MyNaming;
import tech.lapsa.javax.cdi.utility.MyAnnotated;
import tech.lapsa.javax.jms.client.JmsCallableClient;
import tech.lapsa.javax.jms.client.JmsResultType;
import tech.lapsa.javax.jms.service.ejbBeans.JmsInternalClient;
import tech.lapsa.javax.jms.client.JmsConsumerClient;
import tech.lapsa.javax.jms.client.JmsDestination;
import tech.lapsa.javax.jms.client.JmsEventNotificatorClient;
import tech.lapsa.javax.jms.client.JmsEntityType;

@Dependent
public class JmsClientProducerCDIBean {

    @Inject
    private JmsInternalClient internalClient;

    @SuppressWarnings("unchecked")
    @Produces
    public <T extends Serializable, R extends Serializable> JmsCallableClient<T, R> produceCalable(final InjectionPoint ip) {
	final Destination destination = MyNaming.requireResource(ip.getAnnotated() //
		.getAnnotation(JmsDestination.class) //
		.value(), Destination.class);
	@SuppressWarnings("unused")
	final Class<? extends Serializable> entityClazz //
		= MyAnnotated.requireAnnotation(ip.getAnnotated(), JmsEntityType.class) //
			.value();
	final Class<? extends Serializable> wildcartResultClazz //
		= MyAnnotated.requireAnnotation(ip.getAnnotated(), JmsResultType.class) //
			.value();

	final Class<R> resultClazz;
	try {
	    resultClazz = (Class<R>) wildcartResultClazz;
	} catch (ClassCastException e) {
	    throw MyExceptions.illegalStateFormat("Types not safe");
	}

	return new JmsCallableImpl<>(resultClazz, internalClient, destination);
    }

    @Produces
    public <T extends Serializable> JmsConsumerClient<T> produceConsumer(final InjectionPoint ip) {
	final Destination destination = MyNaming.requireResource(ip.getAnnotated() //
		.getAnnotation(JmsDestination.class) //
		.value(), Destination.class);
	@SuppressWarnings("unused")
	final Class<? extends Serializable> entityClazz //
		= MyAnnotated.requireAnnotation(ip.getAnnotated(), JmsEntityType.class) //
			.value();
	return new JmsConsumerImpl<>(internalClient, destination);
    }

    @Produces
    public <T extends Serializable> JmsEventNotificatorClient<T> produceEventNotificator(final InjectionPoint ip) {
	final Destination destination = MyNaming.requireResource(ip.getAnnotated() //
		.getAnnotation(JmsDestination.class) //
		.value(), Destination.class);
	@SuppressWarnings("unused")
	final Class<? extends Serializable> entityClazz //
		= MyAnnotated.requireAnnotation(ip.getAnnotated(), JmsEntityType.class) //
			.value();
	return new JmsEventNotificatorImpl<>(internalClient, destination);
    }
}
