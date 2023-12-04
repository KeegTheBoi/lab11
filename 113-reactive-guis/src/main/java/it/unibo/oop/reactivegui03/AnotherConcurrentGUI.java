package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton stopButton = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(stopButton);
        panel.add(up);
        panel.add(down);
        this.getContentPane().add(panel);
        this.setVisible(true);

        Agent agent = new Agent();
        new Thread(agent).start();
        AgentDisable agentDisable = new AgentDisable();
        Thread second = new Thread(agentDisable);
        second.start();

        try {
            second.join();
            agent.stopCounting();
        } catch (InterruptedException e) {
            
            e.printStackTrace();
        }

        stopButton.addActionListener(e -> {
            agent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false); 
        });

        up.addActionListener(e -> agent.setIncrement());
        down.addActionListener(e -> agent.setDecrement());
    }

    

    private class Agent implements Runnable {

        private volatile boolean stop;
        private volatile boolean up;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if(up) {
                        this.counter++;
                    } else {
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
        }

        public void setIncrement() {
            this.up = true;
        }

        public void setDecrement() {
            this.up = false;
        }
    }

     private class AgentDisable implements Runnable {


        @Override
        public void run() {
            
            try {
                // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                Thread.sleep(3000);
                SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(display.getText()));
                up.setEnabled(false);
                down.setEnabled(false); 

            } catch (InvocationTargetException | InterruptedException ex) {
                /*
                    * This is just a stack trace print, in a real program there
                    * should be some logging and decent error reporting
                    */
                ex.printStackTrace();
            }
            
        }
    }
}
