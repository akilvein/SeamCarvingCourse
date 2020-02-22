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
                        .addArguments("-in", "small.png", "-out", "small-reduced.png", "-width", "1", "-height", "1")
                        .setAttach(new OutFile("small-reduced.png", "c58a0cca79215ea9253a92e31144afee")),

                new TestCase<OutFile>()
                        .addArguments("-in", "blue.png", "-out", "blue-reduced.png", "-width", "125", "-height", "50")
                        .setAttach(new OutFile("blue-reduced.png", "e17afe69c7e11e8b0e39638bb8a1fd7f")),

                new TestCase<OutFile>()
                        .addArguments("-in", "trees.png", "-out", "trees-reduced.png", "-width", "100", "-height", "30")
                        .setAttach(new OutFile("trees-reduced.png", "19d5f72aa67dbdd8d965a0407a5c914f"))
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
