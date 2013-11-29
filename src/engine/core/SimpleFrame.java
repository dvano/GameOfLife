package src.engine.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author dvano
 */
public final class SimpleFrame extends JFrame
{
    protected SimpleFrame()
    {
        this.setTitle("GameOfLife");
        this.setSize(600, 400);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        final SimpleComponent component = new SimpleComponent();
        component.startThread_1();
        this.add(component);
        
        JToolBar toolBar = new JToolBar();
        
        JButton addCell = new JButton("ADD");
        JButton removeCell = new JButton("REMOVE");
        
        addCell.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                component.tool = Tool.ADD_CELL;
            }
        });
        removeCell.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                component.tool = Tool.REMOVE_CELL;
            }
        });
        
        toolBar.add(addCell);
        toolBar.add(removeCell);
        
        this.add(toolBar, BorderLayout.NORTH);
        
        JMenuBar menuBar = new JMenuBar();
        
        JMenu game = new JMenu("игра");
        
        final JMenuItem stopGame = new JMenuItem("закончить текущую игру");
        final JMenuItem pauseGame = new JMenuItem("приостановить текущую игру");
        final JMenuItem unpauseGame = new JMenuItem("возобновить текущую игру");
        final JMenuItem loadGame = new JMenuItem("загрузить мир");
        final JMenuItem saveGame = new JMenuItem("сохранить мир");
        
        pauseGame.setEnabled(false);
        
        stopGame.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                pauseGame.setEnabled(false);
                unpauseGame.setEnabled(true);
                component.deleteWorld();
                component.stopGame();
            }
        });
        pauseGame.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                pauseGame.setEnabled(false);
                unpauseGame.setEnabled(true);
                component.stopGame();
            }
        });
        unpauseGame.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                pauseGame.setEnabled(true);
                unpauseGame.setEnabled(false);
                component.startGame();
            }
        });

        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("файлы мира", "dat"));

        loadGame.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                int show = fileChooser.showOpenDialog(SimpleFrame.this);
                if (show == JFileChooser.APPROVE_OPTION)
                {
                    String resource = fileChooser.getSelectedFile().getAbsolutePath();
                    component.loadWorld(resource);
                }
            }
        });
        saveGame.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                int show = fileChooser.showSaveDialog(SimpleFrame.this);
                if (show == JFileChooser.APPROVE_OPTION)
                {
                    String resource = fileChooser.getSelectedFile().getAbsolutePath();
                    component.saveWorld(resource);
                }
            }
        });
        
        game.add(stopGame);
        game.addSeparator();
        game.add(pauseGame);
        game.add(unpauseGame);
        game.addSeparator();
        game.add(loadGame);
        game.add(saveGame);
        
        menuBar.add(game);
        
        this.setJMenuBar(menuBar);
    }

    private final class SimpleComponent extends JComponent
    {
        public static final int CELL_SIZE = 10;

        public Tool tool = Tool.ADD_CELL;

        private final boolean[][][] previousCells = new boolean[1000000][1000000][2];

        private volatile boolean running = false;
        
        private SimpleComponent()
        {
            Mouse mouse = new Mouse();
            this.addMouseMotionListener(mouse);
        }

        public void startThread_1()
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (true)
                    {
                        if (SimpleComponent.this.running)
                        {
                            SimpleComponent.this.updateCells();
                            SimpleComponent.this.repaint();
                            try
                            {
                                Thread.sleep(100L);
                            }
                            catch (InterruptedException ex)
                            {
                                Logger.getLogger(SimpleFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            });
            thread.start();
        }

        public void startGame()
        {
            this.running = true;
            this.repaint();
        }

        public void stopGame()
        {
            this.running = false;
            this.repaint();
        }

        public void generateMap(int coff)
        {
            Random random = new Random();
            for (int i = 0; i < this.previousCells.length; i++)
            {
                for (int j = 0; j < this.previousCells[i].length; j++)
                {
                    int num = random.nextInt(coff);
                    if (num == coff / 2) this.previousCells[i][j][0] = true;
                }
            }
            this.repaint();
        }

        public void deleteWorld()
        {
            for (int i = 0; i < this.previousCells.length; i++)
            {
                for (int j = 0; j < this.previousCells[i].length; j++)
                {
                    this.previousCells[i][j][0] = false;
                    this.previousCells[i][j][1] = false;
                }
            }
        }

        public synchronized void updateCells()
        {
            for (int i = 0; i < this.previousCells.length; i++)
            {
                for (int j = 0; j < this.previousCells[i].length; j++)
                {
                    int index = this.getIndices(i, j);
                    if (!this.previousCells[i][j][0] && index == 3) this.previousCells[i][j][1] = true;
                    if (this.previousCells[i][j][0] && index == 2 || index == 3) this.previousCells[i][j][1] = true;
                    if (this.previousCells[i][j][0] && index < 2 || index > 3) this.previousCells[i][j][1] = false;
                }
            }
            for (int i = 0; i < this.previousCells.length; i++)
            {
                for (int j = 0; j < this.previousCells[i].length; j++)
                {
                    this.previousCells[i][j][0] = this.previousCells[i][j][1];
                }
            }
        }
        
        public void loadWorld(String resource)
        {
            this.deleteWorld();
            try
            {
                BufferedReader in = new BufferedReader(new FileReader(resource));
                String line;
                while ((line = in.readLine()) != null)
                {
                    int x = Integer.valueOf(line.split(" ")[2]);
                    int y = Integer.valueOf(line.split(" ")[5]);
                    this.previousCells[x][y][0] = true;
                }
                in.close();
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(SimpleFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
                Logger.getLogger(SimpleFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.repaint();
        }
        
        public void saveWorld(String resource)
        {
            try
            {
                PrintWriter out = new PrintWriter(new FileWriter(resource), true);
                for (int i = 0; i < this.previousCells.length; i++)
                {
                    for (int j = 0; j < this.previousCells[i].length; j++)
                    {
                        if (this.previousCells[i][j][0]) out.println("x = " + i + " y = " + j);
                    }
                }
                out.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(SimpleFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public int getIndices(int i, int j)
        {
            int index = 0;
            if (i < this.previousCells.length - 1 && this.previousCells[i + 1][j][0]) index++;
            if (j < this.previousCells[i].length - 1 && this.previousCells[i][j + 1][0]) index++;
            if (i < this.previousCells.length - 1 && j < this.previousCells[i].length - 1 && this.previousCells[i + 1][j + 1][0]) index++;
            if (i > 0 && this.previousCells[i - 1][j][0]) index++;
            if (j > 0 && this.previousCells[i][j - 1][0]) index++;
            if (i > 0 && j > 0 && this.previousCells[i - 1][j - 1][0]) index++;
            if (i > 0 && j < this.previousCells[i].length - 1 && this.previousCells[i - 1][j + 1][0]) index++;
            if (i < this.previousCells.length - 1 && j > 0 && this.previousCells[i + 1][j - 1][0]) index++;
            return index;
        }

        @Override
        public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;
            for (int i = 0; i < this.previousCells.length; i++)
            {
                for (int j = 0; j < this.previousCells[i].length; j++)
                {
                    if (this.previousCells[i][j][0])
                    {
                        g2.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                    g2.setColor(Color.LIGHT_GRAY);
                    if (!this.previousCells[i][j][0])
                    {
                        g2.drawRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                    g2.setColor(Color.BLACK);
                }
            }
        }

        private final class Mouse extends MouseAdapter
        {
            @Override
            public void mouseDragged(MouseEvent me)
            {
                int x = me.getX();
                int y = me.getY();
                for (int i = 0; i < SimpleComponent.this.previousCells.length; i++)
                {
                    for (int j = 0; j < SimpleComponent.this.previousCells[i].length; j++)
                    {
                        Rectangle2D b1 = new Rectangle2D.Float(i * SimpleComponent.CELL_SIZE, j * SimpleComponent.CELL_SIZE, SimpleComponent.CELL_SIZE, SimpleComponent.CELL_SIZE);
                        Rectangle2D b2 = new Rectangle2D.Float(x, y, 1.0F, 1.0F);
                        if (b1.intersects(b2))
                        {
                            if (SimpleComponent.this.tool.equals(Tool.ADD_CELL))
                            {
                                SimpleComponent.this.previousCells[i][j][0] = true;
                            }
                            else if (SimpleComponent.this.tool.equals(Tool.REMOVE_CELL))
                            {
                                SimpleComponent.this.previousCells[i][j][0] = false;
                            }
                        }
                    }
                }
                SimpleComponent.this.repaint();
            }
        }
    }
}
