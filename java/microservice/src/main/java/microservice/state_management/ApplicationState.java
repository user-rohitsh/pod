package microservice.state_management;

public interface ApplicationState <V> {
    void save(V message);
    V retrieve();
    long position();
    void seek(long position);
    void rewind(long diff);
}
