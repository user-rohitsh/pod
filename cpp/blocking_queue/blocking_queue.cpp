#include "blocking_queue.h"
#include "thread"
#include <chrono>
#include <deque>
#include <functional>
using namespace std;

blocking_queue<string, deque<string>> q;

void reader(int id) {
  while (1)
    q.pop();
}

void writer(int id) {
  for (int i = 0; i < 10; i++) {
    string placeholder =to_string(i * id);
    q.push(placeholder);
  }
}

int main(int argc, char *argv[]) {
  int no_readers = 4;
  int no_writers = 3;

  thread readers[no_readers];
  thread writers[no_writers];

  for (int i = 0; i < no_readers; i++) {
    readers[i] = thread(reader, i);
  }

  for (int i = 0; i < no_writers; i++) {
    writers[i] = thread(writer, i);
  }

  for (auto &writer : writers)
    writer.join();

  for (auto &reader : readers) {
    reader.join();
  }

  while (1) {
    this_thread::sleep_for(chrono::milliseconds(3));
  }
}
