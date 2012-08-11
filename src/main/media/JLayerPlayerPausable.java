package media;

/* *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *   Original by: http://thiscouldbebetter.wordpress.com/2011/07/04/pausing-an-mp3-file-using-jlayer/
 *   Last modified: 21-jul-2012 by Arthur Assuncao
 *----------------------------------------------------------------------
 */

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Equalizer;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

//use with JLayerPausableTest
public class JLayerPlayerPausable {

    private File audioPath;
    private Bitstream bitstream;
    private Decoder decoder;
    private AudioDevice audioDevice;
    private boolean closed;
    private boolean complete;
    private boolean paused;
    private boolean stopped;
    private int fastForward;
    private int frameIndexCurrent;
    private final int lostFrames = 20; // some fraction of a second of the sound
                                       // gets "lost" after every pause. 52 in
                                       // original code
    private int totalFrames;
    private float frameLengthMs;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean ended;

    public boolean getEnded() {
        return ended;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public JLayerPlayerPausable(File audioPath) throws JavaLayerException {
        this.audioPath = audioPath;
        setBitStreamAndDecoder();
        Header header = this.bitstream.readFrame();
        frameLengthMs = header.ms_per_frame();
        boolean shouldContinueReadingFrames = true;
        ended = false;
        while (shouldContinueReadingFrames == true && this.totalFrames < Integer.MAX_VALUE) {
            shouldContinueReadingFrames = this.skipFrame();
            this.totalFrames++;
        }
        header = null;
        this.bitstream.close();
        this.decoder = null;
    }

    private InputStream getAudioInputStream() throws IOException {
        if (this.audioPath != null) {
            return new FileInputStream(this.audioPath);
        }
        return null;
    }

    public boolean play() throws JavaLayerException {
        return this.play(0);
    }

    public boolean play(int frameIndexStart) throws JavaLayerException {
        return this.play(frameIndexStart, -1, lostFrames);
    }

    public boolean play(int frameIndexStart, int frameIndexFinal, int correctionFactorInFrames)
            throws JavaLayerException {

        setBitStreamAndDecoder();
        this.audioDevice = FactoryRegistry.systemRegistry().createAudioDevice();
        this.audioDevice.open(this.decoder);

        boolean shouldContinueReadingFrames = true;
        this.paused = false;
        this.stopped = false;
        this.fastForward = 0;
        this.frameIndexCurrent = 0;

        while (shouldContinueReadingFrames == true
                && this.frameIndexCurrent < frameIndexStart - correctionFactorInFrames) {
            shouldContinueReadingFrames = this.skipFrame();
            this.frameIndexCurrent++;
        }

        while (shouldContinueReadingFrames == true && this.frameIndexCurrent < totalFrames) {
            if (this.paused || this.stopped) {
                shouldContinueReadingFrames = false;
                try {
                    Thread.sleep(1);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                shouldContinueReadingFrames = this.decodeFrame();
                this.frameIndexCurrent++;
                this.pcs.firePropertyChange("newPosition", 0, getPosition());
            }
        }

        // last frame, ensure all data flushed to the audio device.
        if (this.audioDevice != null && !this.paused) {
            this.audioDevice.flush();
            synchronized (this) {
                this.complete = (this.closed == false);
                this.close();
            }

        }

        return shouldContinueReadingFrames;
    }

    private void setBitStreamAndDecoder() {
        try {
            this.bitstream = new Bitstream(this.getAudioInputStream());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        this.decoder = new Decoder();
    }

    public boolean resume(int frame) throws JavaLayerException {
        fastForward = 0;
        return this.play(frame);
    }

    public synchronized void close() {
        if (this.audioDevice != null) {
            this.closed = true;
            this.audioDevice.close();
            this.audioDevice = null;
            try {
                this.bitstream.close();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected boolean decodeFrame() throws JavaLayerException {
        boolean returnValue = false;
        if (this.stopped) { // nothing for decode
            return false;
        }
        try {
            if (this.audioDevice != null) {
                final Header header = this.bitstream.readFrame();
                if (header != null) {
                    // sample buffer set when decoder constructed
                    SampleBuffer output = (SampleBuffer) this.decoder.decodeFrame(header, this.bitstream);
                    if (fastForward > 0) {
                        final double fast = fastForward / 10.0;
                        final short[] buffer = output.getBuffer();
                        final SampleBuffer output2 = new SampleBuffer(output.getSampleFrequency(),
                                output.getChannelCount());
                        int i = 0;
                        final short[] newBuffer = new short[(int) (buffer.length * (1 - fast))];
                        for (int j = 0; j < buffer.length; j += 2) {
                            if (j % (1 / fast) != 0) {
                                newBuffer[i] = buffer[j];
                                newBuffer[i + 1] = buffer[j + 1];
                                i += 2;
                            }
                        }
                        i = 0;
                        for (final short value : newBuffer) {
                            output2.append(i, value);
                            if (i == 1) {
                                i = 0;
                            } else {
                                i = 1;
                            }
                        }
                        output = output2;
                    }
                    synchronized (this) {
                        if (this.audioDevice != null) {
                            this.audioDevice.write(output.getBuffer(), 0, output.getBufferLength());
                        }
                    }
                    this.bitstream.closeFrame();
                    returnValue = true;
                } else {
                    ended = true;
                    System.out.println("End of file"); // end of file
                    returnValue = false;
                }
            }
        } catch (final RuntimeException ex) {
            throw new JavaLayerException("Exception decoding audio frame", ex);
        }
        return returnValue;
    }

    public void pause() {
        if (!this.stopped) {
            this.paused = true;
            this.close();
        }
    }

    public boolean skipForward(int skip) {
        if (!this.stopped) {
            if (this.paused == true) {
                this.frameIndexCurrent = Math.min(this.frameIndexCurrent + skip, totalFrames);
            } else {
                this.paused = true;
                this.frameIndexCurrent = Math.min(this.frameIndexCurrent + skip, totalFrames);
                this.close();
            }
        }
        return true;
    }

    public boolean skipBackward(int skip) {
        if (!this.stopped) {
            if (this.paused == true) {
                this.frameIndexCurrent = Math.max(this.frameIndexCurrent - skip, 0);
            } else {
                this.paused = true;
                this.frameIndexCurrent = Math.max(this.frameIndexCurrent - skip, 0);
                this.close();
            }
        }
        return true;
    }

    public void fastForward(int rate) {
        if (!this.stopped && !this.paused) {
            if (this.fastForward > 0) {
                this.fastForward = 0;
            } else {
                this.fastForward = rate;
            }
        }
    }

    protected boolean skipFrame() throws JavaLayerException {
        if (this.bitstream.readFrame() != null) {
            this.bitstream.closeFrame();
            return true;
        }
        return false;
    }

    public void stop() {
        if (!this.stopped) {
            if (!this.closed) {
                this.close();
            }
            this.stopped = true;
            this.frameIndexCurrent = 0;
        }
    }

    public void setEqualizer(float[] settings) {
        final Equalizer eq = new Equalizer(settings);
        this.decoder.setEqualizer(eq);
    }

    public void setPosition(float position) {
        if (!this.stopped) {
            if (this.paused == true) {
                this.frameIndexCurrent = (int) (totalFrames * position);
            } else {
                this.paused = true;
                this.frameIndexCurrent = (int) (totalFrames * position);
                this.close();
            }
        }
    }

    public float getPosition() {
        return (float) this.frameIndexCurrent / (float) this.totalFrames;
    }

    public float getSongLengthMs() {
        return this.totalFrames * frameLengthMs;
    }

    /**
     * @return the paused
     */
    public boolean isPlaying() {
        return !paused && !complete && !stopped;
    }

    /**
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @return the complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * @return the paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * @return the fastForward
     */

    public boolean isFastForward() {
        return fastForward > 0;
    }

    /**
     * @return the stopped
     */
    public boolean isStopped() {
        return stopped;
    }

    public boolean isEnded() {
        return isEnded();
    }

    public int getCurrentFrame() {
        return frameIndexCurrent;
    }

    public int getTotalFrames() {
        return totalFrames;
        // TODO Auto-generated method stub

    }

}