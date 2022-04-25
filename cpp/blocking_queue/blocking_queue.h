#include <chrono>
#include <condition_variable>
#include <deque>
#include <iostream>
#include <mutex>
#include <queue>
#include <sstream>
#include <stdlib.h>

template <typename T, typename Container = std::deque<T>>
class blocking_queue : private std::queue<T, Container> {

private:
  std::mutex mtx;
  std::condition_variable not_empty;

public:
  using value_type = typename std::queue<T, Container>::value_type;
  using reference = typename std::queue<T, Container>::reference;
  using const_reference = typename std::queue<T, Container>::const_reference;
  using Base = std::queue<T, Container>;

  blocking_queue() : Base(){}
  blocking_queue(Container c) : Base(c) {}

  value_type pop() {    
    std::unique_lock<std::mutex>lk (mtx);    
    while (Base::size() == 0)
      not_empty.wait(lk);
    value_type val = Base::front();
    Base::pop();
    //std::cout << "Popped=" << val << std::endl;
    return val;
  }

  void push(std::string &val) {    
    std::unique_lock<std::mutex> lk(mtx);
    Base::push(val);
    //std::cout << "Pushed=" << val << std::endl;
    not_empty.notify_one();   
  }

  void push(value_type &&val) {
    std::unique_lock<std::mutex> lk(mtx);    
    Base::push(std::move(val));
    not_empty.notify_all();
  }
};