/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package src.engine.core;

import java.awt.EventQueue;
import javax.swing.JFrame;

/**
 *
 * @author dvano
 */
public final class SimpleProgram
{
    private SimpleProgram()
    {
    }

    private void initialize()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                SimpleFrame frame = new SimpleFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        new SimpleProgram().initialize();
    }
}
