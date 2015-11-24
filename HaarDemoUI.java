package HaarTransform;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import static javax.swing.UIManager.*;

/**
 * Created by Jason on 5/26/2015.
 */
public class HaarDemoUI extends JFrame {
    private static final int DEFAULT_MIN_SIZE = 800;
    private static final int DEFAULT_PREFERRED_SIZE = 900;
    private static final int SCREEN_WIDTH_OFFSET = 500;
    private static final int SCREEN_HEIGHT_OFFSET = 300;
    private static final int INITIAL_STAGE_SHOWN = 2;
    private static final ImageIcon DEFAULT_IMAGE = new ImageIcon("default.png");

    private final HaarDemo program;
    private final JFrame baseUIFrame = this;
    private SwingWorker currWorker;
    private int screenHeight, screenWidth;
    private JPanel mainWindow;
    private JButton browseButton;
    private JButton loadButton;
    private JButton cancelButton;
    private JButton transformButton;
    private JButton recoverButton;
    private JProgressBar progressBar;
    private JScrollPane scrollImg;
    private JLabel displayedImg;
    private Icon baseImage;
    private JFileChooser fileChooser;
    private JTextField origFileField;
    private JCheckBox fileGenerationToggle;
    private ImageChooserUI transformImageChoicePanel;
    private ImageChooserUI recoverImageChoicePanel;

    public HaarDemoUI() {
        this.program = new HaarDemo();
        setScreenHeightAndWidth();
        initializeUIFrameSettings();
        setupUI();
        addListeners();
        this.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        new HaarDemoUI();
    }

    private void setScreenHeightAndWidth() {
        screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    }

    private void initializeUIFrameSettings() {
        baseUIFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        baseUIFrame.setPreferredSize(new Dimension(DEFAULT_PREFERRED_SIZE, DEFAULT_PREFERRED_SIZE));
        baseUIFrame.setMinimumSize(new Dimension(DEFAULT_MIN_SIZE, DEFAULT_MIN_SIZE));
        baseUIFrame.setLayout(new BorderLayout());
    }

    private void setupUI() {
        setupMainWindow();
        baseUIFrame.pack();
    }

    private void setupMainWindow() {
        mainWindow = new JPanel();
        mainWindow.setLayout(new GridBagLayout());
        mainWindow.setBackground(Color.BLACK);

        setupBanner();
        setupGenerateFilesPanel();
        setupChooseFilePanel();
        setupExecutionButtons();
        setupProgressBar();
        setupImageDisplay();
        setupTransformElements();
        setupRecoveryElements();

        baseUIFrame.getContentPane().add(mainWindow, BorderLayout.CENTER);
    }

    private void setupBanner() {
        JLabel banner = new JLabel("HAAR Transform Demo");
        banner.setFont(new Font("Arial", Font.BOLD, 20));
        banner.setForeground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weighty = .2;
        mainWindow.add(banner, c);
    }

    private void setupGenerateFilesPanel() {
        JPanel toggleLoadPanel = new JPanel();
        toggleLoadPanel.setLayout(new GridBagLayout());
        toggleLoadPanel.setBackground(Color.BLACK);

        fileGenerationToggle = new JCheckBox("Generate image files for each stage");
        fileGenerationToggle.setBackground(Color.BLACK);
        fileGenerationToggle.setForeground(Color.WHITE);
        fileGenerationToggle.setBorder(null);
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 0;
        c.gridwidth = 1;
        c.weighty = 0;
        toggleLoadPanel.add(fileGenerationToggle, c);

        loadButton = new JButton("Load Image");
        cancelButton = new JButton("Cancel");
        cancelButton.setVisible(false);
        c = new GridBagConstraints();
        c.gridy = 1;
        toggleLoadPanel.add(loadButton, c);
        toggleLoadPanel.add(cancelButton, c);

        c = new GridBagConstraints();
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        mainWindow.add(toggleLoadPanel, c);
    }

    private void setupChooseFilePanel() {
        JPanel chooseFilePanel = new JPanel();
        chooseFilePanel.setLayout(new GridBagLayout());
        chooseFilePanel.setBackground(Color.BLACK);

        Font basicFont = new Font("Arial", Font.BOLD, 16);
        JLabel origLabel = new JLabel("Picture File Name: ");
        origLabel.setFont(basicFont);
        origLabel.setForeground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridx = 0;
        chooseFilePanel.add(origLabel, c);

        try{ setLookAndFeel(getSystemLookAndFeelClassName());}
        catch(Exception ex){}
        fileChooser = new JFileChooser();
        try{ setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");}
        catch(Exception ex){}
        origFileField = new JTextField();
        origFileField.setFont(basicFont);
        origFileField.setPreferredSize(new Dimension(200, 30));
        origFileField.setMinimumSize(new Dimension(100, 30));

        c = new GridBagConstraints();
        c.gridx = 1;
        chooseFilePanel.add(origFileField, c);

        browseButton = new JButton("browse");
        c = new GridBagConstraints();
        c.gridx = 2;
        c.insets =  new Insets(0, 5, 0, 0);
        chooseFilePanel.add(browseButton, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        mainWindow.add(chooseFilePanel, c);
    }

    private void setupExecutionButtons() {
        transformButton = new JButton("Transform");
        transformButton.setEnabled(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.insets = new Insets(5, 0, 5, 0);
        mainWindow.add(transformButton, c);

        recoverButton = new JButton("Recover");
        recoverButton.setVisible(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.insets = new Insets(5, 0, 5, 0);
        mainWindow.add(recoverButton, c);
    }

    private void setupProgressBar(){
        try{ setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");}
        catch(Exception ex){System.out.println("Progressbar can't load MetalLF");}
        progressBar = new JProgressBar(0,100);
        try{ setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");}
        catch(Exception ex){System.out.println("Progressbar can't load NimbusLF");}
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.weighty = .2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 0, 0, 0);
        c.anchor = GridBagConstraints.NORTH;
        mainWindow.add(progressBar, c);
    }

    private void setupImageDisplay() {
        JPanel imgPanel = new JPanel();
        imgPanel.setLayout(new BorderLayout());
        imgPanel.setBackground(Color.BLACK);

        baseImage = DEFAULT_IMAGE;
        displayedImg = new JLabel(DEFAULT_IMAGE);
        imgPanel.add(displayedImg, BorderLayout.CENTER);

        scrollImg = new JScrollPane(imgPanel);
        scrollImg.setBorder(new EtchedBorder(EtchedBorder.RAISED, Color.CYAN, Color.BLUE));
        scrollImg.setWheelScrollingEnabled(true);
        scrollImg.setPreferredSize(new Dimension(DEFAULT_IMAGE.getIconHeight() + 10, DEFAULT_IMAGE.getIconWidth() + 10));
        scrollImg.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollImg.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.weighty = .2;
        c.insets = new Insets(30, 0, 0, 0);
        c.anchor = GridBagConstraints.NORTH;
        mainWindow.add(scrollImg, c);    }

    private void setupTransformElements() {
        transformImageChoicePanel = new ImageChooserUI("Transform");

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.NORTH;
        mainWindow.add(transformImageChoicePanel.getPixelPanel(), c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;
        c.anchor = GridBagConstraints.NORTH;
        mainWindow.add(transformImageChoicePanel.getStagePanel(), c);
    }

    private void setupRecoveryElements() {
        recoverImageChoicePanel = new ImageChooserUI("Recovery");
        recoverImageChoicePanel.setVisible(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.weighty = 0;
        c.anchor = GridBagConstraints.NORTH;
        mainWindow.add(recoverImageChoicePanel.getPixelPanel(), c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;
        c.anchor = GridBagConstraints.NORTH;
        mainWindow.add(recoverImageChoicePanel.getStagePanel(), c);
    }

    private void addListeners() {
        loadButton.addActionListener(new LoadButtonHandler());
        cancelButton.addActionListener(new CancelButtonHandler());
        transformButton.addActionListener(new TransformButtonHandler());
        recoverButton.addActionListener(new RecoverButtonHandler());
        browseButton.addActionListener(new BrowseButtonHandler());
        origFileField.addKeyListener(new FileInputHandler());
        transformImageChoicePanel.setPixelPanelListener(new TransformPixelSelectionHandler());
        transformImageChoicePanel.setStagePanelListener(new TransformStageSelectionHandler());
        recoverImageChoicePanel.setPixelPanelListener(new RecoverPixelSelectionHandler());
        recoverImageChoicePanel.setStagePanelListener(new RecoverStageSelectionHandler());
    }

    private class LoadButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            resetUIForCurrentImage();
            try {
                loadImageAndAdjustUI();
                transformButton.setEnabled(true);
            } catch (TransformException ex) {
                resetUIToDefault();
                JOptionPane.showMessageDialog(rootPane, ex.getMessage(), "ERROR: Invalid file",
                        JOptionPane.PLAIN_MESSAGE);
                origFileField.selectAll();
            }
        }
    }

    private class CancelButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            currWorker.cancel(true);
            displayedImg.setIcon(baseImage);
            resetUIForCurrentImage();
            transformButton.setEnabled(true);
        }
    }

    private void resetUIForCurrentImage(){
        transformImageChoicePanel.clearPixelPanel();
        transformImageChoicePanel.clearStagePanel();
        program.clearTransformImages();
        transformButton.setEnabled(false);

        if (windowNeedsResetToDefault()) {
            displayDefaultWindow();
        } else {
            recoverButton.setVisible(false);
            transformButton.setVisible(true);
        }
    }
    private void resetUIToDefault() {
        resetUIForCurrentImage();
        displayedImg.setIcon(DEFAULT_IMAGE);
        setSizeOfImageScrollableDisplay(DEFAULT_IMAGE);

    }

    private Boolean windowNeedsResetToDefault() {
        return transformButton.isVisible();
    }

    private void displayDefaultWindow() {
        transformImageChoicePanel.setVisible(true);
        recoverImageChoicePanel.setVisible(false);
    }

    private void loadImageAndAdjustUI() throws TransformException {
        ImageIcon newImage = program.loadImage(origFileField.getText());
        baseImage = newImage;
        displayedImg.setIcon(newImage);
        setSizeOfImageScrollableDisplay(displayedImg.getIcon());
    }

    private void setSizeOfImageScrollableDisplay(Icon image) {
        int scrollHeight, scrollWidth;
        boolean heightOverflow = false;
        boolean widthOverflow = false;
        final int DISPLAY_AREA_PADDING = 10;
        final int SCROLL_BAR_PADDING = 11;

        if (imageTooWide(image)) {
            scrollWidth = screenWidth - SCREEN_WIDTH_OFFSET;
            widthOverflow = true;
        }
        else {
            scrollWidth = image.getIconWidth() + DISPLAY_AREA_PADDING;
        }

        if (imageTooTall(image)) {
            scrollHeight = screenHeight - SCREEN_HEIGHT_OFFSET;
            heightOverflow = true;
        }
        else {
            scrollHeight = image.getIconHeight() + DISPLAY_AREA_PADDING;
        }

        if (widthOverflow) {
            scrollHeight += SCROLL_BAR_PADDING;
        }

        if (heightOverflow) {
            scrollWidth += SCROLL_BAR_PADDING;
        }

        scrollImg.setPreferredSize(new Dimension(scrollWidth, scrollHeight));
        resizeWindowToImage(image);
    }

    private boolean imageTooWide(Icon image) {
        return image.getIconWidth() + SCREEN_WIDTH_OFFSET > screenWidth;
    }

    private boolean imageTooTall(Icon image) {
        return image.getIconHeight() + SCREEN_HEIGHT_OFFSET > screenHeight;
    }

    private void resizeWindowToImage(Icon image) {
        int newHeight, newWidth;

        if (imageTooWide(image)) {
            newWidth = screenWidth;
        }
        else {
            newWidth = image.getIconWidth() + SCREEN_WIDTH_OFFSET;
        }

        if (imageTooTall(image)) {
            newHeight = screenHeight;
        }
        else {
            newHeight = image.getIconHeight() + SCREEN_HEIGHT_OFFSET;
        }

        baseUIFrame.setMinimumSize(new Dimension(newWidth, newHeight));
        baseUIFrame.pack();
        mainWindow.revalidate();
    }

    private class BrowseButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            File file = browseForFile();
            if (file != null) {
                try {
                    resetUIForCurrentImage();
                    loadImageAndAdjustUI();
                    transformButton.setEnabled(true);
                } catch (TransformException ex) {
                    resetUIToDefault();
                    JOptionPane.showMessageDialog(
                            rootPane, ex.getMessage(), "ERROR: Invalid file", JOptionPane.PLAIN_MESSAGE);
                    origFileField.selectAll();
                }
            }
        }
    }

    private File browseForFile() {
        fileChooser.showOpenDialog(this);
        File file = fileChooser.getSelectedFile();
        if (file != null) {
            origFileField.setText(file.getPath());
        }
        return file;
    }

    private class RecoverButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mainWindow.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            loadButton.setVisible(false);
            cancelButton.setVisible(true);
            recoverButton.setEnabled(false);
            currWorker = new recoverImage(progressBar);
            currWorker.execute();
        }
    }

    private class TransformButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mainWindow.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            loadButton.setVisible(false);
            cancelButton.setVisible(true);
            transformButton.setEnabled(false);
            currWorker = new transformImage(progressBar);
            currWorker.execute();
        }
    }

    /**
     * Custom KeyListener to handle when a user presses enter/return while the
     * inputFile text field has focus.
     */
    private class FileInputHandler implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == '\n' || e.getKeyChar() == '\r')
                loadButton.doClick();
            else {
                if (transformButton.isEnabled())
                    transformButton.setEnabled(false);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            //do nothing
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //do nothing
        }

    }

    private abstract class SelectionHandler implements ItemListener{
        JRadioButton theSelectedBtn;
        int index;
        ImageIcon selectedImg;
    }

    private class TransformStageSelectionHandler extends SelectionHandler {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                theSelectedBtn = (JRadioButton) e.getItem();
                index = transformImageChoicePanel.indexOfStageImage(theSelectedBtn);
                selectedImg = program.getTransformSPic(index);
                setDisplayImage(selectedImg);
            }
        }
    }

    private void setDisplayImage(ImageIcon image) {
        displayedImg.setIcon(image);
        mainWindow.revalidate();
        mainWindow.repaint();
    }

    private class TransformPixelSelectionHandler extends SelectionHandler {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                theSelectedBtn = (JRadioButton) e.getItem();
                index = transformImageChoicePanel.indexOfPixelImage(theSelectedBtn);
                selectedImg = program.getTransformPPic(index);
                setDisplayImage(selectedImg);
            }
        }
    }

    private class RecoverStageSelectionHandler extends SelectionHandler {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                theSelectedBtn = (JRadioButton) e.getItem();
                index = recoverImageChoicePanel.indexOfStageImage(theSelectedBtn);
                selectedImg = program.getRecoverSPic(index);
                setDisplayImage(selectedImg);
            }
        }
    }

    private class RecoverPixelSelectionHandler extends SelectionHandler {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                theSelectedBtn = (JRadioButton) e.getItem();
                index = recoverImageChoicePanel.indexOfPixelImage(theSelectedBtn);
                selectedImg = program.getRecoverPPic(index);
                setDisplayImage(selectedImg);
            }
        }
    }

    /**
     * Custom SwingWorker to execute the Haar transformation in the background and
     * update the GUI of this HaarDemo when the transformation is done.
     */
    class transformImage extends SwingWorker{
        JProgressBar progressBar;
        public transformImage(JProgressBar progressBar){
            this.progressBar = progressBar;
        }

        /**
         * Calls the HaarTransform instance's loadImage and generateTransformImages methods
         * @return an ArrayList<BufferedImage> of the transformation stage images
         */
        @Override
        protected Object doInBackground() {
            transformImageChoicePanel.clearPixelPanel();
            transformImageChoicePanel.clearStagePanel();
            progressBar.setValue(0);
            progressBar.setVisible(true);
            program.clearTransformImages();
            program.performTransform(fileGenerationToggle.isSelected(), this, progressBar);
            return null;
        }

        /**
         * Updates this HaarDemo's UI when the Haar transformation is complete
         */
        @Override
        protected void done() {
            mainWindow.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            progressBar.setVisible(false);
            cancelButton.setVisible(false);
            loadButton.setVisible(true);
            if (!isCancelled()) {
                updateTransformImageChoicePanel();
                transformButton.setVisible(false);
                transformButton.setEnabled(true);
                recoverButton.setVisible(true);
                baseUIFrame.pack();
                baseUIFrame.revalidate();
                transformImageChoicePanel.manualSelectStageByIndex(INITIAL_STAGE_SHOWN);
            }
        }
    }

    private void updateTransformImageChoicePanel(){
        transformImageChoicePanel.addRadioButtons(program.getNumOfTransformSPics());

        if(!transformImageChoicePanel.isVisible()){
            transformImageChoicePanel.setVisible(true);
            recoverImageChoicePanel.setVisible(false);
        }
    }

    /**
     * Custom SwingWorker to generateRecoverImages from the Haar transformation and
     * update the GUI of this HaarDemo when the recovery is complete.
     */
    class recoverImage extends SwingWorker{
        JProgressBar progressBar;
        public recoverImage(JProgressBar progressBar){
            this.progressBar = progressBar;
        }

        /**
         * Calls the HaarTransform instance's generateRecoverImages method
         * @return an ArrayList<BufferedImage> of the recovery stage images
         */
        @Override
        protected Object doInBackground() {
            recoverImageChoicePanel.clearPixelPanel();
            recoverImageChoicePanel.clearStagePanel();
            progressBar.setValue(0);
            progressBar.setVisible(true);
            program.clearRecoverImages();
            program.performRecover(fileGenerationToggle.isSelected(), this, progressBar);
            return null;
        }

        /**
         * Updates this HaarDemo's UI when the recovery is complete
         */
        @Override
        protected void done(){
            int INDEX_BY_ZERO_OFFSET = 1;
            mainWindow.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            progressBar.setVisible(false);
            cancelButton.setVisible(false);
            loadButton.setVisible(true);
            recoverButton.setEnabled(true);
            if(!isCancelled()) {
                updateRecoverImageChoicePanel();
                recoverButton.setVisible(false);
                transformButton.setVisible(true);
                baseUIFrame.pack();
                baseUIFrame.revalidate();
                recoverImageChoicePanel.manualSelectStageByIndex(program.getNumOfRecoverSPics() - INDEX_BY_ZERO_OFFSET);
            }
        }
    }

    private void updateRecoverImageChoicePanel(){
        recoverImageChoicePanel.addRadioButtons(program.getNumOfRecoverSPics());

        if(!recoverImageChoicePanel.isVisible()){
            recoverImageChoicePanel.setVisible(true);
            transformImageChoicePanel.setVisible(false);
        }
    }
}
