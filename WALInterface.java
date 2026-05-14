
import java.io.IOException;
import java.util.List;

interface WALInterface {
    boolean writeIntoWAL(String operation);
    List<String> replay() throws IOException;
}
