import org.apache.commons.codec.binary.Hex;
import org.hyperskill.hstest.v6.testcase.TestCase;
import org.hyperskill.hstest.v6.stage.BaseStageTest;
import org.hyperskill.hstest.v6.testcase.CheckResult;
import seamcarving.MainKt;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.List;


class OutFile {
    String hash;
    String filename;
    OutFile(String filename, String hash) {
        this.filename = filename;
        this.hash = hash;
    }

    public Boolean compareWithActualMD5() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(new FileInputStream(filename).readAllBytes());
            byte[] digest = md.digest();
            return Hex.encodeHexString(digest).equalsIgnoreCase(hash);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
                        .addArguments("-in", "small.png", "-out", "small-seam-hor.png")
                        .setAttach(new OutFile("small-seam-hor.png", "b4f1dd20ad157a74bfb21be9961954e4")),

                new TestCase<OutFile>()
                        .addArguments("-in", "blue.png", "-out", "blue-seam-hor.png")
                        .setAttach(new OutFile("blue-seam-hor.png", "c1383f5d285bd7c090294f4982607e1d")),

                new TestCase<OutFile>()
                        .addArguments("-in", "trees.png", "-out", "trees-seam-hor.png")
                        .setAttach(new OutFile("trees-seam-hor.png", "6367ef0c071daf4f33d79e9391bbbbc0"))
        );
    }

    @Override
    public CheckResult check(String reply, OutFile expectedFile) {
        if (expectedFile.compareWithActualMD5()) {
            return CheckResult.TRUE;
        }

        return CheckResult.FALSE;
    }
}
