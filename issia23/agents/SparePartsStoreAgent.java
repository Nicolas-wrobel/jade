package issia23.agents;

import issia23.data.Part;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.Color;
import java.util.*;

public class SparePartsStoreAgent extends AgentWindowed {
    List<Part> parts;

    @Override
    protected void setup() {
        this.window = new SimpleWindow4Agent(getLocalName(), this);
        this.window.setBackgroundTextColor(Color.cyan);
        println("hello, do you want a piece of something ?");

        // Registration
        AgentServicesTools.register(this, "repair", "SparePartsStore");
        println("I'm just registered as a Spare Parts Store");

        // Initialisation des pièces en stock
        parts = new ArrayList<>();
        var allParts = Part.getListParts();
        var nb = allParts.size();
        var nbStock = (int)(nb * 0.6);
        for(int i=0; i<nbStock; i++) {
            var rand = (int)(Math.random() * nb);
            for(int j=0; j<3; j++)
                parts.add(allParts.get(rand));
        }
        println("I have the following parts:");
        for(var p:parts) println(p.getName());

        // Ajout d'un comportement pour répondre aux demandes de pièces
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    String requestedPart = msg.getContent();
                    System.out.println(getLocalName() + "Demande reçue pour " + requestedPart);

                    ACLMessage reply = msg.createReply();
                    Optional<Part> partToSell = parts.stream()
                            .filter(p -> p.getName().equals(requestedPart))
                            .findFirst();

                    if (partToSell.isPresent()) {
                        double partPrice = 20 + Math.random() * 20; // Prix entre 20€ et 40€
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(String.valueOf(partPrice));
                        myAgent.send(reply);

                        // Attendre la réponse de l'utilisateur avant de livrer la pièce
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Plus de stock pour cette pièce.");
                        myAgent.send(reply);
                    }
                }
                else if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    String purchasedPart = msg.getContent();
                    System.out.println("Livraison en cours pour " + msg.getSender().getLocalName());

                    // Simuler un délai de livraison
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ACLMessage delivery = msg.createReply();
                            delivery.setPerformative(ACLMessage.INFORM);
                            delivery.setContent("Pièce livrée : " + purchasedPart);
                            myAgent.send(delivery);

                            // Supprimer la pièce du stock après livraison
                            parts.removeIf(p -> p.getName().equals(purchasedPart));

                            System.out.println("Pièce " + purchasedPart + " livrée à " + msg.getSender().getLocalName());
                        }
                    }, 5000); // Délai de livraison de 5 secondes
                }
                else {
                    block();
                }
            }
        });

        System.out.println(getLocalName() + " est prêt !");
    }


}
