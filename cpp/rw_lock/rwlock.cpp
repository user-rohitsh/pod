#include <chrono>
#include <thread>
#include <condition_variable>
#include <iostream>
#include <mutex>
#include <sstream>
#include <stdlib.h>

using namespace std;

class rw_lock {

private:
  unique_lock<mutex> lk;
  mutex mtx;

public:
  static condition_variable no_one_reading;
  static int num_readers;
  rw_lock() = delete;
  rw_lock(mutex &mtx) : lk(mtx, defer_lock) {}

  void w_lock() {
    // cout <<"locking" <<endl;
    lk.lock();
    // cout << "locked" << endl;
    // cout << "num_readers" << num_readers << endl;
    while (num_readers > 0)
      no_one_reading.wait(lk);
  }

  void w_unlock() { lk.unlock(); }

  void r_lock() {
    lk.lock();
    ++num_readers;
    lk.unlock();
  }

  void r_unlock() {
    lk.lock();
    --num_readers;
    if (num_readers == 0)
      no_one_reading.notify_all();
    lk.unlock();
  }
};

int rw_lock::num_readers = 0;
condition_variable rw_lock::no_one_reading;
string str = "";
mutex mtx;

string outs[4];

void reader(int id) {
    outs[id] = outs[id] +  " Reader " + to_string(id)  + " str=" + str +"\n";
}

void catenate(int id, const string &s) {
  rw_lock lk(mtx);
  lk.w_lock();
  for (int i = 0; i < 10; i++) {
    str += s;
  }
  lk.w_unlock();
}

int main(int argc, char *argv[]) { 
  int no_readers = 4;
  int no_writers = 3;

  thread readers[no_readers];
  thread writers[no_writers];

  for (int i = 0; i < no_readers; i++) {
    readers[i] = thread(reader, i);
  }

  writers[0] = thread(catenate, 0, "a");
  writers[1] = thread(catenate, 1, "b");
  writers[2] = thread(catenate, 1, "d");

  for (auto &writer : writers)
    writer.join();

  for (auto &reader : readers)
    reader.join();

  for (auto &out : outs)
    cout << out;
}
