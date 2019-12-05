package org.openstatic;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.Arrays;

import java.io.IOException;
import java.util.regex.Pattern;

import org.openstatic.lovense.*;

public class LovenseCLI extends BasicWindow implements LovenseConnectListener
{
    private Panel mainPanel;
    private Panel toyPanel;
    private static MultiWindowTextGUI gui;
    private LinkedHashMap<LovenseToy, LovenseToyPanel> toys;
    private boolean keep_running;
    private Thread mainThread = new Thread(() ->
    {
        while(this.keep_running)
        {
            try
            {
                Thread.sleep(1000);
                LovenseConnect.refreshIfNeeded();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    });

    public LovenseCLI()
    {
        super();
        // Create panel to hold components
        this.mainPanel = new Panel();
        this.toyPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        toyPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        this.keep_running = true;
        // Create window to hold the panel
        this.setComponent(this.mainPanel.withBorder(Borders.singleLine("Lovense Connect CLI Remote")));
        this.toys = new LinkedHashMap<LovenseToy, LovenseToyPanel>();
        LovenseConnect.addLovenseConnectListener(this);
        this.mainThread.start();
        this.mainPanel.addComponent(this.toyPanel);
        /*
        this.mainPanel.addComponent(new Button("Exit", new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }));
            */
    }
    
    public static void main(String[] args) throws IOException 
    {
        LovenseConnect.setDebug(false);
         // Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        LovenseCLI lcli = new LovenseCLI();
        // Create gui and start gui
        lcli.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));
        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));
        gui.addWindowAndWait(lcli);
    }
    
    public void toyAdded(int idx, LovenseToy toy)
    {
        LovenseToyPanel panel = new LovenseToyPanel(toy);
        this.toyPanel.addComponent(panel);
        panel.takeFocus();
        this.toys.put(toy, panel);
        updateScreen();
    }
    
    public static void updateScreen()
    {
        try
        {
            LovenseCLI.gui.updateScreen();
        } catch (Exception e) {
            
        }
    }

    public void toyRemoved(int idx, LovenseToy toy)
    {
        LovenseToyPanel panel = this.toys.get(toy);
        this.mainPanel.removeComponent(panel);
        this.toys.remove(toy);
        updateScreen();
    }
}
