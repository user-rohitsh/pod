#include <condition_variable>
#include <mutex>
#include <thread>

using namespace std;

struct resource {
  mutex write_mtx;
  condition_variable num_readers_cond;
  mutex read_mtx;
};

class rw_lock {

private:
  resource res;
  static int num_readers;

public:
  void w_lock() {
    res.write_mtx.lock();
    if (num_readers != 0)
      res.num_readers_cond.wait()();
  }

  void w_unlock() { write_mtx.unlock(); }
};
