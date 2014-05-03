package com.thisisnoble.javatest.processors;

import com.thisisnoble.javatest.Event;
import com.thisisnoble.javatest.Orchestrator;
import com.thisisnoble.javatest.Processor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractProcessor implements Processor {

    private final Orchestrator orchestrator;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private boolean isDone = false;
    
    public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	protected AbstractProcessor(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public final void process(Event event) {
        threadPool.submit(new EventTask(event));
    }

    protected abstract Event processInternal(Event event);

    private class EventTask implements Runnable {
        private final Event input;

        private EventTask(Event input) {
            this.input = input;
        }

        @Override
        public void run() {
        	
        	System.out.println("running the abstract processor");
            Event output = processInternal(input);
            output.setProcessed(true);
            orchestrator.receive(output);
            isDone = true;
        }
    }
}
