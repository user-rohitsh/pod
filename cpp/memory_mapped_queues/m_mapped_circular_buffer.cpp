#include "m_mapped_circular_buffer.h"
#include <chrono>
#include <cstdio>
#include <fcntl.h>
#include <ratio>
#include <sys/mman.h>
#include <thread>

void m_mapped_circular_buffer::map_file() {
  fd = open(file_name.c_str(), O_RDWR | O_CREAT, S_IRWXU);
  if (fd == -1)
    handle_error("open");

  int ret = ftruncate(fd, MAX_CAPACITY);
  if (ret == -1)
    handle_error("error truncating file");

  // long page_alligned_size = size & ~(sysconf(_SC_PAGE_SIZE) - 1);
  start_addr = (byte_ptr)mmap(NULL, MAX_CAPACITY, PROT_WRITE | PROT_READ,
                              MAP_SHARED, fd, 0);
  if (start_addr == MAP_FAILED)
    handle_error("mmap");

  ret = lseek(fd, SIZE_OFFSET, SEEK_SET);
  if (ret == -1)
    handle_error("error seeling in file");

  // close(fd);
}

uint m_mapped_circular_buffer::write(byte_ptr buffer, uint len) {

  uint front = 0;
  uint size = 0;

  read(FRONT_OFFSET, (byte_ptr)&front, SIZE_UINT);

  lock();
  read(SIZE_OFFSET, (byte_ptr)&size, SIZE_UINT);
  unlock();

  if (size + len > MAX_CAPACITY)
    return 0;

  write(front + DATA_OFFSET, buffer, len);
  front = (front + len) % MAX_CAPACITY;
  size += len;
  write(FRONT_OFFSET, (byte_ptr)&front, SIZE_UINT);
  
  lock();
  write(SIZE_OFFSET, (byte_ptr)&size, SIZE_UINT);
  unlock();

  return len;
}

uint m_mapped_circular_buffer::read(byte_ptr buffer, uint len) {
  uint tail = 0;
  uint size = 0;

  read(TAIL_OFFSET, (byte_ptr)&tail, SIZE_UINT);

  lock();
  read(SIZE_OFFSET, (byte_ptr)&size, SIZE_UINT);
  unlock();

  if (size < len)
    return 0;

  read(tail + DATA_OFFSET, buffer, len);
  tail = (tail + len) % MAX_CAPACITY;
  size -= len;
  write(TAIL_OFFSET, (byte_ptr)&tail, SIZE_UINT);

  lock();
  write(SIZE_OFFSET, (byte_ptr)&size, SIZE_UINT);
  unlock();

  return len;
}

void m_mapped_circular_buffer::write(uint offset, byte_ptr buffer, uint len) {
  memcpy(start_addr + offset, buffer, len);
}

void m_mapped_circular_buffer::read(uint offset, byte_ptr buffer, uint len) {
  memcpy(buffer, start_addr + offset, len);
}

int m_mapped_circular_buffer::lock() {
  int ret = lockf(fd, F_TLOCK, SIZE_UINT);
  int counter = 0;
  while (ret == -1) {
    counter++;
    if (errno != EAGAIN && errno != EACCES) {
      handle_error("error in locking");
    }

    if (counter == 1) {
      std::this_thread::sleep_for(std::chrono::microseconds(100)); // try one more time to get lock
      ret = lockf(fd, F_TLOCK, SIZE_UINT);
    } else {
      ret = lockf(fd, F_LOCK, SIZE_UINT); // blocking now - should happen very rarely
    }
  }
  return ret;
}

int m_mapped_circular_buffer::unlock() { return lockf(fd, F_ULOCK, SIZE_UINT); }
