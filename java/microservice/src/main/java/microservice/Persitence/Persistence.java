package microservice.Persitence;

public interface Persistence {
    Byte[] read(String key);
    void write(String key, Byte[] bytes);
}
