#include "../m_mapped_circular_buffer.h"
#include <chrono>
#include <cstring>
#include <ostream>
#include <thread>
#include <unistd.h>

int main(int argc, char **argv) {

  m_mapped_circular_buffer buffer(
      "/workspace/pod/queue");

  for (int i = 0; i < 10; i++) {
    std::this_thread::sleep_for(std::chrono::milliseconds(1000));
    buffer.write((unsigned char *)"abcdef", 6);
  }
}
