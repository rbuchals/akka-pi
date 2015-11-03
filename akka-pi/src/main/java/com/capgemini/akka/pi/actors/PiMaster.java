package com.capgemini.akka.pi.actors;

import java.util.concurrent.TimeUnit;

import com.capgemini.akka.pi.messages.Calculate;
import com.capgemini.akka.pi.messages.PiApproximation;
import com.capgemini.akka.pi.messages.Result;
import com.capgemini.akka.pi.messages.Work;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinRouter;
import akka.util.Duration;

public class PiMaster extends UntypedActor {
	private final int nrOfMessages;
	private final int nrOfElements;

	private double pi;
	private int nrOfResults;
	private final long start = System.currentTimeMillis();

	private final ActorRef listener;
	private final ActorRef workerRouter;

	public PiMaster(final int nrOfWorkers, int nrOfMessages, int nrOfElements, ActorRef listener) {
		this.nrOfMessages = nrOfMessages;
		this.nrOfElements = nrOfElements;
		this.listener = listener;

		workerRouter = this.getContext()
				.actorOf(new Props(PiWorker.class).withRouter(new RoundRobinRouter(nrOfWorkers)), "workerRouter");
	}

	public void onReceive(Object message) {
		if (message instanceof Calculate) {
			for (int start = 0; start < nrOfMessages; start++) {
				workerRouter.tell(new Work(start, nrOfElements), getSelf());
			}
		} else if (message instanceof Result) {
			Result result = (Result) message;
			pi += result.getValue();
			nrOfResults += 1;
			if (nrOfResults == nrOfMessages) {
				// Send the result to the listener
				Duration duration = Duration.create(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
				listener.tell(new PiApproximation(pi, duration), getSelf());
				// Stops this actor and all its supervised children
				getContext().stop(getSelf());
			}
		} else {
			unhandled(message);
		}
	}
}
