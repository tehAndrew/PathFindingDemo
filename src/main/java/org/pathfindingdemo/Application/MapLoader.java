package org.pathfindingdemo.Application;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.pathfindingdemo.AStar.CellType;
import org.pathfindingdemo.Helpers.Pair;

import java.io.*;


/* ###################
 * # Class MapLoader #
 * ###################
 * File structure of demomap files:
 * --------------------------------------------------------------------------------------------
 * byte | 1           | 2           | 3         | 4         | 5         | 6          | 7 - n
 * data | start pos x | start pos y | end pos x | end pos y | map width | map height | map data
 * --------------------------------------------------------------------------------------------
 */
public class MapLoader {
    private FileChooser fileChooser;

    public MapLoader() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("map file", "*.demomap")
        );
        fileChooser.setInitialDirectory(new File("Demo Maps"));
    }

    public MapData loadMap(Stage stage) {
        MapData mapData = null;
        CellType[][] map = null;
        int hMapWidth, hMapHeight, hStartPosX, hStartPosY, hEndPosX, hEndPosY;
        hMapWidth = hMapHeight = hStartPosX = hStartPosY = hEndPosX = hEndPosY = 0;

        fileChooser.setTitle("Open Map");
        File mapFile = fileChooser.showOpenDialog(stage);
        if (mapFile == null)
            return null;

        try (InputStream in = new FileInputStream(mapFile)) {
            hMapWidth = in.read();
            hMapHeight = in.read();
            hStartPosX = in.read();
            hStartPosY = in.read();
            hEndPosX = in.read();
            hEndPosY = in.read();

            int cellType;
            int x, y;
            x = y = 0;

            map = new CellType[hMapWidth][hMapHeight];
            while ((cellType = in.read()) != -1) {
                map[x][y++] = CellType.values()[cellType]; // Expensive but we can afford it.
                if (y >= hMapHeight) {
                    y = 0;
                    x++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new MapData(
                new Pair(hStartPosX, hStartPosY),
                new Pair(hEndPosX, hEndPosY),
                hMapWidth,
                hMapHeight,
                map
        );
    }

    public void saveMap(Stage stage, MapData mapData) {
        fileChooser.setTitle("Open Map");
        File mapFile = fileChooser.showSaveDialog(stage);
        if (mapFile == null)
            return;

        try (OutputStream out = new FileOutputStream(mapFile)) {
            out.write(mapData.getMapWidth());
            out.write(mapData.getMapHeight());
            out.write(mapData.getStartPos().getX());
            out.write(mapData.getStartPos().getY());
            out.write(mapData.getEndPos().getX());
            out.write(mapData.getEndPos().getY());

            for (int x = 0; x < mapData.getMapWidth(); x++) {
                for (int y = 0; y < mapData.getMapHeight(); y++) {
                    out.write(mapData.getMap()[x][y].ordinal());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
