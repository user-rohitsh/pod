#include "m_mapped_circular_buffer.h"
#include <cstdio>
#include <sys/mman.h>

void m_mapped_circular_buffer::map_file() {
  int fd = open(file_name.c_str(), O_RDWR | O_CREAT, S_IRWXU);
  if (fd == -1)
    handle_error("open");

  int ret = ftruncate(fd, MAX_CAPACITY);
  if (ret == -1)
    handle_error("error truncating file");

  ret = lseek(fd, 0, SEEK_SET);
  if (ret == -1)
    handle_error("error seeking to 0 in file");

  // long page_alligned_size = size & ~(sysconf(_SC_PAGE_SIZE) - 1);
  start_addr = (byte_ptr)mmap(NULL, MAX_CAPACITY, PROT_WRITE | PROT_READ,
                              MAP_SHARED | MAP_POPULATE, fd, 0);
  if (start_addr == MAP_FAILED)
    handle_error("mmap");

  close(fd);
}

uint m_mapped_circular_buffer::write(byte_ptr buffer, uint len) {

  uint front = 0;
  uint size = 0;

  read(FRONT_OFFSET, (byte_ptr)&front, SIZE_UINT);
  read(SIZE_OFFSET, (byte_ptr)&size, SIZE_UINT);

  if (size + len > MAX_CAPACITY)
    return 0;

  write(front + DATA_OFFSET, buffer, len);
  front += len;
  size += len;
  write(FRONT_OFFSET, (byte_ptr)&front, SIZE_UINT);
  write(SIZE_OFFSET, (byte_ptr)&size, SIZE_UINT);

  return len;
}

uint m_mapped_circular_buffer::read(byte_ptr buffer, uint len) {
  uint tail = 0;
  uint size = 0;

  read(TAIL_OFFSET, (byte_ptr)&tail, SIZE_UINT);
  read(SIZE_OFFSET, (byte_ptr)&size, SIZE_UINT);

  if (size <= 0)
    return 0;

  read(tail + DATA_OFFSET, buffer, len);
  tail -= len;
  size -= len;
  write(TAIL_OFFSET, (byte_ptr)&tail, SIZE_UINT);
  write(SIZE_OFFSET, (byte_ptr)&size, SIZE_UINT);

  return len;
}

void m_mapped_circular_buffer::write(uint offset, byte_ptr buffer, uint len) {
  memcpy(start_addr + offset, buffer, len);
}

void m_mapped_circular_buffer::read(uint offset, byte_ptr buffer, uint len) {
  memcpy(buffer, start_addr + offset, len);
}
