#include <fcntl.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

inline void handle_error(std::string error) {
  perror(error.c_str());
  exit(EXIT_FAILURE);
}

class m_mapped_circular_buffer {
private:
  uint front;
  uint tail;
  uint size;
  std::string file_name;
  unsigned char *start_addr;

public:
  m_mapped_circular_buffer(uint size, std::string file_name)
      : front(0), tail(0), size(size), file_name(std::move(file_name)),
        start_addr(0) {
    map_file();
  }

  void map_file() ;

  uint write(unsigned char *buffer, uint len);

  void readUint(uint &i, uint offset);

  void writeUint(uint i, uint offset);
};