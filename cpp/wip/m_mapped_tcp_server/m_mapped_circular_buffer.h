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

private:
  std::string file_name;
  unsigned char *start_addr = 0;
  ENDIAN endian;

public:
  m_mapped_circular_buffer(std::string file_name)
      : file_name(std::move(file_name)), start_addr(0) {

    uint i = 1;

    if (*((char *)&i) == 1)
      endian = SMALL;
    else
      endian = BIG;

    map_file();
  }

  void map_file();

  uint write(unsigned char *buffer, uint len);
  void write(uint offset, unsigned char *buffer, uint len);

  void readUint(uint offset, uint &i);

  void writeUint(uint offset, const uint i);
};