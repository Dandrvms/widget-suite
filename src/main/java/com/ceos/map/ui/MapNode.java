/*
 * Copyright (c) 2018, 2023, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ceos.map.ui;

import com.ceos.map.model.MarkerData;
import com.ceos.map.model.PoiLayer;
import com.ceos.map.server.LocalTileRetriever;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

import java.util.logging.Level;

import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import org.csstudio.display.builder.model.Widget;

/**
 *
 * @author Daniel
 *
 * JavaFX Node that contains the map
 *
 */
public class MapNode extends StackPane {

    private final int maxZoom = 15;
    private Widget widget;

    private static final Logger LOGGER = Logger.getLogger(MapNode.class.getName());

    static {

        String[] loggers = {"com.gluonhq", "com.gluonhq.maps", "com.gluonhq.impl.maps"};
        for (String l : loggers) {
            Logger logger = Logger.getLogger(l);
            logger.setLevel(Level.OFF);
            for (java.util.logging.Handler handler : Logger.getLogger("").getHandlers()) {
                if (handler instanceof java.util.logging.ConsoleHandler) {
                }
            }
        }
    }

    private boolean editMode = false;
    private BiConsumer<Double, Double> onAddMarker;
    private IntConsumer onDeleteMarker;
    private IntConsumer onEditMarker;
    private BiConsumer<Integer, MarkerData> onMoveMarker;

    private final MapView view = new MapView();
    private final PoiLayer markerLayer = new PoiLayer();

    private final ContextMenu addMenu = new ContextMenu();
    private final ContextMenu editMenu = new ContextMenu();


    private void init() {
        this.setPickOnBounds(true);
        this.setPrefSize(400, 400);
        view.addLayer(markerLayer);
        view.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (editMode) e.consume();
            if (addMenu.isShowing()) {
                addMenu.hide();
            }
            if (editMenu.isShowing()) {
                editMenu.hide();
            }
        });

        view.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (editMode) e.consume();
        });

        // Venezuela
        MapPoint country = new MapPoint(7.0, -66.0);
        view.setZoom(6);
        view.setCenter(country);

        view.prefWidthProperty().bind(this.widthProperty());
        view.prefHeightProperty().bind(this.heightProperty());

        this.getChildren().add(view);

        initContextMenu();
    }

    public MapNode() {
        init();
    }

    public MapNode(Widget widget) {
        this.widget = widget;
        init();
    }

    private void initContextMenu() {
        MenuItem addItem = new MenuItem("Add Marker Here");
        ImageView add = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/markeradd.png"))));
        add.setFitWidth(16);
        add.setFitHeight(16);

        addItem.setGraphic(add);
        addMenu.getItems().add(addItem);

        MenuItem editItem = new MenuItem("Edit Marker");
        ImageView edt = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/markeredit.png"))));
        edt.setFitWidth(16);
        edt.setFitHeight(16);
        editItem.setGraphic(edt);

        MenuItem delItem = new MenuItem("Delete Marker");

        ImageView del = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/markerdelete.png"))));
        del.setFitWidth(16);
        del.setFitHeight(16);
        delItem.setGraphic(del);
        editMenu.getItems().add(editItem);
        editMenu.getItems().add(delItem);

    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        if (editMode) {
            setupEditModeContextMenu();
        } else {
            view.setOnContextMenuRequested(null);
        }
    }

    public void setOnAddMarker(BiConsumer<Double, Double> callback) {
        this.onAddMarker = callback;
    }

    public void setOnDeleteMarker(IntConsumer callback) {
        this.onDeleteMarker = callback;
    }

    public void setOnEditMarker(IntConsumer callback){
        this.onEditMarker = callback;
    }

    private void setupEditModeContextMenu() {
        view.setOnContextMenuRequested(e -> {

            if (!editMode || onAddMarker == null) {
                return;
            }
            e.consume();
            MapPoint point = view.getMapPosition(e.getX(), e.getY());
            if (point == null) {
                return;
            }

            if (addMenu.isShowing()) {
                addMenu.hide();
            }
            if (editMenu.isShowing()) {
                editMenu.hide();
            }

            MenuItem addItem = addMenu.getItems().getFirst();

            addItem.setOnAction(ev
                    -> onAddMarker.accept(point.getLatitude(), point.getLongitude())
            );

            addMenu.show(view, e.getScreenX(), e.getScreenY());
        });
    }

    public void setMarkers(List<MarkerData> markers) {
        markerLayer.clearChildren();
        for (int i = 0; i < markers.size(); i++) {
            addMarker(markers.get(i), i);
        }
    }

    private void addMarker(MarkerData point, int index) {
        MapMarker marker = new MapMarker(point.getIconPath());
        marker.setDisplay(point.getDisplayPath());
        marker.setUserData(index);

        boolean[] dragged = { false };

        marker.setOnMousePressed(e -> dragged[0] = false);

        marker.setOnMouseClicked(e -> {

            if (e.getButton() == MouseButton.PRIMARY) {
                if(editMode) return;
                if (e.getClickCount() == 2) {
                    marker.openDisplay(this.widget);
                } else {
                    Platform.runLater(() -> {
                        view.setCenter(point.getPoint());
                        if (view.getZoom() >= maxZoom) {
                            view.setZoom(maxZoom - 0.0001);
                        }
                        view.setZoom(maxZoom);
                        markerLayer.markdirty();
                    });
                }

                e.consume();
            }
        });


        marker.setOnMouseDragged((MouseEvent e) ->{
            if(!editMode) return;
            if(e.getButton() != MouseButton.PRIMARY) return;
            e.consume();
            Point2D viewCoords = view.sceneToLocal(e.getSceneX(), e.getSceneY());
            MapPoint newPoint = view.getMapPosition(viewCoords.getX(), viewCoords.getY());
            if(newPoint != null){
                markerLayer.updatePosition(marker, newPoint);
                dragged[0] = true;
            }
        });

        marker.setOnMouseReleased(e -> {
            if (!editMode) return;
            if(e.getButton() != MouseButton.PRIMARY) return;
            if(!dragged[0]) return;
            Point2D viewCoords = view.sceneToLocal(e.getSceneX(), e.getSceneY());
            MapPoint finalPoint = view.getMapPosition(viewCoords.getX(), viewCoords.getY());
            if (finalPoint != null){
                int idx = (int) marker.getUserData();
                onMoveMarker.accept(idx, new MarkerData(finalPoint.getLatitude(), finalPoint.getLongitude()));
            }
        });


        marker.setOnContextMenuRequested(e -> {
            if (!editMode || onDeleteMarker == null || onEditMarker == null) {
                return;
            }
            e.consume();
            int idx = (int) marker.getUserData();

            if (addMenu.isShowing()) {
                addMenu.hide();
            }
            if (editMenu.isShowing()) {
                editMenu.hide();
            }

            MenuItem editItem = editMenu.getItems().get(0);
            editItem.setOnAction(eh -> onEditMarker.accept(idx));

            MenuItem delItem = editMenu.getItems().get(1);
            delItem.setOnAction(eh -> onDeleteMarker.accept(idx));

            editMenu.show(marker, e.getScreenX(), e.getScreenY());

        });

        Tooltip data = new Tooltip(point.getName() + ": " + point.getPoint().getLatitude() + ", " + point.getPoint().getLongitude());
        Tooltip.install(marker, data);

        markerLayer.addPoint(point.getPoint(), marker);
    }

    public void setOnMoveMarker(BiConsumer<Integer, MarkerData> callback){
        this.onMoveMarker = callback;
    }

    public void setHost(String url){
        LocalTileRetriever.setHost(url);
    }

    public String getHost(){
        return LocalTileRetriever.getHost();
    }

    public void dispose() {
        if (addMenu.isShowing()) {
            addMenu.hide();
        }
        if (editMenu.isShowing()) {
            editMenu.hide();
        }

        view.prefWidthProperty().unbind();
        view.prefHeightProperty().unbind();

        markerLayer.clearChildren();

        view.setOnContextMenuRequested(null);

        onAddMarker = null;
        onDeleteMarker = null;
        onEditMarker = null;
        widget = null;
        
        markerLayer.cleanup();
    }
}
