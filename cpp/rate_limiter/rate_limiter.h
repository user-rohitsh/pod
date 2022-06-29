#include <chrono>
#include <climits>
#include <iostream>
#include <vector>

template <int N, typename RESOLUTION = std::chrono::microseconds>
class rate_limiter {
private:
  using ulong = unsigned long;
  using clock = std::chrono::high_resolution_clock;

  std::vector<long> buffer;
  int head=0;
  rate_limiter(const rate_limiter &) = delete;
  rate_limiter(rate_limiter &&) = delete;
  rate_limiter &operator=(const rate_limiter &) = delete;
  rate_limiter &operator=(rate_limiter &&) = delete;

public:
  rate_limiter<N, RESOLUTION>() : buffer(N, 0) {}

  bool checkForLimitBreach() {
    std::chrono::time_point<clock, RESOLUTION> now = clock::now();

    if ( std::chrono::duration_cast <RESOLUTION> (now - buffer[head]) > 0)
        
  }
};