package media;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

@SuppressWarnings("serial")
public class MediaLoaderProgressBar extends JFrame implements ActionListener, PropertyChangeListener {

    private JProgressBar progressBar;
    private JPanel panel;
    private Task task;
    private SongLibrary songLibrary;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        int progress = 0;

        @Override
        public Void doInBackground() {

            setProgress(0);

            MetaData metaData;
            final Loader loader = new Loader();
            final File file = loader.load();

            final Collection<File> files = FileUtils.listFiles(
                    file,
                    new RegexFileFilter("^(.*.mp3)"),
                    DirectoryFileFilter.DIRECTORY
                    );
            final List<Song> songList = new ArrayList<Song>();
            int onePercent = files.size() / 100;
            if (onePercent == 0) {
                onePercent = 1;
            }
            int progressSoFar = 0;
            for (final File singleFile : files) {
                metaData = new MetaData(singleFile);
                songList.add(new Song(metaData, singleFile));

                progress++;
                if (progress % onePercent == 0) {
                    setProgress(Math.min(++progressSoFar, 99));
                }

            }

            songLibrary = new SongLibrary(songList);
            setProgress(100);
            pcs.firePropertyChange("libraryLoaded", 0, songLibrary);
            return null;
        }

        /*
         * Executed in event dispatch thread
         */
        @Override
        public void done() {
            // done
        }
    }

    class Loader extends JPanel {

        public Loader() {
            // empty
        }

        public File load() {
            final JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("open");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
            task.firePropertyChange("progress", 0, 100);
            return null;
        }

    }

    public MediaLoaderProgressBar() {

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);

        // Call setStringPainted now so that the progress bar height
        // stays the same whether or not the string is shown.
        progressBar.setStringPainted(true);

        panel = new JPanel();
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        // Display the window.
        pack();
        setVisible(true);
        // progressBar.setIndeterminate(true);
        task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();

    }

    /**
     * Invoked when task's progress property changes.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            final int progress = (Integer) evt.getNewValue();
            progressBar.setIndeterminate(false);
            progressBar.setValue(progress);
            if (progress == 100) {
                dispose();
            }

        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        // TODO Auto-generated method stub
    }

}
