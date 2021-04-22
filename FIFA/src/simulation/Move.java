package simulation;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Random;
import static java.lang.Math.abs;
import static java.lang.Math.random;

class Move extends TickerBehaviour {
    int dis=10;
    int directive=1;
    int step =5;
    int hold =0;
    Footballer footballer;

    public Move(Footballer a, long period) {
        super(a, period);
        footballer=a;
    }

    @Override
    protected void onTick() {

        dis=0;
        dis=checkDis();

        if(footballer.haveball | footballer.friendHaveBall){
            moveGoal(dis);
        }else{
            moveBall(dis);
        }


        if((abs(footballer.ball_position[0]-footballer.x)<=25
                & abs(footballer.ball_position[1]-footballer.y)<=25)  ) {
            //Tworzenie wiadomości do piłki
            ACLMessage ball_msg = new ACLMessage(ACLMessage.INFORM);
            ball_msg.addReceiver(footballer.ball_aid);
            //Informacja, że piłkarz ma piłke
            hold++;
            footballer.haveball=true;

            //MOGĄ BYĆ PROBLEMY
            if(footballer.x == abs(footballer.goal_x)) {
                ball_msg.setContent(Integer.toString(footballer.goal_x)
                        + ";" + Integer.toString(footballer.goal_y));
                ball_msg.setOntology("Kick");
            }else if(hold<25){
                ball_msg.setContent(Integer.toString(footballer.x)
                        + ";" + Integer.toString(footballer.y));
                ball_msg.setOntology("drybling");
            }else if(hold>25){
                Random rand = new Random();
                int random_position = rand.nextInt(2);
                ball_msg.setContent(Integer.toString(footballer.player_position[random_position][0])
                        + ";" + Integer.toString(footballer.player_position[random_position][1]));
                ball_msg.setOntology("pass");
            }

            footballer.send(ball_msg);
            //------
        }else{
            footballer.haveball=false;
            hold=0;
        }
        //Wysyłanie swojej pozycji zawodniką
        footballer.msg_Footballer.setContent(Integer.toString(footballer.x) + ";" + Integer.toString(footballer.y));
        footballer.msg_Footballer.setOntology("position");
        footballer.send(footballer.msg_Footballer);

        footballer.msg_Footballer.setContent(Boolean.toString(footballer.haveball));
        footballer.msg_Footballer.setOntology("have_ball");
        footballer.send(footballer.msg_Footballer);
        //if(footballer.position==2)System.out.println(footballer.friendHaveBall);
    }

    public void moveBall(int d){
        int dis =d;
        int check;

        //Bieganie za piłką
        //step_y=(ball_position[1]-y<50)?(ball_position[1]-y<30)?3:5:8;
        footballer.step_y=abs(footballer.ball_position[1]-footballer.y)/9<=3
                ?5:abs(footballer.ball_position[1]-footballer.y)/9;
        //step_x=(ball_position[0]-x<50)?step_x=(ball_position[0]-x<30)?3:5:8;
        //Dynamiczna zmiana ilości kroków zależna od odległości(cały casz ma być 8 kroków)
        footballer.step_x=abs(footballer.ball_position[0]-footballer.x)/9<=3
                ?5:abs(footballer.ball_position[0]-footballer.x)/9;

        check = footballer.x + ((footballer.ball_position[0]-footballer.x)/footballer.step_x);

        if(footballer.goal_x>500) {
            if (check <= abs(footballer.goal_x) & check>410) footballer.x = check;

        }else{

            if (check >= abs(footballer.goal_x) & check<610) footballer.x = check;
        }

        switch(footballer.position){
            case 1:
                check= footballer.y + ((footballer.ball_position[1]-footballer.y)/footballer.step_y);
                if(check<170 & check>25){
                    footballer.y = check+dis;
                }
                break;
            case 2:
                check= footballer.y + ((footballer.ball_position[1]-footballer.y)/footballer.step_y);
                if(check>190 & check<330){
                    footballer.y = check;
                }
                break;
            case 3:
                check= footballer.y + ((footballer.ball_position[1]-footballer.y)/footballer.step_y);
                if(check>350 & check<525){
                    footballer.y = check+dis;
                }
                break;
        }
    }
    public void moveGoal(int d){
//        footballer.x = footballer.x + (step * directive);
//        if(footballer.x >=1000-30) directive = -1;
//        if(footballer.x <=0) directive = 1;
        int dis =d;
        //int stop_dis = 100;
        int check;

        footballer.step_y=abs(footballer.goal_y-footballer.y)/9<=2?1:abs(footballer.goal_y-footballer.y)/9;
        footballer.step_x=abs(footballer.goal_x-footballer.x)/7<=2 ?1:abs(footballer.goal_x-footballer.x)/7;

        footballer.x = footballer.x + ((footballer.goal_x-footballer.x)/footballer.step_x);
//        if(check<=abs(footballer.goal_x-125)) {footballer.x = check;
//        }else if(check>abs(footballer.goal_x-125)){
//            footballer.x = abs(footballer.goal_x-125);
//        }
        switch(footballer.position){
            case 1:
                check= footballer.y + ((275-footballer.y)/footballer.step_y);
                check = dribble(check);
                if(check<170 & check>25){
                    footballer.y = check+dis;
                }
                break;
            case 2:
                check= footballer.y + ((275-footballer.y)/footballer.step_y);
                check = dribble(check);
                if(check>190 & check<330){
                    footballer.y = check;
                }
                break;
            case 3:
                check= footballer.y + ((275-footballer.y)/footballer.step_y);
                check = dribble(check);
                if(check>350 & check<525){
                    footballer.y = check+dis;
                }
                break;
        }

    }
    public int checkDis(){
        int dis=0;
        for(int i=0;i<footballer.player_position.length;i++ ) {
            if (abs(footballer.x - footballer.player_position[i][0]) <= 30
                    & abs(footballer.y - footballer.player_position[i][1]) <= 30){
                if (footballer.y > footballer.player_position[i][1]) dis = 8;
                if (footballer.y < footballer.player_position[i][1]) dis = (-8);
            }
        }
        //if(stan1&stan2){ dis=0;}
        return dis;
    }
    public int dribble(int check){
        for(int i=0;i<footballer.playerOpponent_position.length;i++ ) {
            if (abs(footballer.y - footballer.playerOpponent_position[i][1]) <= 25 &
                    abs(footballer.x - footballer.playerOpponent_position[i][0]) <= 105){
                Random rand = new Random();
                int dribbleTarget = rand.nextInt(2) * 2 - 1;
                check= footballer.y + (8*dribbleTarget);
            }
        }
        return check;
    }

}
