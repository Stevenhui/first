package com.thisisnoble.javatest;

import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import com.thisisnoble.javatest.impl.CompositeEvent;

public class EventOrchestrator implements Orchestrator, Runnable{
	
	
	static ArrayBlockingQueue<Event> queue = null;
	static ArrayBlockingQueue<Event> processedQueue = null;
	ArrayList<Processor> procList = null;
	Publisher publisher = null;
	
	public EventOrchestrator() {
		EventOrchestrator.queue = new ArrayBlockingQueue<Event>(1000);
		EventOrchestrator.processedQueue = new ArrayBlockingQueue<Event>(1000);
		procList = new ArrayList<Processor>();
		
	}

	@Override
	public void register(Processor processor) {
		// TODO Auto-generated method stub
		this.procList.add(processor);
		
	}

	@Override
	public void receive(Event event) {
		// TODO Auto-generated method stub
		
		if (event.isProcessed() == false) {
			System.out.println("New event: " + event.getId());
			EventOrchestrator.queue.add(event);
		} else {
			System.out.println("Processed event: " + event.getId());
			EventOrchestrator.processedQueue.add(event);
		}
	}

	@Override
	public void setup(Publisher publisher) {
		// TODO Auto-generated method stub
		this.publisher = publisher;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		System.out.println("proc list: " + this.procList.size());
		while (true) {
			
		//	System.out.println("Retriving the queue....");
			Event e = EventOrchestrator.queue.poll();
			
			if (e != null) {
				int numOfInterest = 0;
				for (int i = 0; i < this.procList.size(); i++) {
					
					System.out.println(i + " Processing: " + e.getId() + " "+ procList.get(i).interestedIn(e));
					if (procList.get(i).interestedIn(e)) {
						procList.get(i).process(e);
						numOfInterest++;
					}
						
				}
				
				boolean isCompleted = false;
				
				System.out.println("End Processing...");
				
				CompositeEvent ce = new CompositeEvent(e.getId(), e);
				
				if (numOfInterest > processedQueue.size()) {
					System.out.println("Waiting processor....");
					try {
						Thread.currentThread().sleep(10);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				System.out.println("Interested Event: " + EventOrchestrator.processedQueue.size());
			    while (EventOrchestrator.processedQueue.isEmpty() ==  false)  {
			    	ce.addChild(EventOrchestrator.processedQueue.poll());
			    }
			    publisher.publish(ce);
			}
			
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
