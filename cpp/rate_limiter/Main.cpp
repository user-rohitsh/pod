#include "rate_limiter.h"
#include <thread>

using namespace std::chrono;

template <typename unit = std::chrono::seconds>
void generateOrdersAtRate(int rate, int howMany, limiter &limiter)
{
    auto delay = duration<double,typename unit::period>(1.0/rate) ;

    int id=0;

    for (int i = 0; i < howMany; i++)
    {
        id++;
        limiter.checkForLimitBreach(id);
        std::this_thread::sleep_for(delay);
    }
}

int main()
{
    limiter *lmtr = new rate_limiter<10,minutes>();
    generateOrdersAtRate(5, 20, *lmtr);
    //std::this_thread::sleep_for(milliseconds(1000));
    lmtr = new rate_limiter<10,minutes>();
    generateOrdersAtRate<minutes>(5, 20, *lmtr);
}

