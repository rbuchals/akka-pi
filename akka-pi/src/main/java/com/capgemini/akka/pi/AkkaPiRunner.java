package com.capgemini.akka.pi;

import com.capgemini.akka.pi.actors.PiListener;
import com.capgemini.akka.pi.actors.PiMaster;
import com.capgemini.akka.pi.messages.Calculate;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

public class AkkaPiRunner {

	private static final int NUMBER_OF_MESSAGES = 10000;
	private static final int NUMERB_OF_ELEMENTS = 10000;
	private static final int NUMBER_OF_WORKERS = 8;

	public static void main(String[] args) {
		AkkaPiRunner pi = new AkkaPiRunner();
		pi.calculate(NUMBER_OF_WORKERS, NUMERB_OF_ELEMENTS, NUMBER_OF_MESSAGES);
	}

	public void calculate(final int nrOfWorkers, final int nrOfElements, final int nrOfMessages) {
		// Create an Akka system
		ActorSystem system = ActorSystem.create("PiSystem");

		// create the result listener, which will print the result and shutdown
		// the system
		final ActorRef listener = system.actorOf(new Props(PiListener.class), "listener");

		// create the master
		ActorRef master = system.actorOf(new Props(new UntypedActorFactory() {
			public UntypedActor create() {
				return new PiMaster(nrOfWorkers, nrOfMessages, nrOfElements, listener);
			}
		}), "master");

		// start the calculation
		master.tell(new Calculate());
	}
}
