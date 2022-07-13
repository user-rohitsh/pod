package microservice.Persitence;

public class MmapPersistence implements Persistence{
    @Override
    public Byte[] read(String key) {
        return new Byte[0];
    }

    @Override
    public void write(String key, Byte[] bytes) {

    }
}
