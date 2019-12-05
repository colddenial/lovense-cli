package org.openstatic;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.regex.Pattern;

import org.openstatic.lovense.*;

public class LovenseToyPanel extends Panel implements LovenseToyListener
{
    public LovenseToy toy;
    public Label batteryLabel;
    public ScrollBar vibrateBar;
    public Border border;
    public Label toyLabel;
    public TextBox focusBox;
    public int vibrateIntention;
    
    public LovenseToyPanel(LovenseToy toy)
    {
        this.toy = toy;
        this.toy.addLovenseToyListener(this);
        this.setPreferredSize(new TerminalSize(12, 12));
        this.toyLabel = new Label(this.toy.getNickname());
        this.batteryLabel = new Label("battery 0%");
        this.vibrateBar = new ScrollBar(Direction.VERTICAL);
        this.vibrateBar.setScrollMaximum(20);
        this.vibrateBar.setViewSize(1);
        this.vibrateBar.setPreferredSize(new TerminalSize(1, 8));
        this.vibrateBar.setScrollPosition(20);
        this.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        this.border = Borders.singleLine(this.toy.getNickname());
        this.addComponent(this.vibrateBar);
        this.addComponent(this.toyLabel);
        this.addComponent(this.batteryLabel);
        this.focusBox = new TextBox("vibrate 0")
        {
            public Interactable.Result handleKeyStroke(KeyStroke keyStroke)
            {
                if (keyStroke.getKeyType() == KeyType.ArrowUp)
                {
                    LovenseToyPanel.this.vibrateIntention++;
                    if (LovenseToyPanel.this.vibrateIntention > 20) LovenseToyPanel.this.vibrateIntention = 20;
                    LovenseToyPanel.this.toy.vibrate(LovenseToyPanel.this.vibrateIntention);
                    LovenseToyPanel.this.focusBox.setText("vibrate " + String.valueOf(LovenseToyPanel.this.vibrateIntention));
                    //LovenseCLI.updateScreen();
                    return Interactable.Result.HANDLED;
                } else if (keyStroke.getKeyType() == KeyType.ArrowDown) {
                    LovenseToyPanel.this.vibrateIntention--;
                    if (LovenseToyPanel.this.vibrateIntention < 0) LovenseToyPanel.this.vibrateIntention = 0;
                    LovenseToyPanel.this.toy.vibrate(LovenseToyPanel.this.vibrateIntention);
                    LovenseToyPanel.this.focusBox.setText("vibrate " + String.valueOf(LovenseToyPanel.this.vibrateIntention));
                    //LovenseCLI.updateScreen();
                    return Interactable.Result.HANDLED;
                } else if (keyStroke.getKeyType() == KeyType.Escape) {
                    System.exit(0);
                    return Interactable.Result.HANDLED;
                } else {
                    return super.handleKeyStroke(keyStroke);
                }
            }
        };
        this.focusBox.setReadOnly(true);
        this.addComponent(this.focusBox);
    }
    
    public void toyUpdated(LovenseToy toy)
    {
        LovenseToyPanel.this.vibrateBar.setScrollPosition(20-toy.getOutputOneValue());
        this.batteryLabel.setText("battery " + String.valueOf(toy.getBattery()) + "%");
    }

    public void takeFocus()
    {
        this.focusBox.takeFocus();
    }
}
