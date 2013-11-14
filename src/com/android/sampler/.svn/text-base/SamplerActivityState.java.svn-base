package com.android.sampler;

import android.view.View;

public class SamplerActivityState {
    private boolean isRecording;
    private boolean editMode;
    private boolean loopMode;
    private boolean noRecording = true;
    private final View editButton;
    private final View recButton;
    private final View loopButton;
    private final View saveButton;
    private final View resetButton;
    private final View joinJamButton;
    private final View hostJamButton;

    public SamplerActivityState(View edit, View record, View loop, View save, View reset, View joinJam, View hostJam) {
        this.editButton = edit;
        this.recButton = record;
        this.loopButton = loop;
        this.saveButton = save;
        this.resetButton = reset;
        this.joinJamButton = joinJam;
        this.hostJamButton = hostJam;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean inEditMode() {
        return editMode;
    }

    public boolean inLoopMode() {
        return loopMode;
    }

    /**
     * Sets the UI to the state at which a user should see if they started the app for the first time
     */
    public void cleanState() {
        disableAllModes();
        loopButton.setEnabled(false);
        saveButton.setEnabled(false);
        noRecording = true;
    }

    /**
     * Disables all the UI corresponding to all of the major functionality modes
     */
    public void disableAllModes() {
        isRecording = false;
        editMode = false;
        loopMode = false;

        editButton.setEnabled(true);
        recButton.setEnabled(true);
        resetButton.setEnabled(true);
        joinJamButton.setEnabled(true);
        hostJamButton.setEnabled(true);
        if (!noRecording) {
            loopButton.setEnabled(true);
            saveButton.setEnabled(true);
        }
    }

    /**
     * Resets UI state to fresh, for when reset button is clicked
     */
    public void reset() {
        cleanState();
    }

    /**
     * And I hope you like jammin' too
     */
    public void joinJam() {
        cleanState();
        hostJamButton.setEnabled(false);
        joinJamButton.setEnabled(false);
    }

    /**
     * No more jam for you
     */
    public void hostJam() {
        cleanState();
        hostJamButton.setEnabled(false);
        joinJamButton.setEnabled(false);
    }

    /**
     * Recording UI set to enabled state and everything conflicting to disabled
     */
    public void setRecording() {
        isRecording = true;
        noRecording = false;
        editMode = false;
        loopMode = false;

        disableAllButtons();
        recButton.setEnabled(true);
    }

    /**
     * Sets editing UI to enabled state and everything that conflicts to disabled state
     */
    public void setEditMode() {
        editMode = true;
        isRecording = false;
        loopMode = false;

        disableAllButtons();
        editButton.setEnabled(true);
    }

    /**
     * Sets the loop UI to the enabled state and all contradictory UI state is disabled
     */
    public void setLoopMode() {
        loopMode = true;
        isRecording = false;
        editMode = false;

        disableAllButtons();
        loopButton.setEnabled(true);
        saveButton.setEnabled(true);
    }

    private void disableAllButtons() {
        loopButton.setEnabled(false);
        editButton.setEnabled(false);
        recButton.setEnabled(false);
        resetButton.setEnabled(false);
        joinJamButton.setEnabled(false);
        hostJamButton.setEnabled(false);
        saveButton.setEnabled(false);
    }
}
