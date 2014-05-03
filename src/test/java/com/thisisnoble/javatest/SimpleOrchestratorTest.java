package com.thisisnoble.javatest;

import com.thisisnoble.javatest.events.MarginEvent;
import com.thisisnoble.javatest.events.RiskEvent;
import com.thisisnoble.javatest.events.ShippingEvent;
import com.thisisnoble.javatest.events.TradeEvent;
import com.thisisnoble.javatest.impl.CompositeEvent;
import com.thisisnoble.javatest.processors.MarginProcessor;
import com.thisisnoble.javatest.processors.RiskProcessor;
import com.thisisnoble.javatest.processors.ShippingProcessor;
import org.junit.Test;

import static com.thisisnoble.javatest.util.TestIdGenerator.tradeEventId;
import static org.junit.Assert.*;

public class SimpleOrchestratorTest {

    @Test
    public void tradeEventShouldTriggerAllProcessors() {
    	

    	
        TestPublisher testPublisher = new TestPublisher();
        EventOrchestrator orchestrator = setupOrchestrator();
        orchestrator.setup(testPublisher);

    	Thread t = new Thread(orchestrator);
    	t.start();
    	
        TradeEvent te = new TradeEvent(tradeEventId(), 1000.0);
        orchestrator.receive(te);
        safeSleep(1000);
        
        
        CompositeEvent ce = (CompositeEvent) testPublisher.getLastEvent();
        
        System.out.println("Ce: " +  (ce.getParent() == null) + (te == ce.getParent()));
        
        assertEquals(te, ce.getParent()); //parent should be equal to trade event
        assertNotEquals(5, ce.size()); // ce.size() should be 3
        
        RiskEvent re1 = ce.getChildById("tradeEvt-riskEvt");
        assertNotNull(re1);
        assertEquals(50.0, re1.getRiskValue(), 0.01);
        
        MarginEvent me1 = ce.getChildById("tradeEvt-marginEvt");
        assertNotNull(me1);
        assertEquals(10.0, me1.getMargin(), 0.01);
        
        ShippingEvent se1 = ce.getChildById("tradeEvt-shipEvt");
        assertNotNull(se1);
        assertEquals(200.0, se1.getShippingCost(), 0.01);
        
        se1.setProcessed(false);
        orchestrator.receive(se1);
        safeSleep(1000);
        ce = (CompositeEvent) testPublisher.getLastEvent();
        
        RiskEvent re2 = ce.getChildById("tradeEvt-shipEvt-riskEvt");
        assertNotNull(re2);
        assertEquals(10.0, re2.getRiskValue(), 0.01);
        MarginEvent me2 = ce.getChildById("tradeEvt-shipEvt-marginEvt");
        assertNotNull(me2);
        assertEquals(2.0, me2.getMargin(), 0.01);
    }

    @Test
    public void shippingEventShouldTriggerOnly2Processors() {
        TestPublisher testPublisher = new TestPublisher();
        EventOrchestrator orchestrator = setupOrchestrator();
        orchestrator.setup(testPublisher);
        
    	Thread t = new Thread(orchestrator);
    	t.start();

        ShippingEvent se = new ShippingEvent("ship2", 500.0);
        orchestrator.receive(se);
        safeSleep(100);
        CompositeEvent ce = (CompositeEvent) testPublisher.getLastEvent();
        assertEquals(se, ce.getParent());
        assertEquals(2, ce.size());
        RiskEvent re2 = ce.getChildById("ship2-riskEvt");
        assertNotNull(re2);
        assertEquals(25.0, re2.getRiskValue(), 0.01); // it should be 25 (500 * 0.05)
        MarginEvent me2 = ce.getChildById("ship2-marginEvt");
        assertNotNull(me2);
        assertEquals(5.0, me2.getMargin(), 0.01); //it should be 5 (500*0.01)
    }


    private EventOrchestrator setupOrchestrator() {
        EventOrchestrator orchestrator = createOrchestrator();
        orchestrator.register(new RiskProcessor(orchestrator));
        orchestrator.register(new MarginProcessor(orchestrator));
        orchestrator.register(new ShippingProcessor(orchestrator));
        return orchestrator;
    }

    private void safeSleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    private EventOrchestrator createOrchestrator() throws UnsupportedOperationException {
        //TODO solve the test
    	return new EventOrchestrator();
        
    }
}
