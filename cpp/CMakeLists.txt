cmake_minimum_required(VERSION 3.0.0)
project(memmory_mapped_buffer VERSION 0.1.0)

include(CTest)
enable_testing()

add_executable(consumer memory_mapped_queues/m_mapped_circular_buffer.cpp memory_mapped_queues/consumer/main.cpp)
add_executable(producer memory_mapped_queues/m_mapped_circular_buffer.cpp memory_mapped_queues/producer/main.cpp)
add_executable(file_iterator file_iterator/file_iterator.cpp )

set(CPACK_PROJECT_NAME ${PROJECT_NAME})
set(CPACK_PROJECT_VERSION ${PROJECT_VERSION})
include(CPack)
