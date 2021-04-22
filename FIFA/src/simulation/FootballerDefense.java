package simulation;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.StringTokenizer;
import java.util.Vector;

import static java.lang.Math.abs;

public class FootballerDefense extends Agent {
    int x, y;
    int step_x, step_y;
    int position;
    String opponent;
    String team;
    public Vector footballers = new Vector();
    AID ball_aid;
    MessageTemplate mt_of_ball;
    AID goalkeeper;
    ACLMessage msg_goalkeeper;
    int[] ball_position =new int[2];
    ACLMessage msg_FootballerOpponent;
    public Vector footballersOpponent = new Vector();


    public FootballerDefense(int y,int x,int position,String team,String opponent){
        this.position=position;
        this.y = y;
        this.x = x;
        this.team=team;
        this.opponent = opponent;
    }
    protected void setup(){
        //---------- REJESTRACJA DO DF ---------------
        //Zapis do dróżyny
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Footballer");
        sd1.setName(team);
        //Zapis pozycji
        ServiceDescription sd2 = new ServiceDescription();
        sd2.setType("Footballer");
        sd2.setName("defender"+team);

        register(sd1,sd2);
        //------------------------

        //----------- WYSZUKIWANIE ZAWODNIKÓW -----------
        footballers = searchDF("Footballer",team);
        footballersOpponent = searchDF("Footballer",opponent);
        goalkeeper = new AID( "goalkeeper"+team,AID.ISLOCALNAME);

        //wyszukanie siebie w liście i usunięcie
        AID myID = getAID();
        int remove_item=0;
        for (int i=0; i<footballers.size();i++) {
            if(footballers.get(i).equals(myID)){
                remove_item = i;
            }
        }
        //usunięcie
        footballers.remove(remove_item);

        //--------------- DODANIE PIŁKARZY DO ADREDATÓW WIADOMOŚCI
        msg_FootballerOpponent = new ACLMessage(ACLMessage.INFORM);
        for (Object i: footballersOpponent) {
            msg_FootballerOpponent.addReceiver((AID) i);
        }

        msg_goalkeeper = new ACLMessage(ACLMessage.INFORM);
        msg_goalkeeper.addReceiver((goalkeeper));

        //---------------- DODANIE FILTRA WIADOMOŚCI
        //filtry na piłke
        ball_aid = new AID( "Ball",AID.ISLOCALNAME);
        mt_of_ball =
                MessageTemplate.and(
                        MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                        MessageTemplate.MatchSender( ball_aid)) ;

        //--------------- WYSYŁANIE WIADOMOŚCI

        msg_FootballerOpponent.setContent(Integer.toString(x) +
                ";" + Integer.toString(y));
        msg_FootballerOpponent.setOntology("position");
        send(msg_FootballerOpponent );

        msg_goalkeeper.setContent(Integer.toString(x) +
                ";" + Integer.toString(y));
        msg_goalkeeper.setOntology("position");
        send(msg_goalkeeper);

        //Odbieranie wiadomości
        addBehaviour(new FootballerDefense.GetBallPosition());
        addBehaviour(new FootballerDefense.Move(this,200));

    }

    class Move extends TickerBehaviour {
        int dis=0;
        int directive=1;
        int step =5;
        FootballerDefense footballer;
        public Move(FootballerDefense a, long period) {
            super(a, period);
            footballer=a;
        }

        @Override
        protected void onTick() {
            dis=0;
            //dis=checkDis();

            moveBall(dis);

            if((abs(footballer.ball_position[0]-footballer.x)<=13 & abs(footballer.ball_position[1]-footballer.y)<=13)){
                //Tworzenie wiadomości do piłki
                ACLMessage ball_msg = new ACLMessage(ACLMessage.INFORM);
                ball_msg.addReceiver(footballer.ball_aid);
                //Informacja, że piłkarz ma piłke
                ball_msg.setContent(Integer.toString(footballer.x)
                        + ";" + Integer.toString(footballer.y));
                ball_msg.setOntology("Defense");
                footballer.send(ball_msg);
            }

            msg_FootballerOpponent.setContent(Integer.toString(x) +
                    ";" + Integer.toString(y));
            msg_FootballerOpponent.setOntology("position");
            send(msg_FootballerOpponent );

            msg_goalkeeper.setContent(Integer.toString(x) +
                    ";" + Integer.toString(y));
            msg_goalkeeper.setOntology("position");
            send(msg_goalkeeper);
        }

        public void moveBall(int d){
            int dis =d;
            int check;

            //Bieganie za piłką
            footballer.step_y=abs(footballer.ball_position[1]-footballer.y)/9<=2
                    ?5:abs(footballer.ball_position[1]-footballer.y)/9;

            footballer.step_x=abs(footballer.ball_position[0]-footballer.x)/9<=2
                    ?5:abs(footballer.ball_position[0]-footballer.x)/9;

            check = footballer.x + ((footballer.ball_position[0]-footballer.x)/footballer.step_x);
            if(check>750) footballer.x = check;
            if(check<300) footballer.x = check;
            switch(footballer.position){
                case 1:
                    check= footballer.y + ((footballer.ball_position[1]-footballer.y)/footballer.step_y);
                    if(check<105 & check>0){
                        footballer.y = check+dis;
                    }
                    break;
                case 2:
                    check= footballer.y + ((footballer.ball_position[1]-footballer.y)/footballer.step_y);
                    if(check>110 & check<210){
                        footballer.y = check;
                    }
                    break;
                case 3:
                    check= footballer.y + ((footballer.ball_position[1]-footballer.y)/footballer.step_y);
                    if(check>220 & check<320){
                        footballer.y = check+dis;
                    }
                    break;
                case 4:
                    check= footballer.y + ((footballer.ball_position[1]-footballer.y)/footballer.step_y);
                    if(check>330 & check<500){
                        footballer.y = check+dis;
                    }
                    break;
            }
        }

    }

    class GetBallPosition extends CyclicBehaviour {

        @Override
        public void action() {
            //Odbieranie pozycji piłki
            ACLMessage msg_of_ball = receive(mt_of_ball);
            if (msg_of_ball != null) {
//                    System.out.println( "Otrzymałem wiadomość " +
//                            myAgent.getLocalName() + " <- " +
//                            msg_of_ball.getContent()+" od " + msg_of_ball.getSender() );
                StringTokenizer st = new StringTokenizer(msg_of_ball.getContent(), ";");
                int i = 0;
                while (st.hasMoreTokens()) {
                    ball_position[i] = Integer.parseInt(st.nextToken());
                    i++;
                }

            }
        }
    }

    //Metoda do zarejestrowanie agenta w
    void register( ServiceDescription... sd)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        for(ServiceDescription i: sd) dfd.addServices(i);
        try {
            DFService.register(this, dfd );
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
    }
    //Wyszukiwanie agenta po jego usłudzie
    Vector searchDF(String service,String team )
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType( service );
        //Jest możliwość wybierania innych usług
        sd.setName(team);
        dfd.addServices(sd);

        try
        {
            DFAgentDescription[] result = DFService.search(this, dfd);
            //System.out.println("PLAYER Found " + result.length + " PLAYERS");
            Vector agents = new Vector();
            for (int i=0; i<result.length; i++)
                agents.add(result[i].getName()) ;
            return agents;

        }
        catch (FIPAException fe) { fe.printStackTrace(); }

        return null;
    }

}
