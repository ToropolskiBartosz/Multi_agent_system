package simulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Animation extends JPanel implements ActionListener {
    Footballer[] playersHomeTeam;
    FootballerDefense[] playersDefenseHomeTeam;
    Goalkeeper goalkeeperHomeTeam;
    Ball ball;
    Footballer[] playersVisitingTeam;
    FootballerDefense[] playersDefenseVisitingTeam;
    Goalkeeper goalkeeperVisitingTeam;
    Animation(Footballer[] playersHomeTeam,
              FootballerDefense[] playersDefenseHomeTeam,
              Goalkeeper goalkeeperHomeTeam,
              Footballer[] playersVisitingTeam,
              FootballerDefense[] playersDefenseVisitingTeam,
              Goalkeeper goalkeeperVisitingTeam,
              Ball ball){
        this.playersHomeTeam = playersHomeTeam;
        this.playersDefenseHomeTeam = playersDefenseHomeTeam;
        this.goalkeeperHomeTeam = goalkeeperHomeTeam;
        this.ball = ball;
        this.playersDefenseVisitingTeam = playersDefenseVisitingTeam;
        this.playersVisitingTeam = playersVisitingTeam;
        this.goalkeeperVisitingTeam = goalkeeperVisitingTeam;
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        setBackground(Color.GREEN);
        Graphics2D g2D = (Graphics2D)g;
        g2D.setColor(Color.WHITE);
        //Wyrysowanie Boiska
        Stroke pioro2 = new BasicStroke(6);
        g2D.setStroke(pioro2);
        //Wymiary boiska
        g2D.drawRect(50, 25, 1000, 500);
        //Srodek boiska
        g2D.drawLine(525,25,525,525);
        g2D.drawOval(500,250,50,50);
        //Bramik
        g2D.drawRect(30, 225, 20, 100);
        g2D.drawRect(50, 175, 75, 200);
        g2D.drawRect(1050, 225, 20, 100);
        g2D.drawRect(975, 175, 75, 200);

        //Wyrysowanie zawodników dróżyny  czerwonej
        g2D.setColor(Color.RED);
        for(int i=0; i<playersHomeTeam.length;i++){
            g2D.fillOval(playersHomeTeam[i].x,playersHomeTeam[i].y,20,20);
        }
        for(int i=0; i<playersDefenseHomeTeam.length;i++){
            g2D.fillOval(playersDefenseHomeTeam[i].x,playersDefenseHomeTeam[i].y,20,20);
        }
        g2D.fillOval(goalkeeperHomeTeam.x,goalkeeperHomeTeam.y,20,20);

        //Wyrysowanie zawodników drózyny niebieskiej
        g2D.setColor(Color.BLUE);
        for(int i=0; i<playersVisitingTeam.length;i++){
            g2D.fillOval(playersVisitingTeam[i].x,playersVisitingTeam[i].y,20,20);
        }

        for(int i=0; i<playersDefenseVisitingTeam.length;i++){
            g2D.fillOval(playersDefenseVisitingTeam[i].x,playersDefenseVisitingTeam[i].y,20,20);
        }

        g2D.fillOval(goalkeeperVisitingTeam.x,goalkeeperVisitingTeam.y,20,20);

        //Wyrysowanie piłki
        g2D.setColor(Color.BLACK);
        g2D.fillOval(ball.x,ball.y,20,20);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
