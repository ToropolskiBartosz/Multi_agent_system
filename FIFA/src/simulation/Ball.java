package simulation;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

import static java.lang.Math.abs;

public class Ball extends Agent {
    boolean move = false;
    boolean drybling = false;
    boolean pass = false;
    boolean defense= false;
    int x=525-10;
    int y=275-10;
    int[] footbalerposition = new int[2];
    int[] goalposition = new int[2];
    int[] old_footbalerposition = new int[2];
    int directive=1;
    int directive2=1;
    public Vector footballers = new Vector();

    protected void setup(){

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        addBehaviour(new TickerBehaviour(this,200) {
            @Override
            protected void onTick() {
                footballers.clear();
                footballers = searchDF("Footballer");
                for(Object i: footballers){
                      msg.addReceiver((AID) i);
                }
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg_of_ball = receive(
                        MessageTemplate.MatchOntology("Kick"));
                if (msg_of_ball != null) {
                    StringTokenizer st = new StringTokenizer(msg_of_ball.getContent(), ";");
                    int position = 0;
                    while(st.hasMoreTokens()){
                        goalposition[position] = Integer.parseInt(st.nextToken());
                        position++;
                    }
                    goalposition[0] = (goalposition[0]>500)?goalposition[0]+100:goalposition[0]-100;
                    move = true;
                    drybling = false;
                    pass = false;
                    defense= false;
                }else{
                    block();
                }
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg_of_ball = receive(
                        MessageTemplate.MatchOntology("drybling"));
                if (msg_of_ball != null) {
                    StringTokenizer st = new StringTokenizer(msg_of_ball.getContent(), ";");
                    int position = 0;
                    while(st.hasMoreTokens()){
                        footbalerposition[position] = Integer.parseInt(st.nextToken());
                        position++;
                    }
                    drybling = true;
                    move = false;
                    pass = false;
                    defense= false;
                }else{
                    block();
                }
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg_of_ball = receive(
                        MessageTemplate.MatchOntology("pass"));
                if (msg_of_ball != null) {
                    StringTokenizer st = new StringTokenizer(msg_of_ball.getContent(), ";");
                    int position = 0;
                    while(st.hasMoreTokens()){
                        footbalerposition[position] = Integer.parseInt(st.nextToken());
                        position++;
                    }
                    pass = true;
                    move = false;
                    drybling = false;
                    defense= false;
                }else{
                    block();
                }
            }
        });

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg_of_ball = receive(
                        MessageTemplate.MatchOntology("Defense"));
                if (msg_of_ball != null) {
                    StringTokenizer st = new StringTokenizer(msg_of_ball.getContent(), ";");
                    int position = 0;
                    while(st.hasMoreTokens()){
                        footbalerposition[position] = Integer.parseInt(st.nextToken());
                        position++;
                    }
                    pass = false;
                    move = false;
                    drybling = false;
                    defense= true;
                }else{
                    block();
                }
            }
        });

        //Poruszanie się piłki
        addBehaviour(new TickerBehaviour(this,150){
            int moveball = 23;
            int direction;
            @Override
            protected void onTick() {

                if(move ){
                    x = x+(goalposition[0]-x)/5;
                    y = y+(goalposition[1]-y)/5;
                   // System.out.println("STRZAŁ");
                }else if(drybling){
                    int direction =footbalerposition[0]-old_footbalerposition[0];
                    if(abs(direction)<10){
                        if(direction>0){
                            x = footbalerposition[0]+15;
                        }
                        if(direction<0){
                            x = footbalerposition[0]-15;
                        }
                        y = footbalerposition[1];
                    }
                    //System.out.println(getLocalName()+"zna pozycje:"+footbalerposition[0]+" : "+footbalerposition[1]);
                    old_footbalerposition[0] = footbalerposition[0];
                    old_footbalerposition[1] = footbalerposition[1];

                    //System.out.println("DRYBLING");
                }else if(pass){
                    //System.out.println(footbalerposition[0]);
                    x = x+(footbalerposition[0]-x)/5;
                    y = y+(footbalerposition[1]-y)/5;
                    //System.out.println("PODANIE");
                }else if(defense) {
                    direction = (footbalerposition[0]>500)? -1:1;
                    x = x+(moveball*direction);
                    moveball--;
                    if(moveball==0){ defense=false;moveball=20;}
                    //System.out.println("OBRONA");
                }


                msg.setContent(Integer.toString(x)+";"+Integer.toString(y));
                send(msg);
            }
        });

    }

    Vector searchDF(String service )
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( service );
        //sd.setName("player");
        dfd.addServices(sd);

        try
        {
            DFAgentDescription[] result = DFService.search(this, dfd);
            //System.out.println("Found " + result.length + " PLAYERS");
             Vector agents = new Vector();
            for (int i=0; i<result.length; i++)
                agents.add(result[i].getName()) ;
            return agents;

        }
        catch (FIPAException fe) { fe.printStackTrace(); }

        return null;
    }
}
