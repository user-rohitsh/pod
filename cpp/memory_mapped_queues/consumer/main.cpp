#include "../m_mapped_circular_buffer.h"
#include <chrono>
#include <cstring>
#include <ostream>
#include <ratio>
#include <thread>
#include <unistd.h>

int main(int argc, char **argv) {

  m_mapped_circular_buffer buffer(
      "/workspace/pod/queue");

  char str[100];
  memset(str, 0, 100);

  int count =0;

  while (count < 10) {
    std::this_thread::sleep_for(std::chrono::milliseconds(20));
    uint read = buffer.read((m_mapped_circular_buffer::byte_ptr)str, 6);
    if ( read > 0) count++;
    std::cout << "read = " << str << "len" << read << std::endl;
    memset(str, 0, 100);
  }
}
