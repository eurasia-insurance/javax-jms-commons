package tech.lapsa.javax.jms;

import java.io.Serializable;
import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.TemporaryQueue;

import tech.lapsa.java.commons.function.MyObjects;
import tech.lapsa.javax.jms.JmsClientFactory.JmsCallable;
import tech.lapsa.javax.jms.JmsClientFactory.JmsConsumer;
import tech.lapsa.javax.jms.JmsClientFactory.JmsSender;

public final class JmsClients {

    private JmsClients() {
    }

    //

    public static JMSRuntimeException uchedked(JMSException e) {
	return new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e.getCause());
    }

    //

    public static <E extends Serializable> JmsConsumer<E> createConsumer(final JMSContext context,
	    final Destination destination) {
	return new ConsumerClientImpl<>(context, destination);
    }

    public static <E extends Serializable> JmsConsumer<E> createConsumerQueue(final JMSContext context,
	    final String queuePhysicalName) {
	return new ConsumerClientImpl<>(context, context.createQueue(queuePhysicalName));
    }

    public static <E extends Serializable> JmsConsumer<E> createConsumerTopic(final JMSContext context,
	    final String topicPhysicalName) {
	return new ConsumerClientImpl<>(context, context.createTopic(topicPhysicalName));
    }

    //

    public static <E extends Serializable> JmsSender<E> createSender(final JMSContext context,
	    final Destination destination) {
	return new SenderClientImpl<>(context, destination);
    }

    public static <E extends Serializable> JmsSender<E> createSenderQueue(final JMSContext context,
	    final String queuePhysicalName) {
	return new SenderClientImpl<>(context, context.createQueue(queuePhysicalName));
    }

    public static <E extends Serializable> JmsSender<E> createSenderTopic(final JMSContext context,
	    final String topicPhysicalName) {
	return new SenderClientImpl<>(context, context.createTopic(topicPhysicalName));
    }

    //

    public static <E extends Serializable, R extends Serializable> JmsCallable<E, R> createCallable(
	    final JMSContext context, final Destination destination, final Class<R> resultClazz) {
	return new CallableClientImpl<>(resultClazz, context, destination);
    }

    public static <E extends Serializable, R extends Serializable> JmsCallable<E, R> createCallableQueue(
	    final JMSContext context, final String queuePhysicalName, final Class<R> resultClazz) {
	return new CallableClientImpl<>(resultClazz, context, context.createQueue(queuePhysicalName));
    }

    public static <E extends Serializable, R extends Serializable> JmsCallable<E, R> createCallableTopic(
	    final JMSContext context, final String topicPhysicalName, final Class<R> resultClazz) {
	return new CallableClientImpl<>(resultClazz, context, context.createTopic(topicPhysicalName));
    }

    //

    private static class BaseClient<E extends Serializable, R extends Serializable> {

	private static final int DEFAULT_TIMEOUT = 20 * 1000; // 20 seconds

	final JMSContext context;
	final Destination destination;
	final Class<R> resultClazz;

	private BaseClient(final Class<R> resultClazz, final JMSContext context,
		final Destination destination) {
	    this.resultClazz = MyObjects.requireNonNull(resultClazz, "resultClazz");
	    this.context = MyObjects.requireNonNull(context, "context");
	    this.destination = MyObjects.requireNonNull(destination, "destination");
	}

	@SafeVarargs
	final void _send(final E... entities) {
	    _send(null, entities);
	}

	@SafeVarargs
	final void _send(final Properties properties, final E... entities) {
	    _send(context, destination, properties, entities);
	}

	@SafeVarargs
	final static <E extends Serializable> void _send(final JMSContext context, final Destination destination,
		final Properties properties, final E... entities) {
	    final JMSProducer producer = context.createProducer();
	    _send(producer, destination, properties, entities);
	}

	@SafeVarargs
	final static <E extends Serializable> void _send(final JMSProducer producer, final Destination destination,
		final Properties properties, final E... entities) {
	    if (properties != null)
		MyMessages.propertiesToJMSProducer(producer, properties);
	    for (final E entity : entities)
		producer.send(destination, entity);
	}

	final R _request(final E entity) {
	    return _request(entity, null);
	}

	final R _request(final E entity, final Properties properties) {
	    try {
		Message resultM = null;

		{
		    TemporaryQueue replyToD = null;
		    try {
			replyToD = context.createTemporaryQueue();
			final Message entityM = context.createObjectMessage(entity);
			entityM.setJMSReplyTo(replyToD);
			if (properties != null)
			    MyMessages.propertiesToMessage(entityM, properties);
			context.createProducer().send(destination, entityM);
			final String jmsCorellationID = entityM.getJMSMessageID();
			final String messageSelector = String.format("JMSCorrelationID = '%1$s'", jmsCorellationID);
			try (final JMSConsumer consumer = context.createConsumer(replyToD, messageSelector)) {
			    resultM = consumer.receive(DEFAULT_TIMEOUT);
			}

		    } finally {
			try {
			    if (replyToD != null)
				replyToD.delete();
			} catch (final JMSException ignored) {
			}
		    }
		}

		if (resultM == null)
		    throw new ResponseNotReceivedException();

		if (resultM.isBodyAssignableTo(resultClazz))
		    return resultM.getBody(resultClazz);

		if (resultM.isBodyAssignableTo(RuntimeException.class))
		    throw resultM.getBody(RuntimeException.class);

		if (resultM.isBodyAssignableTo(Serializable.class)) {
		    final Object wrongTypedObject = resultM.getBody(Object.class);
		    if (wrongTypedObject != null)
			throw new UnexpectedResponseTypeException(resultClazz, wrongTypedObject.getClass());
		}

		throw new UnexpectedResponseTypeException("Unknown response type");

	    } catch (JMSException e) {
		throw uchedked(e);
	    }
	}
    }

    static final class SenderClientImpl<E extends Serializable> extends BaseClient<E, VoidResult>
	    implements JmsSender<E> {

	private SenderClientImpl(final JMSContext context, final Destination destination) {
	    super(VoidResult.class, context, destination);

	}

	@Override
	@SafeVarargs
	public final void send(final E... entities) {
	    _send(context, destination, null, entities);
	}

	@Override
	public void send(E entity, Properties properties) {
	    _send(context, destination, properties, entity);
	}
    }

    static final class ConsumerClientImpl<E extends Serializable> extends BaseClient<E, VoidResult>
	    implements JmsConsumer<E> {

	private ConsumerClientImpl(final JMSContext context, final Destination destination) {
	    super(VoidResult.class, context, destination);
	}

	@Override
	public void accept(final E entity, final Properties properties) {
	    final VoidResult outO = _request(entity, properties);
	    if (outO == null)
		throw new RuntimeException(VoidResult.class.getName() + " expected");
	}

	@Override
	public void accept(final E entity) {
	    accept(entity, null);
	}
    }

    static final class CallableClientImpl<E extends Serializable, R extends Serializable> extends BaseClient<E, R>
	    implements JmsCallable<E, R> {

	private CallableClientImpl(final Class<R> resultClazz, final JMSContext context,
		final Destination destination) {
	    super(resultClazz, context, destination);
	}

	@Override
	public R call(final E entity, final Properties properties) {
	    return _request(entity, properties);
	}

	@Override
	public R call(final E entity) {
	    return call(entity, null);
	}
    }
}