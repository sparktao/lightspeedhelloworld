package org.hexagonsi;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.util.service.TLcdServiceLoader;
import com.luciad.view.lightspeed.TLspFXView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collection;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("First Lightspeed application");
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        TLspFXView view = createView();
        borderPane.setCenter(view.getHostNode());


        // 添加顶层工具栏
        ToolBar toolBar = new ToolBar();
        borderPane.setTop(toolBar);
        RadioButton b2d = new RadioButton("2D");
        RadioButton b3d = new RadioButton("3D");

        toolBar.getItems().add(b2d);
        toolBar.getItems().add(b3d);

        //Place the buttons in a group.
        //This ensures that only one of them can be selected at the same time
        ToggleGroup group = new ToggleGroup();
        b2d.setToggleGroup(group);
        b3d.setToggleGroup(group);

        b2d.setSelected(true);//start with a 2D view

        ILcdModel shpModel = createSHPModel();
        view.addLayer(createLayer(shpModel));

        ILcdModel rasterModel = createRasterModel();
        view.addLayer(createLayer(rasterModel));

        view.addLayer(createGridLayer());
    }

    private TLspFXView createView() {
        return TLspViewBuilder.newBuilder().buildFXView();
    }


    private static ILspLayer createLayer(ILcdModel aModel) {
        try {
            TLspCompositeLayerFactory layerFactory =
                    new TLspCompositeLayerFactory(TLcdServiceLoader.getInstance(ILspLayerFactory.class));

            if (layerFactory.canCreateLayers(aModel)) {
                Collection<ILspLayer> layers = layerFactory.createLayers(aModel);
                //We only expect a single layer for our data
                return layers.iterator().next();
            }
        }catch (Exception ex) {
            System.out.println(ex);
        }
        throw new RuntimeException("Could not create a layer for " + aModel.getModelDescriptor().getDisplayName());
    }

    private ILcdModel createSHPModel() throws IOException {
        ILcdModelDecoder decoder =
                new TLcdCompositeModelDecoder(TLcdServiceLoader.getInstance(ILcdModelDecoder.class));

        ILcdModel shpModel = decoder.decode("Data/Shp/Usa/city_125.shp");
        return shpModel;
    }

    private ILcdModel createRasterModel() throws IOException {
        ILcdModelDecoder decoder =
                new TLcdCompositeModelDecoder(TLcdServiceLoader.getInstance(ILcdModelDecoder.class));
        ILcdModel geopackageModel = decoder.decode("Data/GeoPackage/bluemarble.gpkg");

        return geopackageModel;
    }

    ILspLayer createGridLayer() {
        return TLspLonLatGridLayerBuilder.newBuilder().build();
    }
}
