#include "m_mapped_circular_buffer.h"
#include <ostream>
#include <unistd.h>

int main(int argc, char **argv) {

  m_mapped_circular_buffer buffer("test");

  std::cout << "pid=" << getpid() << std::endl;

  uint i = 5;

  buffer.write((unsigned char *)"abcdef", 6);
  buffer.write((unsigned char *)"abcdef", 6);
}