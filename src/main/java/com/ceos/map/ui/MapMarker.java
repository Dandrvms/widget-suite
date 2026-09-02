package com.ceos.map.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.runtime.script.ScriptUtil;

/**
 *
 * @author Daniel
 *
 * Location Marker
 *
 */
public class MapMarker extends Group {

    private String display = "";
    private Map<String, String> macros = new HashMap<>();

    public MapMarker(String iconPath) {
        this.setPickOnBounds(false);

        Image image = getImage(iconPath);

        if (image != null) {
            ImageView imagev = new ImageView(image);
            imagev.setFitWidth(32);
            imagev.setFitHeight(32);
            imagev.setPreserveRatio(true);
            this.getChildren().add(imagev);
        } else {
            Circle circle = new Circle(7, Color.RED);
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2);
            this.getChildren().add(circle);
        }
    }

    private Image getImage(String iconPath) {
        if (iconPath != null && !iconPath.isEmpty()) {
            File file = new File(iconPath);
            if (file.exists()) {
                try {
                    return new Image(file.toURI().toString());
                } catch (Exception e) {
                    System.out.println("No se pudo cargar el icono custom: " + iconPath);
                }
            }
        }
        try {
            return new Image(getClass().getResourceAsStream("/icons/marker.png"));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono por defecto");
            return null;
        }
    }

    public void openDisplay(Widget widget) {
        if (display == null || display.isEmpty()) {
            System.out.println("No hay ninguna ruta configurada en este marcador.");
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Something went wrong");
            alert.setContentText("No display has been set for this marker");

            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("/icons/exclamation.png"));
            alert.showAndWait();
            return;
        }

        try {
            ScriptUtil.openDisplay(widget, display, "TAB", macros);
        } catch (Exception e) {
            System.out.println("--- ERROR AL ABRIR PANTALLA ---");
            System.out.println("Intentando abrir: " + display);

            e.printStackTrace();
            System.out.println("-------------------------------");
        }
    }

    public void setDisplay(String path) {
        this.display = path;
    }

    public String getDisplay() {
        return this.display;
    }

    public void setMacros(Map<String, String> macros) {
        this.macros = macros;
    }

    public Map<String, String> getMacros() {
        return this.macros;
    }
}
