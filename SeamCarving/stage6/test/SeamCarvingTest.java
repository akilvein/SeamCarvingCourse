import org.apache.commons.codec.binary.Hex;
import org.hyperskill.hstest.v6.testcase.TestCase;
import org.hyperskill.hstest.v6.stage.BaseStageTest;
import org.hyperskill.hstest.v6.testcase.CheckResult;
import seamcarving.MainKt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

class CheckFailException extends Exception {
    public CheckFailException(String s) {
        super(s);
    }
}

class OutFile {
    String hash;
    String filename;
    int width;
    int height;

    OutFile(String filename, int width, int height, String hash) {
        this.filename = filename;
        this.width = width;
        this.height = height;
        this.hash = hash;
    }

    public boolean compareWithActualMD5() throws CheckFailException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(new FileInputStream(filename).readAllBytes());
            byte[] digest = md.digest();
            if (!Hex.encodeHexString(digest).equalsIgnoreCase(hash)) {
                throw new CheckFailException("Hash sum of your image does not match expected value");
            }
        } catch (IOException e) {
            throw new CheckFailException(
                    String.format(
                            "Could not read output file '%s'. Please check you produce output file",
                            filename));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new CheckFailException("Internal test error. please report to Hyperskill team");
        }

        return true;
    }

    public boolean compareActualDimensions() throws CheckFailException {
        try {
            BufferedImage image = ImageIO.read(new File(filename));

            if (image == null) {
                throw new CheckFailException(
                        String.format(
                                "File format error. Looks like your output '%s' is not a valid image file.",
                                filename));
            }

            if (image.getWidth() != width) {
                throw new CheckFailException(
                        String.format(
                                "Dimensions mismatch. Output image width: %d; expected width: %d",
                                image.getWidth(), width));
            }

            if (image.getHeight() != height) {
                throw new CheckFailException(
                        String.format(
                                "Dimensions mismatch. Output image height: %d; expected height: %d",
                                image.getHeight(), height));
            }

        } catch (IOException e) {
            throw new CheckFailException(
                    String.format(
                            "Could not read output file '%s'. Please check you produce output file",
                            filename));
        }

        return true;
    }
}

public class SeamCarvingTest extends BaseStageTest<OutFile> {

    public SeamCarvingTest() {
        super(MainKt.class);
    }

    @Override
    public List<TestCase<OutFile>> generate() {

        return List.of(
                new TestCase<OutFile>()
                        .addArguments("-in", "small.png", "-out", "small-reduced.png", "-width", "1", "-height", "1")
                        .setAttach(new OutFile("small-reduced.png", 14, 9, "c58a0cca79215ea9253a92e31144afee")),

                new TestCase<OutFile>()
                        .addArguments("-in", "blue.png", "-out", "blue-reduced.png", "-width", "125", "-height", "50")
                        .setAttach(new OutFile("blue-reduced.png", 375, 284, "e17afe69c7e11e8b0e39638bb8a1fd7f")),

                new TestCase<OutFile>()
                        .addArguments("-in", "trees.png", "-out", "trees-reduced.png", "-width", "100", "-height", "30")
                        .setAttach(new OutFile("trees-reduced.png", 500, 399, "19d5f72aa67dbdd8d965a0407a5c914f"))
        );
    }

    @Override
    public CheckResult check(String reply, OutFile expectedFile) {
        try {
            expectedFile.compareActualDimensions();
            expectedFile.compareWithActualMD5();
        } catch (CheckFailException e) {
            return CheckResult.FALSE(e.getMessage());
        }

        return CheckResult.TRUE;
    }
}
