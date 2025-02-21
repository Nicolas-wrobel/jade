package issia23.agents;

import issia23.data.Product;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import issia23.behaviours.ContacterRepairCafe;

import java.util.*;

public class UserAgent extends AgentWindowed {
    /**
     * skill in "repairing" from 0 (not understand) to 3 (repairman like)
     */
    int skill;
    /**
     * list of potential helpers (agent registered in the type of service "repair"
     */
    List<AID> helpers;

    public List<Product> products;
    private boolean cfpSent = false;
    public double budget;

    @Override
    public void setup() {
        this.window = new SimpleWindow4Agent(getLocalName(), this);
        window.setButtonActivated(true);

        skill = (int) (Math.random() * 4);
        budget = 50 + Math.random() * 200;

        println("Mon budget initial est de " + String.format("%.2f", budget) + "€");
        println("hello, I have a skill = " + skill);

        helpers = new ArrayList<>();
        products = new ArrayList<>();
        var allProducts = Product.getListProducts();
        var nb = allProducts.size();
        for (int i = 0; i < 3; i++) {
            var rand = (int) (Math.random() * nb);
            products.add(allProducts.get(rand));
        }
        println("I have the following products:");
        for (var p : products) println(p.getName());

        System.out.println(getLocalName() + " est prêt !");

        addBehaviour(new jade.core.behaviours.OneShotBehaviour() {
            @Override
            public void action() {
                helpers.addAll(Arrays.stream(AgentServicesTools.searchAgents(myAgent, "repair", null)).toList());

                println("-".repeat(30));
                for (AID aid : helpers)
                    println("Found repair agent: " + aid.getLocalName());
                println("-".repeat(30));

                if (!cfpSent) {
                    addCFP();
                    cfpSent = true;
                }
            }
        });
    }


    @Override
    public void onGuiEvent(GuiEvent evt) {
        helpers.addAll(Arrays.stream(AgentServicesTools.searchAgents(this, "repair", null)).toList());

        println("-".repeat(30));
        for (AID aid : helpers)
            println("found this agent : " + aid.getLocalName());
        println("-".repeat(30));

        if (!cfpSent) {  // Vérifie que `addCFP()` n'a pas déjà été appelé
            addCFP();
            cfpSent = true;
        }
    }

    /**add a CFP from user to list of helpers*/
    private void addCFP() {
        System.out.println("`addCFP()` est appelé !");
        System.out.println("Nombre de produits avant sélection: " + products.size());

        if (products.isEmpty()) {
            println("No products available for repair request.");
            return;
        }

        int randint = (int) (Math.random() * products.size());
        Product selectedProduct = products.get(randint);

        if (selectedProduct == null || selectedProduct.getName() == null || selectedProduct.getName().isEmpty()) {
            println("Erreur : produit sélectionné invalide.");
            return;
        }

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId("id");
        msg.setContent(selectedProduct.getName());
        msg.addReceivers(helpers.toArray(AID[]::new));

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

        println("Demande envoyée pour la réparation de : " + selectedProduct.getName());
        addBehaviour(new ContacterRepairCafe(this, msg));
    }



    /**here we simplify the scenario. A breakdown is about 1 elt..
     * so whe choose a no between 1 to 4 and ask who can repair at at wich cost.*/
    private void breakdown(){

    }

    public void println(String s){
        window.println(s);
    }

    @Override
    public void takeDown(){println("bye !!!");}
}
