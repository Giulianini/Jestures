/*******************************************************************************
 * Copyright (c) 2018 Giulianini Luca Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *******************************************************************************/

package recorder.view;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.Logger;
import org.kordamp.ikonli.material.Material;

import com.google.gson.JsonSyntaxException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.controls.JFXTreeView;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jestures.core.codification.GestureLength;
import jestures.core.file.FileManager;
import jestures.core.recognition.gesturedata.DefaultGesture;
import jestures.core.view.enums.DialogsType.DimDialogs;
import jestures.core.view.enums.IconDim;
import jestures.core.view.enums.NotificationType;
import jestures.core.view.enums.NotificationType.Duration;
import jestures.core.view.utils.ListViewFactory;
import jestures.core.view.utils.ViewUtilities;
import recorder.controller.Recorder;

/**
 * Javafx controller for fxml file.
 */
public class RecorderScreenView extends AbstractRecorderScreenView implements RecordingView {
    private static final Logger LOG = Logger.getLogger(RecorderScreenView.class);
    private final Recorder recorder;
    private GestureLength gestureLength;

    // VIEW
    private Stage stage; // NOPMD
    private Scene scene; // NOPMD

    // TREE VIEW
    private TreeItem<String> root; // NOPMD

    // ########### ALL TABS #############
    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private HBox gestureHBox;
    @FXML
    private BorderPane recorderPane; // NOPMD
    @FXML
    private JFXButton startButton;
    @FXML
    private JFXComboBox<String> gestureComboBox;
    @FXML
    private JFXButton addGestureButton; // NOPMD
    @FXML
    private JFXScrollPane userScrollPane;
    // ########### TAB 1 #############
    @FXML
    private JFXTreeView<String> treeView;
    @FXML
    private BorderPane userBorderPane; // NOPMD
    @FXML
    private JFXComboBox<String> selectUserCombo;
    @FXML
    private ComboBox<GestureLength> frameLengthCombo;

    // ########### TAB 2 #############

    // ########### TAB 3 #############

    // ########### TAB 4 #############
    @FXML
    private JFXScrollPane scrollPane;
    @FXML
    private JFXListView<BorderPane> listView;

    /**
     * @param recorder
     *            the {@link RecorderScreenView}
     */
    public RecorderScreenView(final Recorder recorder) {
        super(recorder);
        this.recorder = recorder;
        this.gestureLength = recorder.getFrameLength();
        // CREATE AND SET THE CONTROLLER. INIT THE BORDER PANE
        Platform.runLater(() -> {
            final FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            loader.setLocation(this.getClass().getResource(FXMLScreens.HOME.getPath()));
            try {
                this.recorderPane = (BorderPane) loader.load();
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        });
        RecorderScreenView.LOG.getClass();
    }

    @Override
    @FXML
    public final void initialize() { // NOPMD
        super.initialize();
        // CREATION OF STAGE SCENE AND PANE
        this.stage = new Stage();
        this.scene = new Scene(this.recorderPane);
        this.stage.setScene(this.scene);
        // SETTING EXIT ACTIONS
        this.stage.setOnCloseRequest(e -> {
            this.stopSensor();
            Platform.exit();
            System.exit(0);
        });
        // CHARGE THE CSS
        this.chargeSceneSheets(FXMLScreens.HOME);
        this.stage.show();

    }

    private void chargeSceneSheets(final FXMLScreens screen) {
        this.scene.getStylesheets().add(RecorderScreenView.class.getResource(screen.getCssPath()).toString());
    }

    // ############################################## FROM TRACKER (VIEW OBSERVER) ###################################
    // KINECT
    // ######## TAB 2 AND 3 #########
    @Override
    public final void notifyOnFrameChange(final int frame, final Vector3D derivative, final Vector3D path) {
        Platform.runLater(() -> {
            if (frame == 0) {
                this.clearCanvasAndChart();
            }
            this.getxSeries().getData().add(new XYChart.Data<Number, Number>(frame, (int) derivative.getX()));
            this.getySeries().getData().add(new XYChart.Data<Number, Number>(frame, (int) derivative.getY()));

            this.getLiveContext().fillOval(-path.getX() + this.getLiveCanvas().getWidth() / 2,
                    path.getY() + this.getLiveCanvas().getHeight() / 2, 10, 10);
            this.progressBar.setProgress(frame / (this.gestureLength.getFrameNumber() + 0.0));
        });
    }

    @Override
    public final void notifyOnFeatureVectorEvent() {
        Platform.runLater(() -> {
            this.addFeatureVectorToListView(this.listView.getItems().size(),
                    this.getLiveCanvas().snapshot(new SnapshotParameters(), null));
        });
    }

    // TRACKER
    @Override
    public final void refreshUsers() {
        Platform.runLater(() -> {
            try {
                this.selectUserCombo.getItems().clear();
                FileManager.getAllUserFolder().stream().forEachOrdered(t -> this.selectUserCombo.getItems().add(t));
            } catch (final IOException e) {
                ViewUtilities.showNotificationPopup("Exception", "Cannot read user data", Duration.MEDIUM,
                        NotificationType.WARNING, null);
            }
        });
    }

    // ###################################### FROM RECORDER (REC VIEW OBSERVER) ##################################

    @Override
    public final void setRecording(final boolean isRecording) {
        if (isRecording) {
            Platform.runLater(() -> {
                ViewUtilities.showSnackBar((Pane) this.recorderPane.getCenter(), "Record is started", Duration.MEDIUM,
                        DimDialogs.SMALL, null);
            });
        } else {
            Platform.runLater(() -> {
                ViewUtilities.showSnackBar((Pane) this.recorderPane.getCenter(), "Record is stopped", Duration.MEDIUM,
                        DimDialogs.SMALL, null);
            });
        }
    }

    @Override
    public final void setGuiGestureLenght(final GestureLength gestureLength) {
        Platform.runLater(() -> {
            this.frameLengthCombo.setValue(gestureLength);
            this.gestureLength = gestureLength;
        });
    }

    // ############################################## TO TRACKER ###################################
    @Override
    public final void startSensor() {
        this.selectUserCombo.setDisable(true);
        this.gestureHBox.setDisable(true);
        this.recorder.startSensor();
    }

    @Override
    public final void stopSensor() {
        this.clearCanvasAndChart();
        this.recorder.stopSensor();
        this.selectUserCombo.setDisable(false);
        this.gestureHBox.setDisable(false);
    }

    @Override
    public final void setUserGestureLength(final GestureLength length) {
        try {
            this.recorder.setUserGestureLength(length);
            this.setChart(length.getFrameNumber(), length.getFrameNumber());
            this.gestureLength = length;
        } catch (final Exception e) {
            ViewUtilities.showNotificationPopup("Gesture Length Error", e.getMessage() + ": " + this.gestureLength,
                    Duration.MEDIUM, NotificationType.WARNING, null);
            this.frameLengthCombo.getSelectionModel().select(this.recorder.getUserGestureLength());
        }
    }

    @Override
    public final void selectGesture(final String gesture) {
        this.startButton.setDisable(false);
    }

    @Override
    public final void setSensorElevation(final int angle) {
        this.recorder.setElevationAngle(angle);
    }

    // ############################################## TO RECORDER ###################################
    // ###### TAB 1 ######
    @Override
    public final void createUserProfile(final String username) {
        try {
            if (!this.recorder.createUserProfile(username)) {
                this.setUserGestureLength(this.recorder.getUserGestureLength());
                ViewUtilities.showNotificationPopup("Cannot create User", username + " already exists", Duration.MEDIUM,
                        NotificationType.WARNING, null);
            } else {
                ViewUtilities.showNotificationPopup("User Created", username + " created!", Duration.MEDIUM,
                        NotificationType.SUCCESS, null);
                // IF USER IS LOADED CORRECLY ENABLE BUTTONS
                this.gestureHBox.setDisable(false);
                this.frameLengthCombo.setDisable(false);
                this.refreshUsers();
                this.selectUserCombo.getSelectionModel().select(username);
            }
        } catch (final IOException e) {
            ViewUtilities.showNotificationPopup("Io  Exception", "Cannot create user file. \nClick for info",
                    Duration.LONG, NotificationType.ERROR, t -> e.printStackTrace());
        }
    }

    @Override
    public final void deleteSelectedUserProfile() {
        ViewUtilities.showConfirmDialog(this.userScrollPane, "Delete User",
                "Delete user " + this.recorder.getUserName() + "?", DimDialogs.MEDIUM, (final Event event) -> {
                    if (((JFXButton) event.getSource()).getText().equals("YES")) {
                        try {

                            this.recorder.deleteUserProfile();
                            this.selectUserCombo.getItems().remove(this.recorder.getUserName());
                            this.selectUserCombo.getSelectionModel().select(0);
                        } catch (final IOException e) {
                            ViewUtilities.showNotificationPopup("Io Exception",
                                    "Cannot delete user file. \nClick for info", Duration.LONG, NotificationType.ERROR,
                                    t -> e.printStackTrace());
                        }
                    }
                });
    }

    @Override
    public final void loadUserProfile(final String name) {
        try {
            this.recorder.loadUserProfile(name);
        } catch (final IOException e1) {
            ViewUtilities.showNotificationPopup("User Dataset not found", "Regenerating it", Duration.MEDIUM, // NOPMD
                    NotificationType.ERROR, t -> e1.printStackTrace());
        } catch (final JsonSyntaxException e2) {
            ViewUtilities.showNotificationPopup("Json file changed by human!", "Please click to se exception",
                    Duration.MEDIUM, // NOPMD
                    NotificationType.ERROR, t -> e2.printStackTrace());
        }
        ((Label) this.userScrollPane.getBottomBar().getChildren().get(0)).setText(name);
        // IF USER IS LOADED CORRECLY ENABLE BUTTONS
        this.gestureHBox.setDisable(false);
        // SAVE COPY OF GESTURES LOAD USER WITH GESTURE AND UPDATE THE GESTURES
        final Set<String> set = new HashSet<>();
        // DELETE GESTURES
        this.gestureComboBox.getItems().clear();
        // LOAD USER GESTURES
        set.addAll(this.recorder.getAllUserGesture());
        set.addAll(DefaultGesture.getAllDefaultGestures());
        this.gestureComboBox.getItems().addAll(set);
        Collections.sort(this.gestureComboBox.getItems());
        ViewUtilities.showSnackBar((Pane) this.recorderPane.getCenter(), "Database loaded and Gesture updated!",
                Duration.MEDIUM, DimDialogs.SMALL, null);

        this.createGestureTreeView(this.recorder.getUserName());
        this.startButton.setDisable(true);
        this.frameLengthCombo.setDisable(false);
    }

    /**
     * Delete the gesture.
     */
    @Override
    public void deleteGesture() {
        final String gesture = this.gestureComboBox.getSelectionModel().getSelectedItem();
        if (gesture != null) {
            try {
                this.recorder.deleteGestureDataset(gesture);
                this.gestureComboBox.getItems().remove(gesture);
                // REFRESH TREEVIEW
                this.createGestureTreeView(this.recorder.getUserName());
            } catch (final IOException e1) {
                ViewUtilities.showNotificationPopup("Error deleting gesture", "Cannot delete" + gesture + " gesture",
                        Duration.MEDIUM, NotificationType.ERROR, null);
            }
        } else {
            ViewUtilities.showNotificationPopup("Gesture not selected", "Select a gesture!", Duration.MEDIUM,
                    NotificationType.WARNING, null);
        }

    }

    // ################# TREE VIEW ###################
    // CANVAS GESTURE
    @Override
    public final void drawSavedGestureOnCanvas(final TreeItem<String> gestureItem, final int templateIndex) {
        final List<Vector3D> template = this.recorder.getGestureDataset(gestureItem.getValue()).get(templateIndex);
        this.getUserCanvasContext().clearRect(0, 0, this.getLiveCanvas().getWidth(), this.getLiveCanvas().getHeight());
        for (final Vector3D path : template) {
            this.getUserCanvasContext().fillOval(-path.getX() + this.getLiveCanvas().getWidth() / 2,
                    path.getY() + this.getLiveCanvas().getHeight() / 2, 10, 10);
        }
        this.getCnavasPopup().show(this.recorderPane);

    }

    // ###### TAB 4 ######
    @Override
    public final void deleteFeatureVectorInLIstView(final int indexClicked) {
        this.listView.getItems().remove(indexClicked);
        this.recorder.deleteRecordedFeatureVector(indexClicked);
        this.scrollPane.setContent(this.listView);
    }

    @Override
    public final void clearListView() {
        this.listView.getItems().clear();
        this.recorder.clearRecordedDataset();
        this.scrollPane.setContent(this.listView);
    }

    @Override
    public final void addFeatureVectorToDataset(final String gesture, final int indexClicked) {
        try {
            this.recorder.addFeatureVector(this.getGesture(), indexClicked);
            // REFRESH TREEVIEW
            this.createGestureTreeView(this.recorder.getUserName());
        } catch (final IOException e2) {
            ViewUtilities.showNotificationPopup("Io Exception", "Cannot serialize file", Duration.MEDIUM,
                    NotificationType.ERROR, t -> e2.printStackTrace());
        }
        this.deleteFeatureVectorInLIstView(indexClicked);
    }

    @Override
    public final void addAllElemInListViewToDataset() {
        try {
            this.recorder.addAllFeatureVectors(this.getGesture());
            // REFRESH TREEVIEW
            this.createGestureTreeView(this.recorder.getUserName());
        } catch (final IOException e2) {
            ViewUtilities.showNotificationPopup("Io Exception", "Cannot serialize file", Duration.MEDIUM,
                    NotificationType.ERROR, t -> e2.printStackTrace());
        }
        this.clearListView();
    }

    // ############################################## INSTANCE METHODS ###################################
    private void addFeatureVectorToListView(final int index, final Image image) {
        ListViewFactory.addVectorToListView(this.listView, image, index);

        // ON CLICK ACTION
        this.listView.setOnMouseClicked(t -> {
            final int indexClicked = this.listView.getSelectionModel().getSelectedIndex();
            if (t.getButton().equals(MouseButton.PRIMARY) && indexClicked != -1) {

                ViewUtilities.showConfirmDialog(this.scrollPane, "Save",
                        "Save the feature vector N: " + indexClicked + "?", DimDialogs.MEDIUM, (final Event event) -> {
                            if (((JFXButton) event.getSource()).getText().equals("YES")) {
                                try {
                                    this.addFeatureVectorToDataset(this.getGesture(), indexClicked);
                                } catch (final Exception e) {
                                    ViewUtilities.showNotificationPopup("Gesture Error", "Select Gesture!",
                                            Duration.MEDIUM, NotificationType.ERROR, k -> e.printStackTrace());
                                }
                            }
                        });

            } else if (indexClicked != -1) {
                this.deleteFeatureVectorInLIstView(indexClicked);
            }

        });
    }

    // 2 THREAD, UI AND KINECT
    private synchronized void clearCanvasAndChart() {
        this.getLiveContext().clearRect(0, 0, this.getLiveCanvas().getWidth(), this.getLiveCanvas().getHeight());
        this.getxSeries().getData().clear();
        this.getySeries().getData().clear();
    }

    private String getGesture() {
        final int i = this.gestureComboBox.getSelectionModel().getSelectedIndex();
        if (i != -1) {
            return this.gestureComboBox.getSelectionModel().getSelectedItem();
        } else {
            throw new IllegalStateException();
        }
    }

    private void createGestureTreeView(final String root) {
        // REGENERATE TREVIEW FOR USER
        this.root = new TreeItem<String>(root);
        this.root.setExpanded(true);
        this.treeView.setRoot(this.root);
        final List<String> userGestures = this.recorder.getAllUserGesture();
        for (int i = 0; i < userGestures.size(); i++) {
            this.makeGestureBranch(userGestures.get(i), this.root);
        }
    }

    // CREO UN TREEELEM PER OGNI GESTURE
    private TreeItem<String> makeGestureBranch(final String gestureName, final TreeItem<String> parent) {
        final TreeItem<String> item = new TreeItem<>(gestureName);
        item.setGraphic(ViewUtilities.iconSetter(Material.GESTURE, IconDim.SMALL));
        // RICORSIVA CREO TUTTO L'ALBERO
        final List<List<Vector3D>> gestureDataset = this.recorder.getGestureDataset(gestureName);
        for (int i = 0; i < gestureDataset.size(); i++) {
            this.makeTemplateBranch("Template: " + (i + 1), item);
        }
        parent.getChildren().add(item);
        return item;
    }

    // CREO UN TREEELEM PER OGNI TEMPLATE
    private TreeItem<String> makeTemplateBranch(final String gestureName, final TreeItem<String> parent) {
        final TreeItem<String> item = new TreeItem<>(gestureName);
        item.setGraphic(ViewUtilities.iconSetter(Material.SHOW_CHART, IconDim.SMALL));
        parent.getChildren().add(item);
        return item;
    }

}
