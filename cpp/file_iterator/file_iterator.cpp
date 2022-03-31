#include "file_iterator.h"

using namespace std;
int main(int argc, char *argv[]) {

  cout << argv[1] << endl;
  MyFile file(argv[1]);

  MyFile::iterator it;

  for (it = file.begin(); it != file.end(); ++it) {
    cout << *it << endl;
  }
}