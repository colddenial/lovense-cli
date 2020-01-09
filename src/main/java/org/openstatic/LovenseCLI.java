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
import org.apache.commons.cli.*;

public class LovenseCLI extends BasicWindow implements LovenseConnectListener
{
    private Panel mainPanel;
    private Panel toyPanel;
    private Label searchingLabel;
    private Button exitButton;
    private CommandLine commandLineOptions;

    private static MultiWindowTextGUI gui;
    private LinkedHashMap<LovenseToy, LovenseToyPanel> toys;
    private boolean keep_running;
    private boolean toys_found = false;
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

    public LovenseCLI(CommandLine cmdLine)
    {
        super();
        this.commandLineOptions = cmdLine;
        if (this.commandLineOptions != null)
        {
            if (this.commandLineOptions.hasOption("d"))
                LovenseConnect.setDebug(true);
            if (this.commandLineOptions.hasOption("h") && this.commandLineOptions.hasOption("p"))
            {
                int port = Integer.valueOf(this.commandLineOptions.getOptionValue('p',"34568")).intValue();
                String host = this.commandLineOptions.getOptionValue('h',"127.0.0.1");
                LovenseConnect.addDeviceManually(host, port);
            }
        }
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
        this.searchingLabel = new Label("Please Wait... Searching for toys");
        this.exitButton = new Button("Exit", new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            });
        this.toyPanel.addComponent(this.searchingLabel);
        this.toyPanel.addComponent(this.exitButton);
        this.exitButton.takeFocus();
    }
    
    public static void main(String[] args) throws IOException 
    {
        LovenseConnect.setDebug(false);
        CommandLine cmd = null;
        try
        {
            Options options = new Options();
            CommandLineParser parser = new DefaultParser();

            options.addOption(new Option("d", "debug", false, "Turn on debug."));
            options.addOption(new Option("?", "help", false, "Shows help"));
            options.addOption(new Option("h", "host", true, "Manually set a lovense connect host (https only)"));
            options.addOption(new Option("p", "port", true, "Specify HTTPS port for manual host"));

            cmd = parser.parse(options, args);

            if (cmd.hasOption("?"))
            {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "lovense-cli", options );
                System.exit(0);
            }
        } catch (Exception e) {
            
        }
        
        
         // Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        LovenseCLI lcli = new LovenseCLI(cmd);
        // Create gui and start gui
        lcli.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));
        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));
        gui.addWindowAndWait(lcli);
    }
    
    public void toyAdded(int idx, LovenseToy toy)
    {
        if (!toys_found)
        {
            this.toyPanel.removeComponent(this.searchingLabel);
            this.toyPanel.removeComponent(this.exitButton);
            toys_found = true;
        }
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
        this.toyPanel.removeComponent(panel);
        this.toys.remove(toy);
        updateScreen();
    }
}
