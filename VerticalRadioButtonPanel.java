package HaarTransform;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.ArrayList;

/**
 * Created by Jason on 5/27/2015.
 */
public class VerticalRadioButtonPanel extends JPanel{
    private ItemListener radioButtonListener;
    private ButtonGroup buttonGroup;
    private ArrayList<JRadioButton> radioButtons;
    private Color backgroundColor;
    private Color foregroundColor;

    public VerticalRadioButtonPanel(){
        this(new ButtonGroup());
    }

    public VerticalRadioButtonPanel(ButtonGroup buttonGroup){
        backgroundColor = Color.BLACK;
        foregroundColor = Color.LIGHT_GRAY;
        this.buttonGroup = buttonGroup;
        this.radioButtons = new ArrayList<>();
        this.setLayout(new GridBagLayout());
        this.setBorder(new EtchedBorder(EtchedBorder.RAISED, Color.CYAN, Color.BLUE));
    }

    public void removeAllRadioButtons() {
        for (JRadioButton theBtn : radioButtons) {
            this.remove(theBtn);
        }
        this.revalidate();
        this.repaint();
        radioButtons.clear();
    }

    public void addRadioButtons(int numOfButtons, String label){
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weighty = .05;
        c.gridy = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        for (int count = 0; count < numOfButtons; count++) {
            JRadioButton theRButton = new JRadioButton(label + " " + count);
            theRButton.setBackground(backgroundColor);
            theRButton.setForeground(foregroundColor);
            theRButton.addItemListener(radioButtonListener);
            buttonGroup.add(theRButton);
            radioButtons.add(theRButton);
            this.add(theRButton, c);
        }
    }

    public void setItemListener(ItemListener listener){
        radioButtonListener = listener;
    }

    public int indexOf(JRadioButton radioButton){
        return radioButtons.indexOf(radioButton);
    }

    public void selectButtonByIndex(int index){
        radioButtons.get(index).doClick();
    }

    @Override
    public void setBackground(Color newColor){
        backgroundColor = newColor;
        super.setBackground(newColor);
    }

    @Override
    public void setForeground(Color newColor){
        foregroundColor = newColor;
        super.setForeground(newColor);
    }

}
