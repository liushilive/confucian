package confucian.shutterbug;

import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

import confucian.exception.FrameworkException;
import confucian.shutterbug.utils.Coordinates;
import confucian.shutterbug.utils.ImageProcessor;

public class ElementSnapshot extends Snapshot {

    ElementSnapshot() {
    }

    void setImage(BufferedImage image, Coordinates coord) {
        try {
            self().image = ImageProcessor.getElement(image, coord);
        } catch (RasterFormatException rfe) {
            throw new FrameworkException(ELEMENT_OUT_OF_VIEWPORT_EX_MESSAGE, rfe);
        }
    }

    @Override
    protected ElementSnapshot self() {
        return this;
    }
}
