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
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import static java.lang.Math.abs;

public class Goalkeeper extends Agent {
    int x, y;
    int step_y;
    String opponent;
    String team;
    public Vector footballers = new Vector();
    ArrayList<MessageTemplate> mt_footballers_position = new ArrayList<MessageTemplate>();
    int[][] player_position = new int[4][2];
    AID ball_aid;
    MessageTemplate mt_of_ball;
    int[] ball_position =new int[2];
    ACLMessage msg_FootballerOpponent;
    public Vector footballersOpponent = new Vector();

    public Goalkeeper(int y,int x,String team,String opponent){
        this.y = y;
        this.x = x;
        this.team=team;
        this.opponent = opponent;
    }
    protected void setup(){
        //---------- REJESTRACJA DO DF ---------------

        ServiceDescription sd2 = new ServiceDescription();
        sd2.setType("Footballer");
        sd2.setName("Goalkeeper"+team);

        register(sd2);
        //------------------------

        //----------- WYSZUKIWANIE ZAWODNIKÓW -----------
        footballers = searchDF("Footballer","defender"+team);


        //---------------- DODANIE FILTRA WIADOMOŚCI

        ball_aid = new AID( "Ball",AID.ISLOCALNAME);
        mt_of_ball =
                MessageTemplate.and(
                        MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                        MessageTemplate.MatchSender( ball_aid)) ;

        for(Object i: footballers){
            mt_footballers_position.add(MessageTemplate.and(
                    MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                    MessageTemplate.and(
                            MessageTemplate.MatchSender( (AID) i ),
                            MessageTemplate.MatchOntology("position"))));
        }
        //---------------------------------


        addBehaviour(new Goalkeeper.GetBallPosition());
        addBehaviour(new Goalkeeper.GetFootballerPosition());
        addBehaviour(new Goalkeeper.Move(this,200));

    }

    class Move extends TickerBehaviour {
        int dis=0;
        Goalkeeper footballer;
        public Move(Goalkeeper a, long period) {
            super(a, period);
            footballer=a;
        }

        @Override
        protected void onTick() {
            dis=0;

            moveBall(dis);

            if((abs(footballer.ball_position[0]-footballer.x)<=13
                    & abs(footballer.ball_position[1]-footballer.y)<=13)){
                //Tworzenie wiadomości do piłki

                ACLMessage ball_msg = new ACLMessage(ACLMessage.INFORM);
                ball_msg.addReceiver(footballer.ball_aid);
                Random rand = new Random();

                int random_position = rand.nextInt(4);
                ball_msg.setContent(Integer.toString(footballer.player_position[random_position][0] )
                        + ";" + Integer.toString(footballer.player_position[random_position][1] ));
                ball_msg.setOntology("pass");

                footballer.send(ball_msg);
            }

        }

        public void moveBall(int d){
            int dis =d;
            int check;

            //Bieganie za piłką
            footballer.step_y=abs(footballer.ball_position[1]-footballer.y)/9<=2
                    ?5:abs(footballer.ball_position[1]-footballer.y)/9;

            check= footballer.y + ((footballer.ball_position[1]-footballer.y)/footballer.step_y);
            if(check>225 & check<325){
                footballer.y = check+dis;
            }
        }
    }

    class GetBallPosition extends CyclicBehaviour {

        @Override
        public void action() {
            //Odbieranie pozycji piłki
            ACLMessage msg_of_ball = receive(mt_of_ball);
            if (msg_of_ball != null) {
                StringTokenizer st = new StringTokenizer(msg_of_ball.getContent(), ";");
                int i = 0;
                while (st.hasMoreTokens()) {
                    ball_position[i] = Integer.parseInt(st.nextToken());
                    i++;
                }
            }
        }
    }

    class GetFootballerPosition extends CyclicBehaviour{

        @Override
        public void action() {
            for(int i = 0; i< mt_footballers_position.size(); i++){
                ACLMessage msg = receive(mt_footballers_position.get(i));
                if (msg != null){
                    StringTokenizer st = new StringTokenizer(msg.getContent(), ";");
                    int position = 0;
                    while(st.hasMoreTokens()){
                        player_position[i][position] = Integer.parseInt(st.nextToken());
                        position++;
                    }
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
