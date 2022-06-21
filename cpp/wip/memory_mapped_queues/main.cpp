#include "m_mapped_circular_buffer.h"
#include <cstring>
#include <ostream>
#include <unistd.h>

int main(int argc, char **argv) {

  m_mapped_circular_buffer buffer("./queue");

  std::cout << "pid=" << getpid() << std::endl;

  char str[100];
  memset(str, 0, 100);

  uint read = buffer.read((m_mapped_circular_buffer::byte_ptr)str, 6);
  std::cout << "read 1= " << read << std::endl;
  buffer.write((unsigned char *)"abcdef", 6);
  buffer.write((unsigned char *)"abcdef", 6);

  read = buffer.read((m_mapped_circular_buffer::byte_ptr)str, 6);
  std::cout << "read 2 = " << read << std::endl;
}