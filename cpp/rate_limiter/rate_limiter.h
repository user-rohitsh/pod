#include <chrono>
#include <climits>
#include <iostream>
#include <vector>

template <int N, typename RESOLUTION = std::chrono::seconds>
class rate_limiter {
private:
  using clock = std::chrono::high_resolution_clock;

  std::vector<typename RESOLUTION::rep> buffer;
  RESOLUTION one_unit;
  int head = 0;

public:
  rate_limiter<N, RESOLUTION>() : buffer(N, 0) {}

  bool checkForLimitBreach() {
    std::chrono::time_point<clock, RESOLUTION> now = clock::now();

    if ((now - buffer[head]) > one_unit)
  }
};