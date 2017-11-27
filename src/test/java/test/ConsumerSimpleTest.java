package test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Properties;

import javax.inject.Inject;
import javax.jms.JMSException;

import org.junit.Test;

import ejb.resources.consumer.simple.ConsumerSimpleDestination;
import ejb.resources.consumer.simple.ConsumerSimpleDrivenBean;
import ejb.resources.consumer.simple.ConsumerSimpleEntity;
import tech.lapsa.javax.jms.JmsClientFactory;
import tech.lapsa.javax.jms.JmsClientFactory.JmsConsumer;

public class ConsumerSimpleTest extends ArquillianBaseTestCase {

    @Inject
    private JmsClientFactory jmsClientFactory;

    @Inject
    private ConsumerSimpleDestination destination;

    public static ConsumerSimpleEntity BASIC_EXPECTED = null;

    @Test
    public void basic() throws JMSException {
	final JmsConsumer<ConsumerSimpleEntity> consumer //
		= jmsClientFactory.createConsumer(destination.getDestination());
	{
	    final String MESSAGE = "Hello JMS world!";

	    final ConsumerSimpleEntity e = new ConsumerSimpleEntity(MESSAGE);
	    consumer.accept(e);
	    assertThat(BASIC_EXPECTED, allOf(not(nullValue()), is(equalTo(e))));
	}
    }

    public static String WITH_PROPERTIES_EXPECTED = null;

    @Test
    public void withProperties() throws JMSException {
	final JmsConsumer<ConsumerSimpleEntity> consumer //
		= jmsClientFactory.createConsumer(destination.getDestination());
	{
	    final String MESSAGE = "Hello, %1$s!";
	    final String NAME = "John Bull";

	    final Properties properties = new Properties();
	    properties.setProperty(ConsumerSimpleDrivenBean.PROPERTY_NAME, NAME);

	    final ConsumerSimpleEntity e = new ConsumerSimpleEntity(MESSAGE);
	    consumer.accept(e, properties);
	    assertThat(WITH_PROPERTIES_EXPECTED, allOf(not(nullValue()), is(equalTo(NAME))));

	}
    }
}