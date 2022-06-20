#include "m_mapped_circular_buffer.h"

void m_mapped_circular_buffer::map_file() {

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

uint m_mapped_circular_buffer::write(unsigned char *buffer, uint len) {

  uint front;
  uint tail;

  readUint(front, 0);
  readUint(tail, 4);

  if ((front - tail) % size >= size)
    return 0;

  for (uint i = 0; i < len; i++) {
    *(start_addr + front) = buffer[i];
  }

  writeUint(front + len, 0);

  return len;
}

void m_mapped_circular_buffer::readUint(uint &i, uint offset) {
  i = 0;
  i = (i << 8) + (*(start_addr + offset + 0) & 0xFF);
  i = (i << 8) + (*(start_addr + offset + 1) & 0xFF);
  i = (i << 8) + (*(start_addr + offset + 2) & 0xFF);
  i = (i << 8) + (*(start_addr + offset + 3) & 0xFF);
}

void m_mapped_circular_buffer::writeUint(uint i, uint offset) {
  memcpy(start_addr + offset, &offset, 4);
}
