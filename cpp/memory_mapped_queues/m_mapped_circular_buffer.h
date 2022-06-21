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
public:
  enum ENDIAN { BIG, SMALL };
  static const uint MAX_CAPACITY = 1024;
  using byte_ptr = unsigned char *;

  static const uint SIZE_UINT = sizeof(uint);
  static const uint SIZE_OFFSET = 0;
  static const uint TAIL_OFFSET = SIZE_UINT;
  static const uint FRONT_OFFSET = SIZE_UINT * 2;
  static const uint DATA_OFFSET = SIZE_UINT * 3;

private:
  std::string file_name;
  byte_ptr start_addr = 0;
  int fd;
  void write(uint offset, byte_ptr buffer, uint len);
  void read(uint offset, byte_ptr buffer, uint len);
  int lock();
  int unlock();

public:
  m_mapped_circular_buffer(std::string file_name)
      : file_name(std::move(file_name)), start_addr(0) {

    map_file();
  }

  void map_file();

  uint write(byte_ptr buffer, uint len);
  uint read(byte_ptr buffer, uint len);
};