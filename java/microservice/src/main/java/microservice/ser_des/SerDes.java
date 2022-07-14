package microservice.ser_des;

import java.nio.ByteBuffer;

public interface SerDes<M> {
    ByteBuffer toBytes(M message);
    M fromBytes(ByteBuffer bytes);
}
