#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFSIZE 1

char buf[BUFSIZE];

int main(int argc, char** argv)
{
  int src, dst, amount, size;

  if (argc!=4) {
    printf("Usage: test <src> <dst> <size>\n");
    return 1;
  }

  src = open(argv[1]);
  if (src==-1) {
    printf("Unable to open %s\n", argv[1]);
    return 1;
  }

  creat(argv[2]);
  dst = open(argv[2]);
  if (dst==-1) {
    printf("Unable to create %s\n", argv[2]);
    return 1;
  }

  size = argv[3];
  if (size<0) {
    printf("Invalid size\n");
    return 1;
  }

  while ((amount = read(src, buf, BUFSIZE))>0) {
    buf[0] = 1;
    write(dst, buf, amount);
  }

  close(src);
  close(dst);
  unlink(argv[1]);

  return 0;
}
