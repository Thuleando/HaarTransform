package HaarTransform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;

/**
 * Created by Jason on 5/27/2015.
 */
public class ImageChooserUI {
    private String label;
    private Font basicFont;
    private JPanel pixelPanel;
    private JPanel stagePanel;
    private JLabel pixelLabel;
    private JLabel stageLabel;
    private VerticalRadioButtonPanel pixelImageChoicePanel;
    private VerticalRadioButtonPanel stageImageChoicePanel;

    ImageChooserUI(String label){
        this.label = label;
        basicFont = new Font("Arial", Font.BOLD, 16);
        pixelPanel = new JPanel();
        stagePanel = new JPanel();
        pixelLabel = new JLabel(label + " Pixel Expansions");
        stageLabel = new JLabel(label + " Stages");
        ButtonGroup buttonGroup = new ButtonGroup();
        pixelImageChoicePanel = new VerticalRadioButtonPanel(buttonGroup);
        stageImageChoicePanel = new VerticalRadioButtonPanel(buttonGroup);
        setupImageChooserUI();
    }

    private void setupImageChooserUI(){
        setupPixelPanel();
        setupStagePanel();
    }

    private void setupPixelPanel(){
        pixelPanel.setLayout(new GridBagLayout());
        pixelPanel.setBackground(Color.BLACK);
        GridBagConstraints c = new GridBagConstraints();

        pixelLabel.setFont(basicFont);
        pixelLabel.setForeground(Color.WHITE);
        pixelLabel.setPreferredSize(new Dimension(230, 30));
        c.gridy = 0;
        pixelPanel.add(pixelLabel, c);

        pixelImageChoicePanel.setPreferredSize(new Dimension(150, 300));
        pixelImageChoicePanel.setMinimumSize(new Dimension(150, 300));
        pixelImageChoicePanel.setBackground(Color.LIGHT_GRAY);
        pixelImageChoicePanel.setForeground(Color.BLACK);
        c.gridy = 1;
        pixelPanel.add(pixelImageChoicePanel, c);
    }

    private void setupStagePanel(){
        stagePanel.setLayout(new GridBagLayout());
        stagePanel.setBackground(Color.BLACK);
        GridBagConstraints c = new GridBagConstraints();

        stageLabel.setFont(basicFont);
        stageLabel.setForeground(Color.WHITE);
        stageLabel.setPreferredSize(new Dimension(230, 30));
        stageLabel.setHorizontalAlignment(JLabel.CENTER);
        c.gridy = 0;
        stagePanel.add(stageLabel, c);

        stageImageChoicePanel.setPreferredSize(new Dimension(150, 300));
        stageImageChoicePanel.setMinimumSize(new Dimension(150, 300));
        stageImageChoicePanel.setBackground(Color.LIGHT_GRAY);
        stageImageChoicePanel.setForeground(Color.BLACK);
        c.gridy = 1;
        stagePanel.add(stageImageChoicePanel, c);
    }

    public void clearPixelPanel(){
        pixelImageChoicePanel.removeAllRadioButtons();
    }

    public void clearStagePanel(){
        stageImageChoicePanel.removeAllRadioButtons();
    }

    public boolean isVisible(){
        return pixelPanel.isVisible() && stagePanel.isVisible();
    }
    public void setVisible(boolean choice){
        pixelPanel.setVisible(choice);
        stagePanel.setVisible(choice);
    }

    public JPanel getPixelPanel(){
        return pixelPanel;
    }

    public JPanel getStagePanel(){
        return stagePanel;
    }

    public void setPixelPanelListener(ItemListener listener){
        pixelImageChoicePanel.setItemListener(listener);
    }

    public void setStagePanelListener(ItemListener listener){
        stageImageChoicePanel.setItemListener(listener);
    }

    public int indexOfStageImage(JRadioButton radioButton){
        return stageImageChoicePanel.indexOf(radioButton);
    }

    public int indexOfPixelImage(JRadioButton radioButton){
        return pixelImageChoicePanel.indexOf(radioButton);
    }

    public void addRadioButtons(int numOfButtons){
        pixelImageChoicePanel.addRadioButtons(numOfButtons, label);
        stageImageChoicePanel.addRadioButtons(numOfButtons, label);
        pixelImageChoicePanel.revalidate();
        stageImageChoicePanel.revalidate();
    }

    public void manualSelectStageByIndex(int index){
        stageImageChoicePanel.selectButtonByIndex(index);
    }

    public void manualSelectPixelByIndex(int index){
        pixelImageChoicePanel.selectButtonByIndex(index);
    }
}
