#include <fcntl.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>

class m_mapped_circular_buffer {
private:
  long front;
  long tail;
  long size;
  std::string file_name;
  void *start_addr;

public:
  m_mapped_circular_buffer(long size, std::string file_name)
      : front(0), tail(0), size(size), file_name(std::move(file_name)),
        start_addr(0) {}

  void map_file() {
    int fd = open(file_name.c_str(), O_RDWR | O_CREAT, S_IRWXU);
    if (fd == -1) {
      perror("open");
      exit(EXIT_FAILURE);
    }

    long page_alligned_size = size & ~(sysconf(_SC_PAGE_SIZE) - 1);
    start_addr = mmap(NULL, page_alligned_size,PROT_WRITE, MAP_SHARED,fd,pag)
  }


};