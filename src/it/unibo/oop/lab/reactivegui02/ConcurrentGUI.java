package it.unibo.oop.lab.reactivegui02;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class ConcurrentGUI extends JFrame {
	 private static final long serialVersionUID = 1L;
     private static final double WIDTH_PERC = 0.2;
     private static final double HEIGHT_PERC = 0.1;
     private final JLabel display = new JLabel();
     private final JButton stop = new JButton("stop");
     private final JButton up = new JButton("up");
     private final JButton down = new JButton("down");

     /**
      * Builds a new CGUI.
      */
     public ConcurrentGUI() {
         super();
         final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
         this.setDefaultCloseOperation(EXIT_ON_CLOSE);
         final JPanel panel = new JPanel();
         panel.add(display);
         panel.add(up);
         panel.add(down);
         panel.add(stop);
         this.getContentPane().add(panel);
         this.setVisible(true);
         /*
          * Create the counter agent and start it. This is actually not so good:
          * thread management should be left to
          * java.util.concurrent.ExecutorService
          */
         final Agent agent = new Agent();
         new Thread(agent).start();
         /*
          * Register a listener that stops it
          */
         stop.addActionListener(new ActionListener() {
             /**
              * event handler associated to action event on button stop.
              * 
              * @param e
              *            the action event that will be handled by this listener
              */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 // Agent should be final
                 agent.stopCounting();
             }
         });
         up.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					agent.upCounting();
				}
         
         });
         down.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					agent.downCounting();
				}
         	
         });
     }
     private class Agent implements Runnable {
         /*
          * Stop is volatile to ensure visibility. Look at:
          * 
          * http://archive.is/9PU5N - Sections 17.3 and 17.4
          * 
          * For more details on how to use volatile:
          * 
          * http://archive.is/4lsKW
          * 
          */
         private volatile boolean stop;
         private volatile int counter;
         private volatile boolean direction=true;

         @Override
         public void run() {
             while (!this.stop) {
                 try {
                     /*
                      * All the operations on the GUI must be performed by the
                      * Event-Dispatch Thread (EDT)!
                      */
                     SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(Integer.toString(Agent.this.counter)));
                     if(this.direction) {
                     	this.counter++;
                     }else {
                     	this.counter--;
                     }
                     Thread.sleep(100);
                 } catch (InvocationTargetException | InterruptedException ex) {
                     /*
                      * This is just a stack trace print, in a real program there
                      * should be some logging and decent error reporting
                      */
                     ex.printStackTrace();
                 }
             }
         }

         /**
          * External command to stop counting.
          */
         public void stopCounting() {
             this.stop = true;
             up.setEnabled(false);
             down.setEnabled(false);
         }
         
         public void upCounting() {
         	if(!this.direction) {
         		direction=true;
         	}
         }
         
         public void downCounting() {
         	if(this.direction) {
         		direction=false;
         	}
         }
     }
}