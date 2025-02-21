package issia23.behaviours;

import issia23.agents.RepairCoffeeAgent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.*;

public class CafeRepondreUtilisateur extends ContractNetResponder {

    RepairCoffeeAgent monAgent;

    public CafeRepondreUtilisateur(RepairCoffeeAgent a, MessageTemplate model) {
        super(a, model);
        monAgent = a;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
        monAgent.println("Réception d'une demande pour " + cfp.getContent());
        ACLMessage answer = cfp.createReply();

        double basePrice = 5 + Math.random() * 10;
        answer.setPerformative(ACLMessage.PROPOSE);
        answer.setContent(String.valueOf(basePrice));

        return answer;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        monAgent.println("Réparation acceptée pour " + cfp.getContent());

        ACLMessage msg = accept.createReply();
        msg.setPerformative(ACLMessage.INFORM);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                monAgent.println("Réparation terminée pour " + cfp.getContent());
                msg.setContent("Réparation terminée !");
                monAgent.send(msg);
            }
        }, 3000);

        return msg;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        monAgent.println("Proposition refusée pour " + cfp.getContent());
    }

    protected ACLMessage handlePropose(ACLMessage propose) {
        double counterPrice = Double.parseDouble(propose.getContent().trim());
        double minAcceptablePrice = 7.0;

        ACLMessage reply = propose.createReply();

        if (counterPrice >= minAcceptablePrice) {
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            reply.setContent("Offre acceptée à " + counterPrice + "€.");
            monAgent.println("Contre-offre acceptée : " + counterPrice + "€.");
        } else {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Contre-offre refusée.");
            monAgent.println("Refus de la contre-offre.");
        }

        return reply;
    }
}
