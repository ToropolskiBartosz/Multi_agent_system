package simulation;

import javax.swing.*;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Simulation extends JFrame {
    Footballer[] playersHomeTeam;
    FootballerDefense[] playersDefenseHomeTeam;
    Goalkeeper goalkeeperHomeTeam;
    Ball ball;
    FootballerDefense[] playersDefenseVisitingTeam;
    Footballer[] playersVisitingTeam;
    Goalkeeper goalkeeperVisitingTeam;
    public Simulation(Footballer[] playersHomeTeam,
                      FootballerDefense[] playersDefenseHomeTeam,
                      Goalkeeper goalkeeperHomeTeam,
                      Footballer[] playersVisitingTeam,
                      FootballerDefense[] playersDefenseVisitingTeam,
                      Goalkeeper goalkeeperVisitingTeam,
                      Ball ball ) {

        this.playersHomeTeam = playersHomeTeam;
        this.playersDefenseHomeTeam = playersDefenseHomeTeam;
        this.goalkeeperHomeTeam = goalkeeperHomeTeam;
        this.ball = ball;
        this.playersVisitingTeam = playersVisitingTeam;
        this.playersDefenseVisitingTeam = playersDefenseVisitingTeam;
        this.goalkeeperVisitingTeam = goalkeeperVisitingTeam;

        setTitle("Prosta animacja");
        Animation animation = new Animation(this.playersHomeTeam,
                                            this.playersDefenseHomeTeam,
                                            this.goalkeeperHomeTeam,
                                            this.playersVisitingTeam,
                                            this.playersDefenseVisitingTeam,
                                            this.goalkeeperVisitingTeam,
                                            this.ball);
        javax.swing.Timer timer = new javax.swing.Timer(30,animation);
        timer.start();
        getContentPane().add(animation);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(200,150);
        setSize(1100,600);
        setVisible( true );
    }

    static public void main(String[] args){
        String homeTeam = "red";
        String visitingTeam = "blue";
        int size_of_a = 3;
        int size_of_d = 4;

        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        //profile.setParameter(Profile.GUI, "true");

        ContainerController containerController = runtime.createMainContainer(profile);

        AgentController footballerAgentController;
        Footballer[] playersHomeTeam = new Footballer[size_of_a];
        FootballerDefense[] playersDefenseHomeTeam = new FootballerDefense[size_of_d];
        Goalkeeper goalkeeperHomeTeam;

        Footballer[] playersVisitingTeam = new Footballer[size_of_a];
        FootballerDefense[] playersDefenseVisitingTeam = new FootballerDefense[size_of_d];
        Goalkeeper goalkeeperVisitingTeam;
        //------------------ TWORZENIE DRÓŻYNY CZERWONEJ
        for(int i=0; i< playersHomeTeam.length; i++){
            try {
                playersHomeTeam[i] = new Footballer((i+1)*135,490,(i+1),950,275,homeTeam ,visitingTeam);
                footballerAgentController = containerController.acceptNewAgent("FootballerHomeTeam"+i,playersHomeTeam[i]);
                footballerAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
        for(int i=0; i< playersDefenseHomeTeam.length; i++){
            try {
                playersDefenseHomeTeam[i] = new FootballerDefense((i+1)*105,250,(i+1),homeTeam,visitingTeam);
                footballerAgentController = containerController.acceptNewAgent("FootballerDefenseHomeTeam"+i,playersDefenseHomeTeam[i]);
                footballerAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        goalkeeperHomeTeam = new Goalkeeper(275,38,homeTeam,visitingTeam);
        try {
            footballerAgentController = containerController.acceptNewAgent("goalkeeper"+homeTeam,goalkeeperHomeTeam);
            footballerAgentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        //---------------------------------------------

        //--------------- TWORZENIE DRÓŻYNY NIEBIESKIEJ
        for(int i=0; i< playersVisitingTeam.length; i++){
            try {
                playersVisitingTeam[i] = new Footballer((i+1)*135,600,(i+1),130,275,visitingTeam,homeTeam);
                footballerAgentController = containerController.acceptNewAgent("FootballerVisitingTeam"+i,playersVisitingTeam[i]);
                footballerAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        for(int i=0; i< playersDefenseVisitingTeam.length; i++){
            try {
                playersDefenseVisitingTeam[i] = new FootballerDefense((i+1)*105,800,(i+1),visitingTeam,homeTeam);
                footballerAgentController = containerController.acceptNewAgent("FootballerDefenseVisitingTeam"+i,playersDefenseVisitingTeam[i]);
                footballerAgentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        goalkeeperVisitingTeam = new Goalkeeper(275,1020,visitingTeam,homeTeam);
        try {
            footballerAgentController = containerController.acceptNewAgent("goalkeeper"+visitingTeam,goalkeeperVisitingTeam);
            footballerAgentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        //-------------------------------

        //--------- TWORZENIE PIŁKI
        Ball ball = new Ball();
        try {
            footballerAgentController = containerController.acceptNewAgent("Ball",ball);
            footballerAgentController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

          new Simulation(playersHomeTeam,
                  playersDefenseHomeTeam,
                  goalkeeperHomeTeam,
                  playersVisitingTeam,
                  playersDefenseVisitingTeam,
                  goalkeeperVisitingTeam,
                  ball) ;

    }
}
