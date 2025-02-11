package issia23.agents;

import issia23.data.Product;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DistributorAgent extends AgentWindowed {
    Set<Product> products;

    @Override
    protected void setup() {
        this.window = new SimpleWindow4Agent(getLocalName(), this);
        this.window.setBackgroundTextColor(Color.GRAY);
        println("hello, do you want a new object?");

        // Registration
        AgentServicesTools.register(this, "repair", "distributor");
        println("I'm just registered as a Distributor");

        products = new HashSet<>();
        var allProducts = Product.getListProducts();
        var nb = allProducts.size();
        var nbStock = (int)(nb * 0.8);
        for(int i=0; i<nbStock; i++) {
            var rand = (int)(Math.random() * nb);
            products.add(allProducts.get(rand));
        }
        println("I have the following products:");
        for(var p:products) println(p.getName());

        // Ajout d'un comportement pour vendre un produit neuf
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    System.out.println(getLocalName() + " received a purchase request for " + msg.getContent());

                    ACLMessage reply = msg.createReply();
                    boolean productAvailable = products.stream().anyMatch(p -> p.getName().equals(msg.getContent()));

                    if (productAvailable) {
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent("Product available for purchase.");
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Sorry, out of stock.");
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
