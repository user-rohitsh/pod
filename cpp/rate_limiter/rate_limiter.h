#include <chrono>
#include <climits>
#include <iostream>
#include <vector>

class limiter
{
public:
  virtual bool checkForLimitBreach(int messageId) = 0;
};

template <int N, typename Duration = std::chrono::seconds>
class rate_limiter : public limiter
{
private:
  using Clock = std::chrono::high_resolution_clock;  
  using ClockDuration = Clock::duration;

  std::vector<ClockDuration> buffer;
  Duration one_unit;
  int head = 0;

public:
  rate_limiter<N, Duration>() : buffer(N, ClockDuration::zero()), one_unit(1) {}

  virtual bool checkForLimitBreach(int messageId)
  {
    ClockDuration now = Clock::now().time_since_epoch();
    auto one = std::chrono::duration_cast<ClockDuration>(one_unit) ;
    auto diff = now - buffer[head];

    if ((now - buffer[head]) <= std::chrono::duration_cast<ClockDuration>(one_unit) )
    {
      std::cout << "Rejected message " << messageId << " at " << now.count() << " Diff =" << (std::chrono::duration_cast<std::chrono::seconds>(diff)).count() << std::endl;
      return true;
    }

    std::cout << "Accepted message " << messageId << " at " << now.count() << " Diff =" << (std::chrono::duration_cast<std::chrono::seconds>(diff)).count() << std::endl;
    buffer[head] = now;
    head = (head + 1) % N;
    return false;
  }
};