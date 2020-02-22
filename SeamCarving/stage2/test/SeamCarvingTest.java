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
                        .addArguments("-in", "small.png", "-out", "small-negative.png")
                        .setAttach(new OutFile("small-negative.png", "f962e35e1d07761892ce84c790e69dce")),

                new TestCase<OutFile>()
                        .addArguments("-in", "blue.png", "-out", "blue-negative.png")
                        .setAttach(new OutFile("blue-negative.png", "1822db3d495f0f46d31e01b3df973a78")),

                new TestCase<OutFile>()
                        .addArguments("-in", "trees.png", "-out", "trees-negative.png")
                        .setAttach(new OutFile("trees-negative.png", "e9dd95534db7285dacbaaf3b31dc828c"))
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
