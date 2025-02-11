package issia23.agents;

import issia23.data.Part;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import issia23.behaviours.CafeRepondreUtilisateur;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class RepairCoffeeAgent extends AgentWindowed {
    List<Part> parts;

    @Override
    protected void setup() {
        this.window = new SimpleWindow4Agent(getLocalName(), this);
        this.window.setBackgroundTextColor(Color.orange);
        println("hello, do you want coffee ?");

        // Registration dans les services JADE
        AgentServicesTools.register(this, "repair", "coffee");
        println("I'm just registered as a repair-coffee");

        // Initialisation des pièces
        parts = new ArrayList<>();
        var allParts = Part.getListParts();
        var nb = allParts.size();
        var nbStock = (int)(nb * 0.05);
        for(int i=0; i<nbStock; i++) {
            var rand = (int)(Math.random()*nb);
            for(int j=0; j<2; j++)
                parts.add(allParts.get(rand));
        }
        println("I have the following parts:");
        for(var p:parts) println(p.getName());

        // Ajout d'un comportement pour écouter les demandes de réparation
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    System.out.println(getLocalName() + " received a message from " + msg.getSender().getLocalName() + " : " + msg.getContent());

                    // Générer un prix aléatoire entre 5€ et 15€
                    int repairCost = (int) (5 + Math.random() * 10);

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(repairCost));  //  Envoyer uniquement le prix
                    myAgent.send(reply);
                } else {
                    block();
                }
            }
        });

        System.out.println(getLocalName() + " est prêt !");
    }



    private void addListeningACFP()
    {

        MessageTemplate model = MessageTemplate.MatchConversationId("id");

        var attenteDemandeUtilisateur = new CafeRepondreUtilisateur(this, model);

        addBehaviour(attenteDemandeUtilisateur);
    }


    public void println(String s){
        window.println(s);
    }

}
