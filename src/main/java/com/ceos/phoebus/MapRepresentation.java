package com.ceos.phoebus;

import com.ceos.map.model.MarkerData;
import com.ceos.map.ui.EditDialog;
import com.ceos.map.ui.MapNode;
import com.ceos.map.ui.MarkerDialog;
import java.util.Optional;
import javafx.application.Platform;
import org.csstudio.display.builder.model.*;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;

/**
 *
 * @author Daniel
 *
 * Returns the actual javafx node for phoebus
 *
 */
public class MapRepresentation extends JFXBaseRepresentation<MapNode, MapWidget> {
    private DirtyFlag dirty_look = new DirtyFlag();

    private final UntypedWidgetPropertyListener contentChangedListener = this::contentChanged;
    private final UntypedWidgetPropertyListener markerListener = (prop, old, val) -> Platform.runLater(this::setupMarkers);


    @Override
    protected MapNode createJFXNode() throws Exception {
        return new MapNode(model_widget);
    }

    @Override
    public void updateChanges() {
        super.updateChanges();

        final MapWidget model = model_widget;
        final MapNode node = jfx_node;

        int height = model.propHeight().getValue();
        int width = model.propWidth().getValue();

        node.setPrefHeight(height);
        node.setPrefWidth(width);
        node.setMarkers(model.getMarkers());
        node.setHost(model.propHost().getValue());

    }

    @Override
    public void registerListeners() {
        super.registerListeners();
        final MapWidget model = model_widget;
        final MapNode node = jfx_node;

        model.propWidth().addUntypedPropertyListener(contentChangedListener);
        model.propHeight().addUntypedPropertyListener(contentChangedListener);
        model.propCoords().addUntypedPropertyListener(contentChangedListener);
        model.propHost().addUntypedPropertyListener(contentChangedListener);

        attachListeners();

        boolean isEditMode = toolkit.isEditMode();
        node.setEditMode(isEditMode);
        if (isEditMode) {
            node.setOnAddMarker((lat, lon)
                    -> Platform.runLater(() -> {
                        try {
                            MarkerDialog d = new MarkerDialog(lat, lon);
                            Optional<Boolean> result = d.showAndWait();
                            if (result.isPresent() && result.get()) {
                                addMarkerToModel(lat, lon, d.getDisplay(), d.getName(), d.getIcon());
                            }
                        } catch (Exception ex) {
                            System.getLogger(MapRepresentation.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                    })
            );

            node.setOnEditMarker(index
                    -> Platform.runLater(() -> {
                        try {
                            MarkerData marker = model_widget.getMarker(index);
                            EditDialog e = new EditDialog(marker.getPoint().getLatitude(), marker.getPoint().getLongitude(), marker.getName(), marker.getDisplayPath(), marker.getIconPath());
                            Optional<Boolean> result = e.showAndWait();
                            if(result.isPresent() && result.get()) {
                                model_widget.updateMarker(index, e.getName(), e.getDisplay(), e.getIcon());
                            }
                        } catch (Exception e) {
                            System.getLogger(MapRepresentation.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
                        }
                    })
            );

            node.setOnMoveMarker((index, marker) -> Platform.runLater(() -> {
                try{
                    StructuredWidgetProperty updated = model_widget.updateMarkerPosition(index, marker);
                    for (WidgetProperty<?> prop : updated.getValue()) {
                        prop.addUntypedPropertyListener(markerListener);
                    }
                    dirty_look.mark();
                } catch(Exception e) {
                    System.getLogger(MapRepresentation.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
                }
            }));

            node.setOnDeleteMarker(index
                    -> Platform.runLater(() -> {
                        try {
                            model_widget.removeMarker(index);
                        } catch (Exception ex) {
                            System.getLogger(MapRepresentation.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                    })
            );
        }
    }

    private void setupMarkers() {
        jfx_node.setMarkers(model_widget.getMarkers());
    }

    private void attachListeners() {
        for (StructuredWidgetProperty marker : model_widget.propCoords().getValue()) {
            for (WidgetProperty<?> prop : marker.getValue()) {
                prop.addUntypedPropertyListener(markerListener);
            }
        }
    }

    private void contentChanged(final WidgetProperty<?> prop, final Object old, Object val) {
        dirty_look.mark();
        toolkit.scheduleUpdate(this);
    }

    private void addMarkerToModel(double lat, double lon, String display, String name, String iconPath) throws Exception {
        StructuredWidgetProperty newMarker = model_widget.addMarker(lat, lon, display, name, iconPath);
        for (WidgetProperty<?> p : newMarker.getValue()) {
            p.addUntypedPropertyListener(markerListener);
        }
    }

    @Override
    protected void unregisterListeners() {
        model_widget.propWidth().removePropertyListener(contentChangedListener);
        model_widget.propHeight().removePropertyListener(contentChangedListener);
        model_widget.propCoords().removePropertyListener(contentChangedListener);

        for (StructuredWidgetProperty marker : model_widget.propCoords().getValue()) {
            for (WidgetProperty<?> prop : marker.getValue()) {
                prop.removePropertyListener(markerListener);
            }
        }
        super.unregisterListeners();
    }
    
    @Override
    public void dispose(){
        jfx_node.dispose();
        super.dispose();
    }

}
