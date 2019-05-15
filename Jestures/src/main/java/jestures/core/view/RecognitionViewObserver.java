/*******************************************************************************
 * Copyright (c) 2018 Giulianini Luca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package jestures.core.view;

import jestures.core.codification.GestureLength;
import jestures.core.recognition.gesturedata.RecognitionSettings;

/**
 * Pattern obsrver that extends the view obersver.
 *
 */
public interface RecognitionViewObserver extends ViewObserver {

    /**
     * Update the recognition settings.
     *
     * @param settings
     *            the {@link RecognitionSettings}
     */
    void updateSettings(RecognitionSettings settings);

    /**
     * Triggered when a gesture is recognized.
     *
     * @param gestureName
     *            the gesture name
     */
    void onGestureRecognized(String gestureName);

    /**
     * Set the frame Length.
     *
     * @param length
     *            the {@link GestureLength}
     */
    void setGestureLengthLabel(GestureLength length);
}
