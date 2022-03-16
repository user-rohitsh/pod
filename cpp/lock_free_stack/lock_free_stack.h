#include <functional>
#include <list>
#include <atomic>
#include <thread>
#include <vector>

template <typename T> class lock_free_stack {
private:
  struct Node {
    T data;
    Node *next;
    Node(T const &data_) { data(data_); }
  };

  std::atomic<Node*> head;

public:
  lock_free_stack() {
      head = nullptr;
  }

  void push(T const &data) {
    Node *new_node = new Node(data);
    new_node->next = head.load();
    while (!head.compare_exchange_weak(new_node->next, new_node))
      ;
  }

  void pop(T &result) {
    Node *node = head.load();
    while (!head.compare_exchange_weak(node, node->next))
      ;
    result = node->data;
  }
};