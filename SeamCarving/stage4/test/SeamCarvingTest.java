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
                        .addArguments("-in", "small.png", "-out", "small-seam.png")
                        .setAttach(new OutFile("small-seam.png", "0c60a2f38c84f23964815b2952f899d1")),

                new TestCase<OutFile>()
                        .addArguments("-in", "blue.png", "-out", "blue-seam.png")
                        .setAttach(new OutFile("blue-seam.png", "3eb404d87b40c124fe0267deb1bcdbe5")),

                new TestCase<OutFile>()
                        .addArguments("-in", "trees.png", "-out", "trees-seam.png")
                        .setAttach(new OutFile("trees-seam.png", "41f89fb6effa1e7a9901b63cfe72958e"))
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
