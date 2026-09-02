package com.ceos.map.ui;

import java.io.File;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/** 
 * Select a .bob file
*/
public class MarkerDialog extends Dialog<Boolean> {

    private final TextField displayField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField iconField = new TextField();

    public MarkerDialog(Double lat, Double lon) {
        setTitle("Setup Marker");
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("/icons/marker.png")); 
        
        setHeaderText("Choose Phoebus display binding.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name: "), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label(".bob file:"), 0, 1);
        displayField.setEditable(false);
        grid.add(displayField, 1, 1);
        filePicker(grid);

        grid.add(new Label("Icon image:"), 0, 2);
        iconField.setEditable(false);
        grid.add(iconField, 1, 2);
        iconPicker(grid);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        grid.add(new Label("Latitude: " + lat), 0, 3);
        grid.add(new Label("Longitude: " + lon), 0, 4);

        setResultConverter(button -> {
            if (button == ButtonType.OK) {
                
                return true;
            }
            return false;
        });
    }
    
    public String getDisplay(){
        String display = this.displayField.getText();
        if(display != null){
            return display;
        }
        return "";
    }

    public String getName(){
        String name = this.nameField.getText();
        if(name != null){
            return name;
        }
        return "";
    }

    public String getIcon(){
        String icon = this.iconField.getText();
        if(icon != null){
            return icon;
        }
        return "";
    }
    
    private void filePicker(GridPane grid){
        Button browse = new Button("...");
        
        browse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose display");
            
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Phoebus Display", "*.bob")
            );
            
            File selected = fc.showOpenDialog(getDialogPane().getScene().getWindow());
            
            if (selected != null){
                displayField.setText(selected.getName());
            }
        });
        
        grid.add(browse, 2, 1);
    }

    private void iconPicker(GridPane grid){
        Button browse = new Button("...");

        browse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose icon image");

            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );

            File selected = fc.showOpenDialog(getDialogPane().getScene().getWindow());

            if (selected != null){
                iconField.setText(selected.getAbsolutePath());
            }
        });

        grid.add(browse, 2, 2);
    }
}
