package issia23.behaviours;

import issia23.agents.UserAgent;
import issia23.data.Product;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.ArrayList;
import java.util.List;

public class ContacterRepairCafe extends ContractNetInitiator {

    UserAgent monAgent;

    public ContacterRepairCafe(UserAgent a, ACLMessage msg) {
        super(a, msg);
        monAgent = a;
        monAgent.println("I am in the contacter repair cafe behaviour");
    }

    @Override
    public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
        monAgent.println("Agent " + propose.getSender().getLocalName() + " proposes " + propose.getContent() + "€");
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        monAgent.println("Refus reçu de " + refuse.getSender().getLocalName());
    }

    @Override
    protected void handleAllResponses(List<ACLMessage> theirVotes, List<ACLMessage> myAnswers) {
        ArrayList<ACLMessage> listeProposals = new ArrayList<>(theirVotes);
        listeProposals.removeIf(v -> v.getPerformative() != ACLMessage.PROPOSE);
        myAnswers.clear();

        ACLMessage bestProposal = null;
        ACLMessage bestAnswer = null;
        double bestPrice = Double.MAX_VALUE;

        for (ACLMessage proposal : listeProposals) {
            var answer = proposal.createReply();
            answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
            myAnswers.add(answer);

            try {
                double price = Double.parseDouble(proposal.getContent().trim());
                monAgent.println(proposal.getSender().getLocalName() + " propose " + price + "€");

                if (price < bestPrice) {
                    bestPrice = price;
                    bestProposal = proposal;
                    bestAnswer = answer;
                }
            } catch (NumberFormatException e) {
                monAgent.println("Erreur de conversion du prix : " + proposal.getContent());
            }
        }

        if (bestProposal != null) {
            double budget = monAgent.budget;
            double counterPrice = bestPrice * 0.90; // Contre-offre à -10%

            if (bestPrice <= budget) {
                bestAnswer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                monAgent.budget -= bestPrice;
                monAgent.println("Accepté pour " + bestPrice + "€. Budget restant : " + monAgent.budget + "€.");
            } else {
                monAgent.println("Prix trop élevé, proposition d'une contre-offre à " + counterPrice + "€.");

                ACLMessage counterOffer = bestProposal.createReply();
                counterOffer.setPerformative(ACLMessage.PROPOSE);
                counterOffer.setContent(String.valueOf(counterPrice));
                myAnswers.add(counterOffer);
            }
        } else {
            monAgent.println("Aucune proposition valable, recherche d'une pièce détachée.");

            ACLMessage requestPart = new ACLMessage(ACLMessage.REQUEST);
            requestPart.setConversationId("part_request");

            Product produitDefectueux = monAgent.products.get(0);
            String pieceRecherchee = produitDefectueux.getFaultyPart().getName();

            monAgent.println("Recherche de la pièce " + pieceRecherchee);
            requestPart.setContent(pieceRecherchee);

            for (AID aid : AgentServicesTools.searchAgents(monAgent, "repair", "SparePartsStore")) {
                requestPart.addReceiver(aid);
            }

            monAgent.send(requestPart);
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        monAgent.println("Réception d'une pièce : " + inform.getContent());

        if (inform.getContent().contains("Pièce livrée")) {
            monAgent.println("Réparation réussie avec la pièce achetée !");
        } else if (inform.getContent().contains("Plus de stock")) {
            monAgent.println("Impossible de réparer, achat d’un produit neuf...");

            ACLMessage requestNewProduct = new ACLMessage(ACLMessage.REQUEST);
            requestNewProduct.setConversationId("product_request");

            Product produitDefectueux = monAgent.products.get(0);
            String produitRecherche = produitDefectueux.getName();

            monAgent.println("Recherche d'un produit neuf : " + produitRecherche);
            requestNewProduct.setContent(produitRecherche);

            for (AID aid : AgentServicesTools.searchAgents(monAgent, "repair", "distributor")) {
                requestNewProduct.addReceiver(aid);
            }

            monAgent.send(requestNewProduct);
        }
    }
}
