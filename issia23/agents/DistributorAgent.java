package issia23.agents;

import issia23.data.Product;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.*;
import java.util.List;

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
                    System.out.println(getLocalName() + " Requête pour un produit neuf : " + msg.getContent());

                    ACLMessage reply = msg.createReply();
                    Optional<Product> productToSell = products.stream()
                            .filter(p -> p.getName().equals(msg.getContent()))
                            .findFirst();

                    if (productToSell.isPresent()) {
                        double productPrice = productToSell.get().getPrice();
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(String.valueOf(productPrice));
                        myAgent.send(reply);
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Produit en rupture de stock.");
                        myAgent.send(reply);
                    }
                }
                else if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    String purchasedProduct = msg.getContent();
                    System.out.println("Livraison du produit " + purchasedProduct + " à " + msg.getSender().getLocalName());

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ACLMessage delivery = msg.createReply();
                            delivery.setPerformative(ACLMessage.INFORM);
                            delivery.setContent("Produit neuf livré : " + purchasedProduct);
                            myAgent.send(delivery);

                            products.removeIf(p -> p.getName().equals(purchasedProduct));
                            System.out.println("Produit " + purchasedProduct + " livré !");
                        }
                    }, 5000);
                } else {
                    block();
                }
            }
        });


        System.out.println(getLocalName() + " est prêt !");
    }


}
