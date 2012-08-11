package media;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.ScrollPaneUI;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.animation.timing.triggers.ActionTrigger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

@SuppressWarnings("serial")
public class MusicPlayer extends JFrame implements Runnable {
	
    private JButton rightLayoutButton;
    private JButton leftLayoutButton;
    private static Point mouseDownCompCoords = null;
    private JButton playButton;
    private JButton pauseButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton minimiseButton;
    private JButton closeButton;
    private JCheckBox shuffleButton;
    private JCheckBox loopAllButton;
    private JCheckBox loopSongButton;
    private JProgressBar positionBar;
    private JLabel artWorkImage;
    private JLabel background;
    private JSlider volumeSlider;
    private SongQueue songQueue;
    private JLabel nameBar;
    private StarRater starRater;
    private String songNameString;
    private int songNameSize = 24;
    private boolean running = true;
    private int currentStringCharacter = 0;
	private SongLibrary songLibrary;
	private JScrollPane songsList;
	private JList list;
	private boolean updating =false;

    public MusicPlayer() {
        super("Motion Demo");

        setUndecorated(true);
        final JComponent buildContentPane = buildContentPane();
        add(buildContentPane);
        
        BufferedImage read1 = null;
		try {
			read1 = ImageIO.read(getClass().getResource("/images/icons/MusicLibrary.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        setIconImage(read1);
        configureAnimations();
        setAlwaysOnTop(true);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBounds(0, 0, 200, 200);
        setBackground(new Color(0, 255, 0, 0));
        addMouseListener(new PopClickListener());
        addMouseListener(new MouseListenerDemo());
        addMouseMotionListener(new MouseMotionListenerDemo());
        loadSongLibraryXML();
        updating = true;
        list.setSelectedIndex(songQueue.getSongIndex());
        updating = false;
    }

    private ImageIcon loadArtworkImage(Image image) {
        try {
            final Image read1 = image.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            return new ImageIcon(read1);
        } catch (final Exception e) {
            final URL path = getClass().getResource("/images/background/noSong.png");
            return new ImageIcon(path);
        }
    }

    private void updateSongInfo() {
        final MetaData songMetaData = songQueue.getCurrentSong().getMetaData();
        artWorkImage.setIcon(loadArtworkImage(songMetaData.getImage()));
        currentStringCharacter = 0;
        songNameString = songMetaData.getArtistName() + " - " + songMetaData.getSongName();
        volumeSlider.setValue((int)(songQueue.getVolume()*100));
        int rating = 0;
        try {
            rating = Integer.parseInt(songMetaData.getRating()) / 51;
        } catch (final Exception e) {
            System.out.println("no rating");
        }
        starRater.setSelection(rating); 
        updating = true;
        list.setSelectedIndex(songQueue.getSongIndex());
        updating = false;
    }

    private void loadSongLibraryXML() {
        try {
            final Serializer serializer = new Persister();
            songLibrary = null;
            final String homeDirectory = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
            final String resource = homeDirectory + "/Library.xml";
            final File source = new File(resource);
            songLibrary = serializer.read(SongLibrary.class, source);
            if (songQueue != null) {
                songQueue.stop();
                pauseButton.setVisible(false);
                playButton.setVisible(true);
                songQueue = null;
            }
            songQueue = new SongQueue(songLibrary.getSongList());
            songQueue.addPropertyChangeListener(new PropertyChangeListenerDemo());
            songQueue.setVolume(0.5f);
            updateSongInfo();     
            list.setListData(songQueue.getSongList());
        } catch (final Exception e) {
            final URL path = getClass().getResource("/images/background/noSong.png");
            artWorkImage.setIcon(new ImageIcon(path));
            System.out.println("no library");
        }
    }

    private void configureAnimations() {

        final Animator leftAnimator = new Animator(200);
        leftAnimator.setAcceleration(0.3f);
        leftAnimator.setDeceleration(0.2f);
//        leftAnimator.addTarget(new PropertySetter(playButton, "location", new Point(playButton.getX() + 200, playButton.getY())));
//        leftAnimator.addTarget(new PropertySetter(pauseButton, "location", new Point(pauseButton.getX() + 200,pauseButton.getY())));
//        leftAnimator.addTarget(new PropertySetter(prevButton, "location", new Point(prevButton.getX() + 200, prevButton.getY())));
//        leftAnimator.addTarget(new PropertySetter(nextButton, "location", new Point(nextButton.getX() + 200, nextButton.getY())));
//        leftAnimator.addTarget(new PropertySetter(nameBar, "location", new Point(nameBar.getX() + 200, nameBar.getY())));
        leftAnimator.addTarget(new PropertySetter(positionBar, "size", new Dimension((positionBar.getSize().width * 2)-1, positionBar.getSize().height)));
        leftAnimator.addTarget(new PropertySetter(rightLayoutButton, "location", new Point( rightLayoutButton.getX() + 200, rightLayoutButton.getY())));
        leftAnimator.addTarget(new PropertySetter(leftLayoutButton, "location", new Point( leftLayoutButton.getX() + 200, leftLayoutButton.getY())));
        leftAnimator.addTarget(new PropertySetter(minimiseButton, "location", new Point(minimiseButton.getX() + 200, minimiseButton.getY())));
        leftAnimator.addTarget(new PropertySetter(closeButton, "location", new Point(closeButton.getX() + 200, closeButton.getY())));
        leftAnimator.addTarget(new PropertySetter(starRater, "location", new Point(starRater.getX() + 200, starRater.getY())));
//        leftAnimator.addTarget(new PropertySetter(volumeSlider, "location", new Point(volumeSlider.getX() + 200, volumeSlider.getY())));
//        leftAnimator.addTarget(new PropertySetter(shuffleButton, "location", new Point(shuffleButton.getX() + 200, shuffleButton.getY())));
//        leftAnimator.addTarget(new PropertySetter(loopAllButton, "location", new Point(loopAllButton.getX() + 200, loopAllButton.getY())));
//        leftAnimator.addTarget(new PropertySetter(loopSongButton, "location", new Point(loopSongButton.getX() + 200, loopSongButton.getY())));
        leftAnimator.addTarget(new PropertySetter(volumeSlider, "size", volumeSlider.getSize()));
        leftAnimator.addTarget(new PropertySetter(songsList, "size", songsList.getSize()));
        leftAnimator.addTarget(new PropertySetter(background, "location", new Point(background.getX() + 200, background .getY()))); 
        leftAnimator.addTarget(new PropertySetter(this, "size", new Dimension(400, 200)));
        ActionTrigger.addTrigger(leftLayoutButton, leftAnimator);

        final Animator rightAnimator = new Animator(200);
        rightAnimator.setAcceleration(0.3f);
        rightAnimator.setDeceleration(0.2f);
//        rightAnimator.addTarget(new PropertySetter(playButton, "location", playButton.getLocation()));
//        rightAnimator.addTarget(new PropertySetter(pauseButton, "location", pauseButton.getLocation()));
//        rightAnimator.addTarget(new PropertySetter(prevButton, "location", prevButton.getLocation()));
//        rightAnimator.addTarget(new PropertySetter(nextButton, "location", nextButton.getLocation()));
//        rightAnimator.addTarget(new PropertySetter(nameBar, "location", nameBar.getLocation()));
        rightAnimator.addTarget(new PropertySetter(positionBar, "size", positionBar.getSize()));
        rightAnimator.addTarget(new PropertySetter(rightLayoutButton, "location", rightLayoutButton.getLocation()));
        rightAnimator.addTarget(new PropertySetter(leftLayoutButton, "location", leftLayoutButton.getLocation()));
        rightAnimator.addTarget(new PropertySetter(minimiseButton, "location", minimiseButton.getLocation()));
        rightAnimator.addTarget(new PropertySetter(closeButton, "location", closeButton.getLocation()));
        rightAnimator.addTarget(new PropertySetter(starRater, "location", starRater.getLocation()));
        rightAnimator.addTarget(new PropertySetter(volumeSlider, "size", new Dimension(0, (int)volumeSlider.getSize().getHeight())));
        rightAnimator.addTarget(new PropertySetter(songsList, "size", new Dimension(0, (int)songsList.getSize().getHeight())));
//        rightAnimator.addTarget(new PropertySetter(shuffleButton, "location", shuffleButton.getLocation()));
//        rightAnimator.addTarget(new PropertySetter(loopAllButton, "location", loopAllButton.getLocation()));
//        rightAnimator.addTarget(new PropertySetter(loopSongButton, "location", loopSongButton.getLocation()));
        rightAnimator.addTarget(new PropertySetter(background, "location", background.getLocation()));
        rightAnimator.addTarget(new PropertySetter(this, "size", new Dimension(200, 200)));
        ActionTrigger.addTrigger(rightLayoutButton, rightAnimator);
    }

    private ImageIcon loadIconImageHover(String iconDirectory, int size) {
        try {
            final BufferedImage read1 = ImageIO.read(getClass().getResource(iconDirectory));
            final BufferedImage read2 = createDropShadow(read1);
            return new ImageIcon(read2.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (final Exception e) {
            return null;
        }
    }
    
    private ImageIcon loadIconImage(String iconDirectory, int size, int amount) {
        try {
            final BufferedImage read1 = ImageIO.read(getClass().getResource(iconDirectory));
            final BufferedImage read2 = createLighterImage(read1,amount);
            final BufferedImage read3 = createDropShadow(read2);
            return new ImageIcon(read3.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (final Exception e) {
            return null;
        }
    }

	private JButton createButton(String imageIconPath, int x, int y, int size) {
        final ImageIcon imageIcon1 = loadIconImageHover(imageIconPath, size);
        final ImageIcon imageIcon2 = loadIconImage(imageIconPath, size, 60);        
        
        final JButton button = new JButton();
        button.setIcon(imageIcon1);
        button.setPressedIcon(imageIcon1);
        button.setRolloverIcon(imageIcon2);
        button.setRolloverEnabled(true);
        button.setSelectedIcon(imageIcon1);
        button.setDisabledIcon(imageIcon1);
        button.setDisabledSelectedIcon(imageIcon1);
        button.setRolloverSelectedIcon(imageIcon2);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBounds(x, y, size, size);
        button.setBackground(new Color(255, 255, 255, 0));
        return button;
    }
	
	private JCheckBox createToggleButton(String imageIconPath, int x, int y, int size) {
		
        final ImageIcon imageIcon1 = loadIconImage(imageIconPath, size, 60);
        final ImageIcon imageIcon2 = loadIconImageHover(imageIconPath, size); 
 
        final JCheckBox toggleButton = new JCheckBox();
        toggleButton.setIcon(imageIcon2);
        toggleButton.setRolloverIcon(imageIcon1);
        toggleButton.setSelectedIcon(imageIcon1);
        toggleButton.setPressedIcon(imageIcon2);
        toggleButton.setOpaque(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setBounds(x, y, size, size);
        toggleButton.setBackground(new Color(255, 255, 255, 0));	
		return toggleButton;		
	}
	
    private JLayeredPane loadIcons(JLayeredPane pane) {

        prevButton = createButton("/images/icons/rew.png", 10, 5, 40);
        prevButton.addActionListener(new PreviousSongActionListener());
        pane.add(prevButton, 0);

        playButton = createButton("/images/icons/play.png", 55, 5, 40);
        playButton.addActionListener(new PlaySongActionListener());
        pane.add(playButton, 0);

        pauseButton = createButton("/images/icons/pause.png", 55, 5, 40);
        pauseButton.addActionListener(new PauseSongActionListener());
        pauseButton.setVisible(false);
        pane.add(pauseButton, 0);

        nextButton = createButton("/images/icons/ff.png", 100, 5, 40);
        nextButton.addActionListener(new NextSongActionListener());
        pane.add(nextButton, 0);

        minimiseButton = createButton("/images/icons/minus.png", 160, 5, 20);
        minimiseButton.addActionListener(new MinimizeActionListener());
        pane.add(minimiseButton, 0);
        
        closeButton = createButton("/images/icons/close.png", 180, 5, 20);
        closeButton.addActionListener(new CloseActionListener());
        pane.add(closeButton, 0);

        leftLayoutButton = createButton("/images/icons/next.png", 170, 170, 30);
        leftLayoutButton.addActionListener(new LeftLayoutActionListener());
        pane.add(leftLayoutButton, 0);

        rightLayoutButton = createButton("/images/icons/back.png", 170, 170, 30);
        rightLayoutButton.addActionListener(new RightLayoutActionListener());
        rightLayoutButton.setVisible(false);
        pane.add(rightLayoutButton, 0);

        shuffleButton = createToggleButton("/images/icons/shuffle.png", 5, 170, 30);
        shuffleButton.addActionListener(new ShuffleActionListener());
        shuffleButton.setSelected(false);
        pane.add(shuffleButton, 0);

        loopAllButton = createToggleButton("/images/icons/loopAll.png", 30, 170, 30);
        loopAllButton.addActionListener(new LoopAllActionListener());
        loopAllButton.setSelected(true);
        pane.add(loopAllButton, 0);

        loopSongButton = createToggleButton("/images/icons/loopSong.png", 55, 170, 30);
        loopSongButton.addActionListener(new LoopSongActionListener());
        loopSongButton.setSelected(false);
        pane.add(loopSongButton, 0);

        return pane;
    }

    private JComponent buildContentPane() {
        JLayeredPane pane = new JLayeredPane();
        pane.setOpaque(false);

        artWorkImage = new JLabel();
        artWorkImage.setBounds(0, 0, 200, 200);
        pane.add(artWorkImage, 1);

        background = new JLabel(loadIconImageHover("/images/background/grey.jpg", 204));
        background.setBounds(0, 0, 200, 200);
        pane.add(background, 2);

        starRater = new StarRater();
        starRater.setBackground(new Color(255, 255, 255, 255));
        starRater.setBounds(5, 180, 100, 20);
        pane.add(starRater, 1);
        starRater.setVisible(true);
        starRater.setRating(0);
        starRater.addPropertyChangeListener(new PropertyChangeListener() {
			
        	@Override
            public void propertyChange(PropertyChangeEvent evt) {
                final String propertyName = evt.getPropertyName();
				if(propertyName.equals("newRating")){
						songQueue.getCurrentSong().getMetaData().setRating("" + (starRater.getSelection()*51));
				}
			}
		});

        pane = loadIcons(pane);

        positionBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 10000);
        positionBar.setSize(200, 5);
        positionBar.addMouseListener(new PositionBarMouseListener());
        positionBar.setBackground(new Color(0, 0, 0, 255));
        positionBar.setBorderPainted(false);
        pane.add(positionBar, 0);

        nameBar = new JLabel();
        nameBar.setBounds(10, 45, 180, 30);
        pane.add(nameBar, 0);

        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 1);
        volumeSlider.setBackground(new Color(255, 255, 255,0));
        volumeSlider.setBounds(285, 180, 90, 20);
       
        volumeSlider.addChangeListener(new VolumeSliderChangeListener());
        volumeSlider.addMouseListener(new VolumeSliderMouseListener());
        volumeSlider.setUI(new MySliderUI(volumeSlider));

        volumeSlider.setFocusable(false);
        pane.add(volumeSlider, 2);
        final Thread t = new Thread(this);
        t.start();
        
    
        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setBackground(new Color(45,45,45,255));
        Font font = new Font("Calibri", Font.PLAIN, 16);
        list.setForeground(new Color(105,105,105,255));
        list.setFont(font );
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if(!updating){
						songQueue.setSongByIndex(list.getSelectedIndex());
						updateSongInfo();
					}

			    }
			}
		});
		songsList = new JScrollPane(list);
		songsList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		songsList.setBounds(new Rectangle(210,10,145,160));
		MyScrollBar b = new MyScrollBar();
		songsList.setVerticalScrollBar(b);
		songsList.setBackground(new Color(45,45,45,255));
		songsList.setWheelScrollingEnabled(true);
		songsList.setBorder(BorderFactory.createEmptyBorder());
		
        pane.add(songsList, 3);
        
        
        
        

        return pane;
    }
    
    private BufferedImage createLighterImage(BufferedImage read1, int amount) {
    	RescaleOp rescaleOp = new RescaleOp(1.7f, amount, null);
    	rescaleOp.filter(read1, read1);   	
    	 final ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
         op.filter(read1, read1);
		return read1;
	}

    public static BufferedImage createDropShadow(BufferedImage image) {
        BufferedImage shadow1 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2 = shadow1.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        final RescaleOp op1 = new RescaleOp(0, 455, null);
        op1.filter(shadow1, shadow1);

        final ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(shadow1, shadow1);

        shadow1 = getGaussianBlurFilter(3, true).filter(shadow1, null);
        shadow1 = getGaussianBlurFilter(3, false).filter(shadow1, null);

        final BufferedImage combined = new BufferedImage(image.getWidth() + 4, image.getHeight() + 2,
                BufferedImage.TYPE_INT_ARGB);
        final Graphics g = combined.getGraphics();
        g.drawImage(shadow1, 0, 0, null);
        g.drawImage(shadow1, 0, 2, null);
        g.drawImage(shadow1, 2, 0, null);
        g.drawImage(shadow1, 2, 2, null);
        g.drawImage(image, 1, 1, null);

        return combined;
    }

    public static ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }

        final int size = radius * 2 + 1;
        final float[] data = new float[size];

        final float sigma = radius / 3.0f;
        final float twoSigmaSquare = 2.0f * sigma * sigma;
        final float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        for (int i = -radius; i <= radius; i++) {
            final float distance = i * i;
            final int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }

        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        Kernel kernel = null;
        if (horizontal) {
            kernel = new Kernel(size, 1, data);
        } else {
            kernel = new Kernel(1, size, data);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }

    private Image process(String sentence, int height) {
    	if(sentence.length() > 0){
        final int w = 1000; // width of box
        final int h = 100;
        final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = img.createGraphics();
        g2d.setPaint(Color.BLACK);
        g2d.setFont(new Font("Calibri", Font.PLAIN, 50));
        final String s = sentence;
        final FontMetrics fm = g2d.getFontMetrics();
        final int x = 0;
        final int y = fm.getHeight() / 2;
        g2d.drawString(s, x, y);

        final BufferedImage stringDropShadow = createDropShadow(img);

        final BufferedImage newImg = new BufferedImage(fm.stringWidth(sentence) + 2, fm.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics g = newImg.createGraphics();
        g.drawImage(stringDropShadow, 0, 0, null);
        
        int height2 = newImg.getHeight();        
        int width2 = newImg.getWidth();
        
        float ratio = (float)height / (float)height2;
        int width =(int) ((float)width2 * ratio);
             
        return newImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    	}
    	return  new BufferedImage(height, height, BufferedImage.TYPE_INT_ARGB);
    }

    private void songLibraryLoader() {

        final JFrame frame = new JFrame("Loading Songs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JFrame newContentPane = new MediaLoaderProgressBar();
        newContentPane.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				String name = arg0.getPropertyName();
				if(name.equals("libraryLoaded") ){
					songLibrary = (SongLibrary) arg0.getNewValue();
					float volume = songQueue.getVolume();
					boolean random = songQueue.isRandom();
					boolean loopone = songQueue.isLoopSong();
					songQueue.stop();
					pauseButton.setVisible(false);
					playButton.setVisible(true);
					songQueue = null;
					songQueue = new SongQueue(songLibrary.getSongList());
		            songQueue.addPropertyChangeListener(new PropertyChangeListenerDemo());
		            songQueue.setVolume(volume);
		            songQueue.setRandom(random);
		            songQueue.setLoopSong(loopone);
		            updateSongInfo(); 
		            list.setListData(songQueue.getSongList());
				}
			}
		});

    }

    private void close() {
    	setVisible(false);
        if (songQueue != null) {
            songQueue.stop();
        }
        final Serializer serializer = new Persister();
        final String homeDirectory = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
        final String resource = homeDirectory + "/Library.xml";

        final File result = new File(resource);
        try {
            serializer.write(songLibrary, result);
        } catch (Exception e){
        	System.out.println("Did not save Library");
        }
        running = false;
        dispose();
    }

    public static void main(String[] args) {
        // final SynthLookAndFeel lookAndFeel = new SynthLookAndFeel();

        // try{
        //
        // lookAndFeel.load(new File("C:\\xml\\synthDemo.xml").toURI().toURL());
        // UIManager.setLookAndFeel(lookAndFeel);
        // } catch (Exception e) {
        // int i =9;
        // }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MusicPlayer().setVisible(true);
            }
        });
    }

    @Override
    public void run() {
        // looping text
        while (running) {
            if (songNameString != null) {
                if (songNameString.length() > 15) {
                    if (currentStringCharacter <= songNameString.length()) {
                        final String rest = songNameString.substring(currentStringCharacter);
                        final Image readname = process(rest, songNameSize);
                        final ImageIcon name = new ImageIcon(readname);
                        nameBar.setIcon(name);
                        currentStringCharacter++;
                        try {
                            Thread.sleep(200);
                        } catch (final InterruptedException e) {
                            // error
                        }
                    } else {
                        currentStringCharacter = 0;
                    }
                } else {
                    currentStringCharacter = 0;
                    final Image readname = process(songNameString, songNameSize);
                    final ImageIcon name = new ImageIcon(readname);
                    nameBar.setIcon(name);
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

     class MyScrollBar extends JScrollBar {

        MyScrollBar() {
            super();            
            setUI(new MyScrollUI());
            setBackground(new Color(45,45,45,255));
            
        }
    }
    
    class PositionBarMouseListener implements MouseListener {
        @Override
        public void mouseReleased(MouseEvent arg0) {
            // stuff
        }

        @Override
        public void mousePressed(MouseEvent arg0) {
            // stuff
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
            // stuff
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
            // stuff
        }

        @Override
        public void mouseClicked(MouseEvent arg0) {
            final int x2 = arg0.getX();
            if (songQueue.isPlaying()) {
                songQueue.pause();
                positionBar.setValue((int) (x2 * (1.0 / positionBar.getWidth() * 100)) * 100);
                songQueue.setPosition((float) (x2 * (1.0 / positionBar.getWidth() * 100)) / 100);

                songQueue.play();
            } else {
                positionBar.setValue((int) (x2 * (1.0 / positionBar.getWidth() * 100)) * 100);
                songQueue.setPosition((float) (x2 * (1.0 / positionBar.getWidth() * 100)) / 100);

            }
        }
    }

    class PropertyChangeListenerDemo implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propertyName = evt.getPropertyName();
            if ("newPosition".equals(propertyName)) {
                positionBar.setValue((int) (Float.parseFloat(evt.getNewValue().toString()) * 10000));
            } else if ("nextSong".equals(propertyName)) {
                updateSongInfo();
            } else if ("playing".equals(propertyName)) {
            	playButton.setVisible(!(boolean)evt.getNewValue());
            	pauseButton.setVisible((boolean)evt.getNewValue());
            	updateSongInfo();
            }
        }
    }

    class MouseMotionListenerDemo implements MouseMotionListener {
        @Override
        public void mouseMoved(MouseEvent e) {
            // stuff
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            final Point currCoords = e.getLocationOnScreen();
            setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    }

    class MouseListenerDemo implements MouseListener {

        public MouseListenerDemo() {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // stuff
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // stuff
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // stuff
        }
    }

    class PopUpDemo extends JPopupMenu {
        JMenuItem closeItem;
        JMenuItem fullItem;
        JMenuItem openItem;

        public PopUpDemo() {
            closeItem = new JMenuItem("Close");
            fullItem = new JMenuItem("Full Player");
            openItem = new JMenuItem("Open Folder");
            add(openItem);
            add(fullItem);
            add(closeItem);
            closeItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });
            openItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    songLibraryLoader();
                }
            });

        }
    }

    class PopClickListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPop(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPop(e);
            }
        }

        private void doPop(MouseEvent e) {
            final PopUpDemo menu = new PopUpDemo();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    class CloseActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }

    class VolumeSliderChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            final JSlider source = (JSlider) e.getSource();
           float volume = ((float) source.getValue()) / 100;
            if(songQueue.getVolume() > volume + 0.01 ||songQueue.getVolume() < volume - 0.01  ||songQueue.getVolume()<0.01 || songQueue.getVolume() >0.99){
            	songQueue.setVolume(volume);
            	repaint();
            }
        }
    }

    class VolumeSliderMouseListener implements MouseListener {

        @Override
        public void mouseReleased(MouseEvent arg0) {
            // stuff

        }

        @Override
        public void mousePressed(MouseEvent arg0) {
            // stuff

        }

        @Override
        public void mouseExited(MouseEvent arg0) {
            // stuff

        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
            // stuff

        }

        @Override
        public void mouseClicked(MouseEvent arg0) {
            final int value = arg0.getX();
            volumeSlider.setValue(value);
            songQueue.setVolume((float) value / 100);
        }
    }

    class PreviousSongActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            songQueue.previous();
            positionBar.setValue(0);
            updateSongInfo();
            if (songQueue.isPlaying()) {
                songQueue.play();
            }

        }
    }

    class PlaySongActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            songQueue.play();
            playButton.setVisible(false);
            pauseButton.setVisible(true);
        }
    }

    class PauseSongActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            songQueue.pause();
            pauseButton.setVisible(false);
            playButton.setVisible(true);
        }
    }

    class NextSongActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            songQueue.next();
            positionBar.setValue(0);
            updateSongInfo();
            if (songQueue.isPlaying()) {
                songQueue.play();
            }else{
            	
            	pauseButton.setVisible(false);
            	playButton.setVisible(true);
            }
        }
    }
    
    class LeftLayoutActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            setSize(400, 200);
            volumeSlider.setVisible(true);
            leftLayoutButton.setVisible(false);
            rightLayoutButton.setVisible(true);
        }
    }

    class RightLayoutActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        	volumeSlider.setVisible(false);
            leftLayoutButton.setVisible(true);
            rightLayoutButton.setVisible(false);
        }
    }

    class ShuffleActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        	if(shuffleButton.isSelected()){
        		loopAllButton.setSelected(false);
        		songQueue.setRandom(true);
        	}else{
        		songQueue.setRandom(false);
        	}
        }
    }

    class LoopAllActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        	if(loopAllButton.isSelected()){
        		shuffleButton.setSelected(false);
        		songQueue.setLoopAll(true);
        		
        	}else{
        		songQueue.setLoopAll(false);
        	}	
            
        }
    }

    class LoopSongActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        	if(songQueue.isLoopSong()){
        		songQueue.setLoopSong(false);
        	}else {
        		songQueue.setLoopSong(true);
			}
        }
    }

    class MinimizeActionListener implements ActionListener {
    	@Override
        public void actionPerformed(ActionEvent e) {
    		setExtendedState(ICONIFIED);
    	}
    }
     
}
