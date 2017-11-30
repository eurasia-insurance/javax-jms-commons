package tech.lapsa.javax.jms.commons;

import java.util.Optional;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import tech.lapsa.java.commons.function.MyExceptions.CheckedExceptionThrowingCallable;
import tech.lapsa.java.commons.function.MyExceptions.CheckedExceptionThrowingSupplier;
import tech.lapsa.java.commons.function.MyObjects;
import tech.lapsa.java.commons.function.MyOptionals;

public final class MyJMSs {

    private MyJMSs() {
    }

    public static JMSRuntimeException uchedked(JMSException e) {
	return new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e.getCause());
    }

    public static <T, E extends JMSException> T reThrowAsUnchecked(
	    final CheckedExceptionThrowingSupplier<T, E> supplier) throws JMSRuntimeException {
	try {
	    return supplier.get();
	} catch (final JMSException e) {
	    throw uchedked(e);
	}
    }

    public static <E extends JMSException> void reThrowAsUnchecked(final CheckedExceptionThrowingCallable<E> callable)
	    throws JMSRuntimeException {
	try {
	    callable.call();
	} catch (final JMSException e) {
	    throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e.getCause());
	}
    }

    public static Optional<String> optJMSMessageIDOf(final Message m) {
	return MyOptionals.of(m) //
		.map(mmm -> {
		    try {
			return mmm.getJMSMessageID();
		    } catch (JMSException e) {
			return null;
		    }
		});
    }

    public static Optional<String> optJMSCorellationIDOf(final Message m) {
	return MyOptionals.of(m) //
		.map(mmm -> {
		    try {
			return mmm.getJMSCorrelationID();
		    } catch (JMSException e) {
			return null;
		    }
		});
    }

    public static Optional<String> optJMSDestination(final Message m) {
	return MyOptionals.of(m) //
		.map(mmm -> {
		    try {
			return mmm.getJMSDestination();
		    } catch (JMSException e) {
			return null;
		    }
		}) //
		.flatMap(MyJMSs::optNameOf);
    }

    public static String getJMSDestination(final Message m) {
	return optJMSDestination(m).orElse("Unknwon JMSDestination");
    }

    public static String getJMSCorellationIDOf(final Message m) {
	return optJMSCorellationIDOf(m).orElse("Unknwon JMSCorellationID");
    }

    public static String getJMSMessageIDOf(final Message m) {
	return optJMSMessageIDOf(m).orElse("Unknwon JMSMessageID");
    }

    public static String getNameOf(final Destination d) {
	return optNameOf(d).orElse("Unknown Destination name");
    }

    public static Optional<String> optNameOf(final Destination d) {
	final Optional<String> tryQueue = MyObjects.optionalA(d, Queue.class) //
		.map(q -> {
		    try {
			return q.getQueueName();
		    } catch (JMSException e) {
			return null;
		    }
		}) //
		.map(x -> "Queue('" + x + "')");
	if (tryQueue.isPresent())
	    return tryQueue;
	final Optional<String> tryTopic = MyObjects.optionalA(d, Topic.class) //
		.map(q -> {
		    try {
			return q.getTopicName();
		    } catch (JMSException e) {
			return null;
		    }
		}) //
		.map(x -> "Topic('" + x + "')");
	if (tryTopic.isPresent())
	    return tryTopic;

	return Optional.empty();
    }
}
