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
                        .addArguments("-in", "small.png", "-out", "small-energy.png")
                        .setAttach(new OutFile("small-energy.png", "f67dee140f6ac86e2289a5c91cda377e")),

                new TestCase<OutFile>()
                        .addArguments("-in", "blue.png", "-out", "blue-energy.png")
                        .setAttach(new OutFile("blue-energy.png", "c0653a6283515bf9791b4e5544dad3b1")),

                new TestCase<OutFile>()
                        .addArguments("-in", "trees.png", "-out", "trees-energy.png")
                        .setAttach(new OutFile("trees-energy.png", "4eb912d893aab6b80979d533277f40b7"))
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
