package droute.legacy;

import java.io.IOException;
import java.io.OutputStream;

public interface Streamable {

	void writeTo(OutputStream out) throws IOException;
	
}
