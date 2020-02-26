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
                        .addArguments("-in", "small.png", "-out", "small-energy.png")
                        .setAttach(new OutFile("small-energy.png", 15, 10, "f67dee140f6ac86e2289a5c91cda377e")),

                new TestCase<OutFile>()
                        .addArguments("-in", "blue.png", "-out", "blue-energy.png")
                        .setAttach(new OutFile("blue-energy.png", 500, 334, "c0653a6283515bf9791b4e5544dad3b1")),

                new TestCase<OutFile>()
                        .addArguments("-in", "trees.png", "-out", "trees-energy.png")
                        .setAttach(new OutFile("trees-energy.png", 600, 429, "4eb912d893aab6b80979d533277f40b7"))
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
