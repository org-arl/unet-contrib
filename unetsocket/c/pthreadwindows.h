#ifndef PTHREAD_WINDOWS_H
#define PTHREAD_WINDOWS_H

#ifdef _WIN32

#include <windows.h>
#include <process.h>
#include <errno.h>

typedef struct pthread_tag {
  HANDLE handle;
} pthread_t;

typedef struct pthread_mutex_tag {
  HANDLE handle;
} pthread_mutex_t;

typedef struct pthread_attr_tag {
  int attr;
} pthread_attr_t;

typedef struct pthread_mutexattr_tag {
  int attr;
} pthread_mutexattr_t;

typedef DWORD pthread_key_t;

int pthread_create(pthread_t *thread, const pthread_attr_t *attr, void
		   *(*start_routine)(void *), void *arg);

void pthread_exit(void *value_ptr);

int pthread_join(pthread_t thread, void **value_ptr);

int pthread_mutex_destroy(pthread_mutex_t *mutex);

int pthread_mutex_init(pthread_mutex_t *mutex, const pthread_mutexattr_t *attr);

int pthread_mutex_lock(pthread_mutex_t *mutex);

int pthread_mutex_trylock(pthread_mutex_t *mutex);

int pthread_mutex_unlock(pthread_mutex_t *mutex);

#else

#include <pthread.h>
#include <unistd.h>
#define Sleep(num) usleep(num*1000)

#endif
#endif /* PTHREAD_WINDOWS_H */
