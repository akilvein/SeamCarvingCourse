import org.apache.commons.codec.binary.Hex;
import org.hyperskill.hstest.v6.testcase.TestCase;
import org.hyperskill.hstest.v6.stage.BaseStageTest;
import org.hyperskill.hstest.v6.testcase.CheckResult;
import seamcarving.MainKt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
            File imgPath = new File(filename);
            BufferedImage bufferedImage = ImageIO.read(imgPath);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "bmp", baos);

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(baos.toByteArray());
            byte[] digest = md.digest();
            String actualHash = Hex.encodeHexString(digest);
            if (!actualHash.equalsIgnoreCase(hash)) {
                throw new CheckFailException(
                        String.format(
                                "Hash sum of your image (%s) does not match expected value",
                                actualHash));
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

        return Arrays.asList(
                new TestCase<OutFile>()
                        .addArguments("-in", "small.png", "-out", "small-seam.png")
                        .setAttach(new OutFile("small-seam.png", 15, 10, "831b2b94d6ca3ae71a8aacec4e5b5e23")),

                new TestCase<OutFile>()
                        .addArguments("-in", "blue.png", "-out", "blue-seam.png")
                        .setAttach(new OutFile("blue-seam.png", 500, 334, "bfde19c2d97092e104b14fd07f2cf5f3")),

                new TestCase<OutFile>()
                        .addArguments("-in", "trees.png", "-out", "trees-seam.png")
                        .setAttach(new OutFile("trees-seam.png", 600, 429, "9298d037e2defc97a1b35e8abddafb02"))
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
