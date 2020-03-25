#include "pthreadwindows.h"
#include <stdio.h>

#ifdef _WIN32

typedef void (*windows_thread)(void *);

int pthread_create(pthread_t *thread, const pthread_attr_t *attr, void *(*start_routine)(void *), void *arg) {
  uintptr_t handle = _beginthread((windows_thread)start_routine,0,arg);
  thread->handle = (HANDLE)handle;
  if(thread->handle == (HANDLE)-1) {
    return 1;
  } else {
  	return 0;
  }
}

void pthread_exit(void *value_ptr) {
	_endthread();
}

int pthread_join(pthread_t thread, void **value_ptr) {
  DWORD retvalue = WaitForSingleObject(thread.handle,INFINITE);
  if(retvalue == WAIT_OBJECT_0) {
    return 0;
  }else {
    return EINVAL;
  }
}

int pthread_mutex_destroy(pthread_mutex_t *mutex) {
  return !CloseHandle(mutex->handle);
}

int pthread_mutex_init(pthread_mutex_t *mutex, const pthread_mutexattr_t *attr) {
  HANDLE handle = CreateMutex(NULL,FALSE,NULL);
  if(handle != NULL){
  	mutex->handle = handle;
  	return 0;
  }else{
  	return 1;
  }
}

int pthread_mutex_lock(pthread_mutex_t *mutex) {
  DWORD retvalue = WaitForSingleObject(mutex->handle,INFINITE);
  if(retvalue == WAIT_OBJECT_0){
  	return 0;
  }else{
  	return EINVAL;
  }
}

int pthread_mutex_trylock(pthread_mutex_t *mutex) {
  DWORD retvalue = WaitForSingleObject(mutex->handle,0);
  if(retvalue == WAIT_OBJECT_0){
  	return 0;
  }else if(retvalue == WAIT_TIMEOUT){
  	return EBUSY;
  }else{
  	return EINVAL;
  }
}

int pthread_mutex_unlock(pthread_mutex_t *mutex) {
  return !ReleaseMutex(mutex->handle);
}

#endif
