package me.toddydev.core.api.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class QRCodeRenderer extends MapRenderer {

    private BufferedImage image;
    private boolean done = false;

    public QRCodeRenderer(String data) {
        load(data);
    }

    private void load(String data) {
        try {
            var url = new URL(
                    String.format("https://chart.googleapis.com/chart?chs=128x128&cht=qr&chl=%s&choe=UTF-8", data)
            );

            image = ImageIO.read(url);
            image = MapPalette.resizeImage(image);
            System.out.println("Loaded QRCode image from " + url.toString());
        } catch (IOException e) {
            Logger.getLogger("QRCodeRenderer").severe("Failed to load QRCode image! You probably don't have a stable internet connection.");
        }
    }

    @Override
    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
        // This method is called every tick.
        if (done) return;

        mapCanvas.drawImage(0, 0, image);
        done = true;

        System.out.println("Image done rendering!");
    }
}
