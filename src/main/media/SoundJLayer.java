package media;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

import javazoom.jl.decoder.JavaLayerException;

class SoundJLayer implements Runnable {
    private File filePath;
    private JLayerPlayerPausable player;
    private Thread playerThread;
    private String namePlayerThread = "AudioPlayerThread";
    private Mixer systemMixer;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private float startPosition = 0;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public SoundJLayer(File filePath) {
        this.filePath = filePath;
        final Mixer.Info[] infos = AudioSystem.getMixerInfo();
        for (final Mixer.Info info : infos) {
            final Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(Port.Info.SPEAKER)) {
                systemMixer = mixer;
            }
        }
    }

    public SoundJLayer(File filePath, String namePlayerThread) {
        this.filePath = filePath;
        this.namePlayerThread = namePlayerThread;
    }

    public void play() {
        if (this.player == null) {
            this.playerInitialize();
        }
        else if (!this.player.isPaused() || this.player.isComplete() || this.player.isStopped()) {
            this.stop();
            this.playerInitialize();
        }
        this.playerThread = new Thread(this, namePlayerThread);
        this.playerThread.setDaemon(true);

        this.playerThread.start();

    }

    public void pause() {
        if (this.player != null) {
            this.player.pause();

            if (this.playerThread != null) {
                this.playerThread = null;
            }
        }
    }

    public void fastForward() {
        if (this.player != null) {
            this.player.fastForward(2);
        }
    }

    public void skipForward() {
        if (this.player != null) {
            if (player.isPlaying()) {
                this.player.skipForward(200);
                this.playerThread = new Thread(this, namePlayerThread);
                this.playerThread.setDaemon(true);

                this.playerThread.start();
            } else {
                this.player.skipForward(200);
            }
        }

    }

    public void skipbackward() {
        if (this.player != null) {
            if (player.isPlaying()) {
                this.player.skipBackward(200);

                this.playerThread = new Thread(this, namePlayerThread);
                this.playerThread.setDaemon(true);

                this.playerThread.start();
            } else {
                this.player.skipBackward(200);
            }
        }
    }

    public void pauseToggle() {
        if (this.player != null) {
            if (this.player.isPaused() && !this.player.isStopped()) {
                this.play();
            }
            else {
                this.pause();
            }
        }
    }

    public void stop() {
        if (this.player != null) {
            this.player.stop();
            if (this.playerThread != null) {
                this.playerThread = null;
            }
        }
    }

    public float getPosition() {
        return this.player.getPosition();
    }

    public void setPosition(float position) {
        if (this.player != null) {
            if (player.isPlaying()) {
                this.player.setPosition(position);
                this.playerThread = new Thread(this, namePlayerThread);
                this.playerThread.setDaemon(true);
                this.playerThread.start();
            } else {
                this.player.setPosition(position);
            }
        } else {
            startPosition = position;
        }
    }

    public void changeEqualizer(float[] settings) {
        this.player.setEqualizer(settings);
    }

    private void playerInitialize() {
        try {
            this.player = new JLayerPlayerPausable(this.filePath);
            player.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent e) {

                    if ("newPosition".equals(e.getPropertyName())) {
                        pcs.firePropertyChange("newPosition", 0, e.getNewValue());

                        if (e.getNewValue().equals((float) 1)) {
                            pcs.firePropertyChange("endOfSong", false, true);
                        }
                    }

                }
            });
        } catch (final JavaLayerException e) {
            e.printStackTrace();
        }
    }

    public void setVolume(float volumeValue) {
        if (systemMixer != null) {
            Port port;
            try {
                port = (Port) systemMixer.getLine(Port.Info.SPEAKER);
                port.open();
                if (port.isControlSupported(FloatControl.Type.VOLUME)) {
                    final FloatControl volume = (FloatControl) port.getControl(FloatControl.Type.VOLUME);
                    volume.setValue(volumeValue);
                }
                port.close();
            } catch (final LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public float getVolume() {
        if (systemMixer != null) {
            float value = 0;
            Port port;
            try {
                port = (Port) systemMixer.getLine(Port.Info.SPEAKER);
                port.open();
                if (port.isControlSupported(FloatControl.Type.VOLUME)) {
                    final FloatControl volume = (FloatControl) port.getControl(FloatControl.Type.VOLUME);
                    value = volume.getValue();
                }
                port.close();
                return value;
            } catch (final LineUnavailableException e) {
                e.printStackTrace();
            }
            return value;
        }
        return 0;
    }

    public float getSongLengthMs() {
        if (this.player != null) {
            return this.player.getSongLengthMs();
        }
        return 0;
    }

    // IRunnable members
    @Override
    public void run() {
        try {
            if (startPosition > 0) {
                final int startFrames = (int) (player.getTotalFrames() * startPosition);
                startPosition = 0;
                this.player.resume(startFrames);
            } else {
                this.player.resume(player.getCurrentFrame());
            }
        } catch (final javazoom.jl.decoder.JavaLayerException ex) {
            ex.printStackTrace();
        }
    }

}
