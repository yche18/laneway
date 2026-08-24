package com.laneway.utils;

public interface ILock {

  /**
   * Attempt to acquire the lock.
   *
   * @param timeoutSec lock lifetime in seconds before automatic expiration
   * @return true when the lock is acquired; false otherwise
   */
  boolean tryLock(long timeoutSec);

  /** Release the lock. */
  void unlock();
}
