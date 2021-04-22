package simulation;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.SimpleBehaviour;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import static java.lang.Math.abs;

public class Footballer extends Agent{
    Behaviour move;
    int x, y;
    int step_x, step_y;
    int goal_x,goal_y;
    int position;
    String opponent;
    String team;
    boolean haveball;
    boolean[] flag = new boolean[2];
    boolean friendHaveBall;
    AID ball_aid;
    MessageTemplate mt_of_ball;
    int[] ball_position =new int[2];
    public Vector footballers = new Vector();
    public Vector footballersOpponent = new Vector();
    ACLMessage msg_Footballer;
    ACLMessage msg_FootballerOpponent;
    ArrayList<MessageTemplate> mt_footballers_position = new ArrayList<MessageTemplate>();
    ArrayList<MessageTemplate> mt_footballersOpponent_position = new ArrayList<MessageTemplate>();
    ArrayList<MessageTemplate> mt_footballers_have_ball = new ArrayList<MessageTemplate>();
    int[][] player_position = new int[2][2];
    int[][] playerOpponent_position = new int[7][2];

    public Footballer(int y,int x,int position,int goal_x,int goal_y, String team,String opponent){
        this.position=position;
        this.y = y;
        this.x = x;
        this.goal_x = goal_x;
        this.goal_y = goal_y;
        this.team=team;
        this.opponent = opponent;
    }

    protected void setup() {
        //---------------- REJESTRACJA DO DF ------------
        //Zapis do dróżyny
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("Footballer");
        sd1.setName(team);
        //Zapis na daną pozycje
        ServiceDescription sd2 = new ServiceDescription();
        sd2.setType("Footballer");
        sd2.setName("attack"+team);
        register(sd1,sd2);

        //---------------- WYSZUKANIE KOMPANÓW Z DRÓŻYNY ------
        footballers.clear();
        footballers = searchDF("Footballer","attack"+team);
        footballersOpponent = searchDF("Footballer",opponent);
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

        //--------------- DODANIE PIŁKARZY DO LISTY MAILINGOWEJ
        msg_Footballer = new ACLMessage(ACLMessage.INFORM);
        for (Object i: footballers) {
            msg_Footballer.addReceiver((AID) i);
        }

        //---------------- DODANIE FILTRA WIADOMOŚCI
        //filtry na piłke
        ball_aid = new AID( "Ball",AID.ISLOCALNAME);
        mt_of_ball =
                MessageTemplate.and(
                        MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                        MessageTemplate.MatchSender( ball_aid)) ;

        //------------ FILTRY DO POBIERANIA POZYCJI PIŁKARZY
        for(Object i: footballers){
            mt_footballers_position.add(MessageTemplate.and(
                                MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                                MessageTemplate.and(
                                    MessageTemplate.MatchSender( (AID) i ),
                                    MessageTemplate.MatchOntology("position"))));
        }

        for(Object i: footballersOpponent){
            mt_footballersOpponent_position.add(MessageTemplate.and(
                    MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                    MessageTemplate.and(
                            MessageTemplate.MatchSender( (AID) i ),
                            MessageTemplate.MatchOntology("position"))));
        }

        //filtrowanie na posiadanie piłki piłkarza
        for(Object i: footballers){
            mt_footballers_have_ball.add(MessageTemplate.and(
                    MessageTemplate.MatchPerformative( ACLMessage.INFORM ),
                    MessageTemplate.and(
                            MessageTemplate.MatchSender( (AID) i ),
                            MessageTemplate.MatchOntology("have_ball"))));
        }

        msg_Footballer.setContent(Integer.toString(x) +
                ";" + Integer.toString(y));
        send(msg_Footballer);

        //Odbieranie wiadomości
        addBehaviour(new GetBallPosition());
        addBehaviour(new GetFootballerPosition(mt_footballers_position,player_position));
        addBehaviour(new GetFootballerPosition(mt_footballersOpponent_position,playerOpponent_position));
        addBehaviour(new DoFootballerHaveBall());
        //Zmiana pozycji zawodnika
        //System.out.println(getLocalName()+" "+position+" "+y);
        move = new Move(this,200);
        addBehaviour(move);
        //removeBehaviour(move);
    }


    //Wyrejestrowanie się z DF kiedy agent umrzę
    protected void takeDown()
    {
        try { DFService.deregister(this); }
        catch (Exception e) {}
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
        //sd.setName("player");//Kolejna warstwa filtrowania osób
        //sd.setName("defender");
        //sd.setName("pomocnik");
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

    //------------------------
     class GetBallPosition extends CyclicBehaviour{

        @Override
        public void action() {
            //Odbieranie pozycji piłki
            ACLMessage msg_of_ball = receive(mt_of_ball);
            if (msg_of_ball != null) {
//                    System.out.println( "Otrzymałem wiadomość" +
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

    class GetFootballerPosition extends CyclicBehaviour{
        ArrayList<MessageTemplate> mt_footballers_position = new ArrayList<MessageTemplate>();
        int[][] player_position;

        GetFootballerPosition(ArrayList<MessageTemplate> mt_footballers_position,
                              int[][] player_position){
            this.mt_footballers_position = mt_footballers_position;
            this.player_position = player_position;
        }

        @Override
        public void action() {
            for(int i = 0; i< mt_footballers_position.size(); i++){
                ACLMessage msg = receive(mt_footballers_position.get(i));
                if (msg != null){
                    StringTokenizer st = new StringTokenizer(msg.getContent(), ";");
                    int position = 0;
                    //System.out.println("Odebrana pozycj: "+msg.getContent());
                    while(st.hasMoreTokens()){
                        player_position[i][position] = Integer.parseInt(st.nextToken());
                        position++;
                    }
                    //System.out.println(getLocalName()+" : "+"Pozycja zawodnika"+i+" :" + player_position[i][0] + " : "+ player_position[i][1]);
                }
            }
        }
    }

    class DoFootballerHaveBall extends CyclicBehaviour{

        @Override
        public void action() {
            for(int i = 0; i< mt_footballers_have_ball.size(); i++){
                ACLMessage msg = receive(mt_footballers_have_ball.get(i));
                if (msg != null){
                    boolean check = Boolean.parseBoolean(msg.getContent());
                    flag[i]=check;
                }
            }
            if(flag[0]|flag[1]){
                friendHaveBall=true;
            }else{
                friendHaveBall=false;
            }
        }
    }

}
