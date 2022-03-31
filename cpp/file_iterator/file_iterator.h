#include <fstream>
#include <ios>
#include <iostream>
#include <istream>
#include <iterator>
#include <limits>
#include <sstream>
#include <unistd.h>

using namespace std;

inline bool isValidNumber(stringstream &str, int &val) {
  int x;
  str >> ws;
  str >> x;
  str >> ws;
  if (str.eof()) {
    val = x;
    //cout << "validating " << x;
    return true;
  }

  return false;
}

class MyFile {
private:
  ifstream stream;

public:
  struct iterator : std::iterator<input_iterator_tag, string, ptrdiff_t,
                                  const string *, const string &> {
  private:
    istream *stream;
    int value;

  public:
    // using iterator_category = std::forward_iterator_tag;
    // using difference_type = std::ptrdiff_t;
    // using value_type = int;
    // using pointer = int *;   // or also value_type*
    // using reference = int &; // or also value_type&

    iterator() { stream = 0; };

    iterator(istream &c_stream) : stream(&c_stream) {
      *stream >> noskipws;
      ++*this;
    }

    iterator(const iterator &copyFrom) { this->stream = copyFrom.stream; }

    friend bool operator==(const iterator &it1, const iterator &it2) {
      if (it1.stream == 0 && it2.stream == 0)
        return true;

      if (it1.stream == 0 || it2.stream == 0)
        return false;

      return it1.stream->tellg() == it2.stream->tellg();
    }

    friend bool operator!=(const iterator &it1, const iterator &it2) {
      return !(it1 == it2);
    }

    iterator &operator++() {
      char ch;
      stringstream line;

      while ((*stream >> ch)) {
        //cout << "ch " <<ch;
        if (ch == '\n') {
          //cout << "candidate " << line.str()<<endl;
          bool succ = isValidNumber(line, value);
          line.str("");
          line.clear();
          if (succ)
            return *this;
        } else {
          line << ch;                
        }
      }     

      isValidNumber(line, value);
      stream = 0;
      return *this;
    }

    iterator operator++(int) {
      iterator temp = *this;
      ++*this;
      return temp;
    }

    int operator*() { return value; }
    int *operator->() { return &value; }
  };

  MyFile() = delete;

  MyFile(string path) : stream(path, ifstream::in) {}

  iterator begin() { return iterator(stream); }

  iterator end() { return iterator(); }
};
