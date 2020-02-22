import org.apache.commons.codec.binary.Hex;
import org.hyperskill.hstest.v6.stage.BaseStageTest;
import org.hyperskill.hstest.v6.testcase.CheckResult;
import org.hyperskill.hstest.v6.testcase.TestCase;
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
                        .setInput("20\n10\nout1.png\n")
                        .setAttach(new OutFile("out1.png", "50c985b7445d6ef0a318b1827bc852f9")),

                new TestCase<OutFile>()
                        .setInput("10\n10\nout2.png\n")
                        .setAttach(new OutFile("out2.png", "940725b08eaa293c6f614310575290d6")),

                new TestCase<OutFile>()
                        .setInput("20\n20\nout3.png\n")
                        .setAttach(new OutFile("out3.png", "55e4b0ee838ff97e43570124bbd05d5d"))
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
