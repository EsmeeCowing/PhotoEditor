//Esmée Cowing

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

class DoodleProgram {
    private ControlPanel controlPanel;
    private MyMenuBar mainMenu;
    private MainCanvas mainCanvas;
    private JPanel mainPanel;
    private JFrame myJFrame;
    ProjectImageHistoryTracker projectImageHistoryTracker;
    Graphics2D projectImagePen;
    private String mode;
    private JFileChooser fileChooser;
    private JSlider penWidthSlider;
    JColorChooser colorChooser;


    public static void main(String[] args) {
        new DoodleProgram();
    }

    DoodleProgram() {
        //misc vars
        projectImageHistoryTracker = new ProjectImageHistoryTracker();
        fileChooser = new JFileChooser();
        colorChooser = new JColorChooser();
        penWidthSlider = new JSlider(0, 50, 5);

        //panels/canvas
        controlPanel = new ControlPanel(BoxLayout.Y_AXIS, new JComponent[]{});
        mainMenu = new MyMenuBar(new MyMenu[]{new MyMenu("File", new JComponent[]{new NewProjectItem(), new LoadProjectItem(), new SaveProjectItem()}), new MyMenu("Mode", new JComponent[]{new DrawItem(), new FilterItem()}), new MyMenu("Edit", new JComponent[]{new UndoItem(), new RedoItem()})});
        mainCanvas = new MainCanvas();

        //general layout
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(mainCanvas, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.WEST);

        myJFrame = new JFrame("Doodle!");

        myJFrame.setJMenuBar(mainMenu);

        myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myJFrame.add(mainPanel);
        myJFrame.pack();
        myJFrame.setLocationRelativeTo(null);
        myJFrame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////

    class MainCanvas extends ImageCanvas {
        int projectImageX;
        int projectImageY;
        int newProjectImageWidth;
        int newProjectImageHeight;
        double projectImageToMainCanvas;
        PenListener drawListener;
        Color backgroundColor;

        MainCanvas() {
            super(1675, 915);
            drawListener = new PenListener();
            addMouseListener(drawListener);
            addMouseMotionListener(drawListener);
            backgroundColor = new Color(174,174,178);
        }

        //MainCanvas functions

        public void setProjectImage(boolean isBlank) {
            if (isBlank) {
                projectImageHistoryTracker.updateCurrentImage(new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB));//todo: perhaps change this so that the user can specify the dimensions of their image (only if time)
                projectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();
                projectImagePen.setColor(Color.WHITE);//this makes the buffered image white
                projectImagePen.fillRect(0,0, projectImageHistoryTracker.getCurrentImage().getWidth(), projectImageHistoryTracker.getCurrentImage().getHeight());

                draw();
            } else {
                try {
                    if (fileChooser.showOpenDialog(myJFrame) == JFileChooser.APPROVE_OPTION) {
                        projectImageHistoryTracker.updateCurrentImage(ImageIO.read(fileChooser.getSelectedFile()));
                        projectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();

                        draw();
                    }
                } catch (IOException ex) {//todo: maybe make this into a user message?
                    ex.printStackTrace();
                }
            }

        }

        public void saveProjectImage() {
            try {
                if (fileChooser.showSaveDialog(myJFrame) == JFileChooser.APPROVE_OPTION) {
                    ImageIO.write(projectImageHistoryTracker.getCurrentImage(), "png", fileChooser.getSelectedFile());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void draw() {
            clear();
            Graphics pen = getPen();
            pen.setColor(backgroundColor);
            pen.fillRect(0,0,this.getWidth(), this.getHeight());

            if (projectImageHistoryTracker.getCurrentImage() != null) {
                double heightToWidthProjectImage = ((double) projectImageHistoryTracker.getCurrentImage().getHeight()) / ((double) projectImageHistoryTracker.getCurrentImage().getWidth());
                if (heightToWidthProjectImage > ((double) this.getHeight()) / ((double) this.getWidth())) {
                    newProjectImageHeight = this.getHeight();
                    newProjectImageWidth = (int) ((newProjectImageHeight * (1 / heightToWidthProjectImage)));
                    projectImageY = (this.getHeight() - newProjectImageHeight) / 2;
                    projectImageX = (this.getWidth() - newProjectImageWidth) / 2;
                    projectImageToMainCanvas = ((double)projectImageHistoryTracker.getCurrentImage().getHeight())/((double)this.getHeight());

                } else {
                    newProjectImageWidth = this.getWidth();
                    newProjectImageHeight = (int) (newProjectImageWidth * heightToWidthProjectImage);
                    projectImageX = (this.getWidth() - newProjectImageWidth) / 2;
                    projectImageY = (this.getHeight() - newProjectImageHeight) / 2;
                    projectImageToMainCanvas = ((double)projectImageHistoryTracker.getCurrentImage().getWidth())/((double)this.getWidth());

                }

                pen.drawImage(projectImageHistoryTracker.getCurrentImage(), projectImageX, projectImageY, newProjectImageWidth, newProjectImageHeight, this);
            }

            display();
        }

        public void resized() {
            draw();
        }

        //MainCanvas classes

        class PenListener implements MouseListener, MouseMotionListener {
            int previousX;
            int previousY;
            int currentX;
            int currentY;

            @Override
            public void mousePressed(MouseEvent e) {
                if (mode == "draw") {
                    BufferedImage oldImage  = projectImageHistoryTracker.getCurrentImage();
                    projectImageHistoryTracker.updateCurrentImage(new BufferedImage(projectImageHistoryTracker.getCurrentImage().getWidth(), projectImageHistoryTracker.getCurrentImage().getHeight(), BufferedImage.TYPE_INT_RGB));
                    projectImageHistoryTracker.getCurrentImage().createGraphics().drawImage(oldImage, 0,0, mainCanvas);

                    Graphics2D newprojectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();
                    newprojectImagePen.setColor(projectImagePen.getColor());
                    newprojectImagePen.setStroke(new BasicStroke(penWidthSlider.getValue(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));//todo: look at basicstroke doc, user set stroke with a slider this will be if I have time

                    projectImagePen = newprojectImagePen;

                    this.previousX = (int)((double)(e.getX()-((mainCanvas.getWidth() - newProjectImageWidth) / 2))*projectImageToMainCanvas);
                    this.previousY = (int)((double)(e.getY()-((mainCanvas.getHeight() - newProjectImageHeight) / 2))*projectImageToMainCanvas);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mode == "draw") {
                    this.currentX = (int)((double)(e.getX()-((mainCanvas.getWidth() - newProjectImageWidth) / 2))*projectImageToMainCanvas);
                    this.currentY = (int)((double)(e.getY()-((mainCanvas.getHeight() - newProjectImageHeight) / 2))*projectImageToMainCanvas);

                    projectImagePen.drawLine(this.previousX, this.previousY, this.currentX, this.currentY);

                    this.previousX = this.currentX;
                    this.previousY = this.currentY;

                    mainCanvas.draw();
                }
            }

            public void mouseMoved(MouseEvent e){}
            public void mouseReleased(MouseEvent e){}
            public void mouseClicked(MouseEvent e){}
            public void mouseEntered(MouseEvent e){}
            public void mouseExited(MouseEvent e){}
        }

    }

    ///////////////////////////////////////////////////////////////////

    //Component Classes Are Below organized by type

    //panel code

    class ControlPanel extends JPanel {

        ControlPanel(int orientation, JComponent[] components)//orientation should either be BoxLayout.X_AXIS or BoxLayout.Y_AXIS
        {
            setLayout(new BoxLayout(this, orientation));
            for (JComponent component : components) {
                add(component);
            }
            add(Box.createGlue());
        }


    }


    //panel buttons

    class ColorChooserButton extends JButton implements ActionListener{

        ColorChooserButton(){
            super("Pen Color");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            projectImagePen.setColor(colorChooser.showDialog(mainCanvas, "Pick A Pen Color!", Color.WHITE));

        }
    }

    class GreyscaleButton extends JButton implements ActionListener{

        GreyscaleButton(){
            super("Greyscale");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedImage oldImage  = projectImageHistoryTracker.getCurrentImage();
            projectImageHistoryTracker.updateCurrentImage(new BufferedImage(projectImageHistoryTracker.getCurrentImage().getWidth(), projectImageHistoryTracker.getCurrentImage().getHeight(), BufferedImage.TYPE_INT_RGB));
            projectImageHistoryTracker.getCurrentImage().createGraphics().drawImage(oldImage, 0,0, mainCanvas);
            Graphics2D newProjectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();
            newProjectImagePen.setColor(projectImagePen.getColor());
            projectImagePen = newProjectImagePen;

            for (int x = 0; x < projectImageHistoryTracker.getCurrentImage().getWidth(); x++) {
                for (int y = 0; y < projectImageHistoryTracker.getCurrentImage().getHeight(); y++) {
                    Color oldColor = new Color(projectImageHistoryTracker.getCurrentImage().getRGB(x, y));
                    int averageRGB = (oldColor.getRed() + oldColor.getGreen() + oldColor.getBlue()) / 3;
                    Color newColor = new Color(averageRGB, averageRGB, averageRGB);

                    projectImageHistoryTracker.getCurrentImage().setRGB(x, y, newColor.getRGB());
                }
            }

            mainCanvas.draw();
        }
    }

    class CoolButton extends JButton implements ActionListener{

        CoolButton(){
            super("Cool");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedImage oldImage  = projectImageHistoryTracker.getCurrentImage();
            projectImageHistoryTracker.updateCurrentImage(new BufferedImage(projectImageHistoryTracker.getCurrentImage().getWidth(), projectImageHistoryTracker.getCurrentImage().getHeight(), BufferedImage.TYPE_INT_RGB));
            projectImageHistoryTracker.getCurrentImage().createGraphics().drawImage(oldImage, 0,0, mainCanvas);
            Graphics2D newprojectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();
            newprojectImagePen.setColor(projectImagePen.getColor());
            projectImagePen = newprojectImagePen;

            for (int x = 0; x < projectImageHistoryTracker.getCurrentImage().getWidth(); x++) {
                for (int y = 0; y < projectImageHistoryTracker.getCurrentImage().getHeight(); y++) {
                    Color oldColor = new Color(projectImageHistoryTracker.getCurrentImage().getRGB(x, y));
                    Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), 255);
                    projectImageHistoryTracker.getCurrentImage().setRGB(x, y, newColor.getRGB());

                }
            }

            mainCanvas.draw();
        }
    }

    class DarkButton extends JButton implements ActionListener{

        DarkButton(){
            super("Dark");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedImage oldImage  = projectImageHistoryTracker.getCurrentImage();
            projectImageHistoryTracker.updateCurrentImage(new BufferedImage(projectImageHistoryTracker.getCurrentImage().getWidth(), projectImageHistoryTracker.getCurrentImage().getHeight(), BufferedImage.TYPE_INT_RGB));
            projectImageHistoryTracker.getCurrentImage().createGraphics().drawImage(oldImage, 0,0, mainCanvas);
            Graphics2D newprojectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();
            newprojectImagePen.setColor(projectImagePen.getColor());
            projectImagePen = newprojectImagePen;

            for (int x = 0; x < projectImageHistoryTracker.getCurrentImage().getWidth(); x++) {
                for (int y = 0; y < projectImageHistoryTracker.getCurrentImage().getHeight(); y++) {
                    Color oldColor = new Color(projectImageHistoryTracker.getCurrentImage().getRGB(x, y));
                    Color newColor = new Color((int)(oldColor.getRed()*0.2), (int)(oldColor.getGreen()*0.2), (int)(oldColor.getBlue()*0.2));
                    projectImageHistoryTracker.getCurrentImage().setRGB(x, y, newColor.getRGB());

                }
            }

            mainCanvas.draw();
        }
    }

    class BlackAndWhiteButton extends JButton implements ActionListener{

        BlackAndWhiteButton(){
            super("Black and White");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedImage oldImage  = projectImageHistoryTracker.getCurrentImage();
            projectImageHistoryTracker.updateCurrentImage(new BufferedImage(projectImageHistoryTracker.getCurrentImage().getWidth(), projectImageHistoryTracker.getCurrentImage().getHeight(), BufferedImage.TYPE_INT_RGB));
            projectImageHistoryTracker.getCurrentImage().createGraphics().drawImage(oldImage, 0,0, mainCanvas);
            Graphics2D newProjectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();
            newProjectImagePen.setColor(projectImagePen.getColor());
            projectImagePen = newProjectImagePen;

            for (int x = 0; x < projectImageHistoryTracker.getCurrentImage().getWidth(); x++) {
                for (int y = 0; y < projectImageHistoryTracker.getCurrentImage().getHeight(); y++) {
                    Color oldColor = new Color(projectImageHistoryTracker.getCurrentImage().getRGB(x, y));

                    int averageRGB = (oldColor.getRed()+oldColor.getGreen()+oldColor.getBlue())/3;

                    Color newColor;
                    if (averageRGB < 127) {
                        newColor = new Color(0,0,0);
                    }
                    else{
                        newColor  = new Color (255,255,255);
                    }
                    projectImageHistoryTracker.getCurrentImage().setRGB(x, y, newColor.getRGB());

                }
            }
            mainCanvas.draw();
        }
    }

    class PixelateButton extends JButton implements ActionListener{

        PixelateButton(){
            super("Pixelate");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedImage oldImage  = projectImageHistoryTracker.getCurrentImage();
            projectImageHistoryTracker.updateCurrentImage(new BufferedImage(projectImageHistoryTracker.getCurrentImage().getWidth(), projectImageHistoryTracker.getCurrentImage().getHeight(), BufferedImage.TYPE_INT_RGB));
            projectImageHistoryTracker.getCurrentImage().createGraphics().drawImage(oldImage, 0,0, mainCanvas);
            Graphics2D newProjectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();
            newProjectImagePen.setColor(projectImagePen.getColor());
            projectImagePen = newProjectImagePen;

            for (int x = 0; x < projectImageHistoryTracker.getCurrentImage().getWidth(); x+=10) {
                for (int y = 0; y < projectImageHistoryTracker.getCurrentImage().getHeight(); y+=10) {
                    double averageR = 0;
                    double averageG = 0;
                    double averageB = 0;

                    for (int nextX = x; nextX < x+10; nextX++){
                        for (int nextY = y; nextY < y+10; nextY++) {
                            if (nextX < projectImageHistoryTracker.getCurrentImage().getWidth() && nextY < projectImageHistoryTracker.getCurrentImage().getHeight()) {
                                Color pixelColor = new Color(projectImageHistoryTracker.getCurrentImage().getRGB(nextX, nextY));
                                averageR += (double)pixelColor.getRed() * 0.01;
                                averageG += (double)pixelColor.getGreen() * 0.01;
                                averageB += (double)pixelColor.getBlue() * 0.01;
                            }
                        }
                    }

                    Color newColor = new Color((int)averageR, (int)averageG, (int)averageB);

                    for (int nextX = x; nextX< x+10; nextX++){
                        for (int nextY = y; nextY < y+10; nextY++){
                            if (nextX < projectImageHistoryTracker.getCurrentImage().getWidth() && nextY < projectImageHistoryTracker.getCurrentImage().getHeight()) {
                                projectImageHistoryTracker.getCurrentImage().setRGB(nextX, nextY, newColor.getRGB());
                            }
                        }
                    }

                }
            }

            mainCanvas.draw();
        }
    }

    class SmoothButton extends JButton implements ActionListener {

        SmoothButton() {
            super("Smooth");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedImage oldImage = projectImageHistoryTracker.getCurrentImage();
            projectImageHistoryTracker.updateCurrentImage(new BufferedImage(projectImageHistoryTracker.getCurrentImage().getWidth(), projectImageHistoryTracker.getCurrentImage().getHeight(), BufferedImage.TYPE_INT_RGB));
            projectImageHistoryTracker.getCurrentImage().createGraphics().drawImage(oldImage, 0, 0, mainCanvas);
            Graphics2D newProjectImagePen = projectImageHistoryTracker.getCurrentImage().createGraphics();
            newProjectImagePen.setColor(projectImagePen.getColor());
            projectImagePen = newProjectImagePen;

            for (int x = 0; x < projectImageHistoryTracker.getCurrentImage().getWidth(); x++) {
                for (int y = 0; y < projectImageHistoryTracker.getCurrentImage().getHeight(); y++) {
                    double averageR = 0;
                    double averageG = 0;
                    double averageB = 0;

                    int numNeighbours = 0;
                    for (int x2 = -1; x2 < 2; x2++) {
                        for (int y2 = -1; y2 < 2; y2++) {
                            if (x + x2 < projectImageHistoryTracker.getCurrentImage().getWidth() && y + y2 < projectImageHistoryTracker.getCurrentImage().getHeight() && x + x2 > 0 && y + y2 > 0) {
                                numNeighbours++;
                            }
                        }
                    }


                    for (int x2 = -1; x2 < 2; x2++) {
                        for (int y2 = -1; y2 < 2; y2++) {
                            if (x + x2 < projectImageHistoryTracker.getCurrentImage().getWidth() && y + y2 < projectImageHistoryTracker.getCurrentImage().getHeight() && x + x2 > 0 && y + y2 > 0) {
                                Color pixelColor = new Color(projectImageHistoryTracker.getCurrentImage().getRGB(x + x2, y + y2));
                                averageR += pixelColor.getRed() / numNeighbours;
                                averageG += pixelColor.getGreen() / numNeighbours;
                                averageB += pixelColor.getBlue() / numNeighbours;
                            }
                        }
                    }

                    Color newColor = new Color((int) averageR, (int) averageG, (int) averageB);
                    projectImageHistoryTracker.getCurrentImage().setRGB(x, y, newColor.getRGB());

                }
            }
            mainCanvas.draw();
        }
    }



    //menu code
    class MyMenuBar extends JMenuBar {
        MyMenuBar(JMenu[] menus) {
            for (JMenu menu : menus) {
                add(menu);
            }
            add(Box.createGlue());
        }
    }

    class MyMenu extends JMenu {
        MyMenu(String label, JComponent[] components) {
            super(label);
            for (JComponent component : components) {
                add(component);
            }
        }
    }




    //Menu Items

    class NewProjectItem extends JMenuItem implements ActionListener {
        NewProjectItem() {
            super("New Project");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mainCanvas.setProjectImage(true);
        }

    }

    class LoadProjectItem extends JMenuItem implements ActionListener {
        LoadProjectItem() {
            super("Load Project");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mainCanvas.setProjectImage(false);
        }
    }

    class SaveProjectItem extends JMenuItem implements ActionListener {
        SaveProjectItem() {
            super("Save Project");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mainCanvas.saveProjectImage();
        }
    }

    class DrawItem extends JMenuItem implements ActionListener {
        DrawItem() {
            super("Draw");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) { //revalidate, JFrame.repack
            mode = "draw";
            mainPanel.remove(controlPanel);
            controlPanel = new ControlPanel(BoxLayout.Y_AXIS, new JComponent[]{penWidthSlider, new ColorChooserButton()});
            mainPanel.add(controlPanel, BorderLayout.WEST);
            mainPanel.revalidate();
        }
    }

    class FilterItem extends JMenuItem implements ActionListener {
        FilterItem() {
            super("Filter");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mode = "filter";
            mainPanel.remove(controlPanel);
            controlPanel = new ControlPanel(BoxLayout.Y_AXIS, new JComponent[]{new GreyscaleButton(), new CoolButton(), new DarkButton(), new BlackAndWhiteButton(), new PixelateButton(), new SmoothButton()}); //todo: add back in buttons
            mainPanel.add(controlPanel, BorderLayout.WEST);
            mainPanel.revalidate();
        }
    }

    class UndoItem extends JMenuItem implements ActionListener {
        UndoItem() {
            super("Undo");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            projectImageHistoryTracker.goBackToPreviousImage();
            mainCanvas.draw();
        }
    }

    class RedoItem extends JMenuItem implements ActionListener {
        RedoItem() {
            super("Redo");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            projectImageHistoryTracker.goBackToNextImage();
            mainCanvas.draw();
        }
    }

    //history code

    class ProjectImageHistoryTracker{
        ArrayList<BufferedImage> projectImageVersions;
        int indexOfCurrentImage;

        ProjectImageHistoryTracker(){
            projectImageVersions = new ArrayList<BufferedImage>();
        }

        public void updateCurrentImage(BufferedImage newImage){
            if (projectImageVersions.size() == 0){
                projectImageVersions.add(newImage);
                indexOfCurrentImage = 0;
            }
            else {
                if (indexOfCurrentImage < projectImageVersions.size() - 1) {
                    for (int i = projectImageVersions.size()-1; i > indexOfCurrentImage; i--) {
                        projectImageVersions.remove(i);
                    }
                }
                projectImageVersions.add(indexOfCurrentImage + 1, newImage);
                indexOfCurrentImage++;
            }
        }

        public BufferedImage getCurrentImage(){
            if (projectImageVersions.size() == 0){
                return null;
            }
            return projectImageVersions.get(indexOfCurrentImage);
        }

        public void goBackToPreviousImage(){
            if (indexOfCurrentImage > 0) {
                indexOfCurrentImage--;
            }
        }

        public void goBackToNextImage(){
            if (indexOfCurrentImage < projectImageVersions.size()-1){
                indexOfCurrentImage++;
            }
        }
    }
}