package jestures.core.recognition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import jestures.core.tracking.Tracking;
import jestures.core.view.View;
import jestures.core.view.ViewObserver;
import smile.math.distance.DynamicTimeWarping;

/**
 * Interface for recognition.
 *
 */
public interface Recognition extends Tracking {

    /**
     * Attache the view.
     *
     * @param view
     *            the {@link View}
     */
    void attacheUI(ViewObserver view);

    /**
     * Load the user.
     *
     * @param name
     *            the {@link String} username
     * @return <code>true</code> if is loaded
     * @throws FileNotFoundException
     *             if file not found
     * @throws IOException
     *             the {@link IOException} if can't create user folder
     */
    boolean loadUserProfile(String name) throws FileNotFoundException, IOException;

    /**
     * Get all template (feature vectors) for the selected gesture.
     *
     * @param gestureName
     *            the {@link String} gesture name
     * @return the {@link List} of feature vectors
     */
    List<List<Vector2D>> getGestureDataset(String gestureName);

    /**
     * Get the {@link DynamicTimeWarping} radius.
     *
     * @return the window width of Sakoe-Chiba band in terms of percentage of sequence length.
     */
    double getDtwRadius();

    /**
     * Set the {@link DynamicTimeWarping} radius.
     *
     * @param radius
     *            the window width of Sakoe-Chiba band in terms of percentage of sequence length.
     */
    void setDtwRadius(double radius);

    /**
     * Get the threshold for gesture minimum acceptance.
     * <p>
     * Only gestures, that have a feature vector distance (by DTW) lower than minThreashold, are accepted.
     *
     *
     * @return represents the minimum distance above which a feature vector is accepted
     */
    double getMinDtwThreashold();

    /**
     * Set the threshold for gesture minimum acceptance.
     * <p>
     * Only gestures, that have a feature vector distance (by DTW) lower than minThreashold, are accepted.
     *
     *
     * @param minDtwThreashold
     *            represents the minimum distance above which a feature vector is accepted
     */
    void setMinDtwThreashold(double minDtwThreashold);

    /**
     * Get the threshold for gesture maximum acceptance.
     * <p>
     * Only gestures, that have a feature vector distance (by DTW) greater than minThreashold, are accepted.
     *
     *
     * @return represents the maximum distance above which a feature vector is accepted
     */
    double getMaxDTWThreashold();

    /**
     * Set the threshold for gesture maximum acceptance.
     * <p>
     * Only gestures, that have a feature vector distance (by DTW) greater than minThreashold, are accepted.
     *
     *
     * @param maxDtwThreashold
     *            represents the maximum distance above which a feature vector is accepted
     */
    void setMaxDtwThreashold(double maxDtwThreashold);

    /**
     * Get the update rate of the recognizer.
     *
     * @return the frame value
     */
    int getUpdateRate();

    /**
     * Set the update rate of the recognizer. The rate must be a value that can be devided by the frame length.
     *
     * @param updateRate
     *            the update rate
     */
    void setUpdateRate(int updateRate);

    /**
     * Get the minimum time separation between two gestures.
     * <p>
     * If the time is too short a long gesture can be recognized multiple time according to update rate value
     *
     * @return the time separation in milliseconds, a value usually between 0 and 1000.
     */
    int getMinTimeSeparation();

    /**
     * Set the minimum time separation between two gestures.
     * <p>
     * If the time is too short a long gesture can be recognized multiple time according to update rate value
     *
     * @param minTimeSeparation
     *            the time separation in milliseconds, a value usually between 0 and 1000.
     */
    void setMinTimeSeparation(int minTimeSeparation);

    /**
     * Get the minimum number of gesture that have to match the template to get a gesture recognized.
     *
     * @return the number of templates.
     */
    int getMatchNumber();

    /**
     * Set the minimum number of gesture that have to match the template to get a gesture recognized.
     *
     * @param matchNumber
     *            the number of templates.
     */
    void setMatchNumber(int matchNumber);

}
