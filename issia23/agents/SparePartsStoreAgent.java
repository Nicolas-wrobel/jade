package issia23.agents;

import issia23.data.Part;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

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
                    System.out.println(getLocalName() + " received a request for " + msg.getContent());

                    ACLMessage reply = msg.createReply();
                    boolean partAvailable = parts.stream().anyMatch(p -> p.getName().equals(msg.getContent()));

                    if (partAvailable) {
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("I have the part!");
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Sorry, no stock.");
                    }
                    myAgent.send(reply);
                } else {
                    block();
                }
            }
        });

        System.out.println(getLocalName() + " est prêt !");
    }


}
