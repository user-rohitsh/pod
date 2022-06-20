#include <fcntl.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
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

  void map_file() {

    int fd = open(file_name.c_str(), O_RDWR | O_CREAT, S_IRWXU);
    if (fd == -1) {
      handle_error("open");
    }

    // long page_alligned_size = size & ~(sysconf(_SC_PAGE_SIZE) - 1);
    start_addr = (unsigned char *)mmap(NULL, size, PROT_WRITE | PROT_READ,
                                       MAP_SHARED, fd, 0);
    if (start_addr == MAP_FAILED)
      handle_error("mmap");
  }

  uint write(unsigned char *buffer, uint len) {

    uint front;
    uint tail;

    readUnsignedInt(front, 0);
    readUnsignedInt(tail, 4);

    if ( ( front - tail ) % size >= size  ) return 0;

    for (uint i = 0; i < len; i++) {
      *(start_addr + front) = buffer[i];
      front++;
    }

    return len;
  }

  void readUnsignedInt(uint &i, uint offset) {
    i = 0;
    i = (i << 8) + (*(start_addr + offset + 0) & 0xFF);
    i = (i << 8) + (*(start_addr + offset + 1) & 0xFF);
    i = (i << 8) + (*(start_addr + offset + 2) & 0xFF);
    i = (i << 8) + (*(start_addr + offset + 3) & 0xFF);    
  }
};