#include "m_mapped_circular_buffer.h"

void m_mapped_circular_buffer::map_file() {
  std::cout << "Map_file=" << std::endl;

  int fd = open(file_name.c_str(), O_RDWR | O_CREAT, S_IRWXU);
  if (fd == -1) {
    std::cout << "open error " << std::endl;
    handle_error("open");
  }

  int ret = ftruncate(fd, MAX_CAPACITY);
  if (ret == -1)
    handle_error("error truncating file");

  std::cout << "file open=" << std::endl;

  // long page_alligned_size = size & ~(sysconf(_SC_PAGE_SIZE) - 1);
  start_addr = (unsigned char *)mmap(NULL, MAX_CAPACITY, PROT_WRITE | PROT_READ,
                                     MAP_SHARED, fd, 0);
  std::cout << "fmmap done=" << (void *)start_addr << std::endl;
  if (start_addr == MAP_FAILED) {
    std::cout << "open error " << std::endl;
    handle_error("mmap");
  }
}

uint m_mapped_circular_buffer::write(unsigned char *buffer, uint len) {

  uint front;
  uint tail;
  uint size;

  readUint(0, front);
  readUint(sizeof(uint), tail);
  readUint(sizeof(uint), size);

  if ((front - tail) % size >= size)
    return 0;

  write(front + sizeof(uint) * 2, buffer, len);

  writeUint(0, front + len);

  return len;
}

void m_mapped_circular_buffer::write(uint offset, unsigned char *buffer,
                                     uint len) {
  memcpy(start_addr + offset, buffer, len);
}

void m_mapped_circular_buffer::readUint(uint offset, uint &i) {
  memcpy(&i, start_addr + offset, sizeof(i));
}

void m_mapped_circular_buffer::writeUint(uint offset, const uint i) {

  memcpy(start_addr + offset, &i, sizeof(i));
}
