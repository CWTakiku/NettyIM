package com.takiku.im_lib.exception;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class RouteException extends RuntimeException {
  private static final Method addSuppressedExceptionMethod;

  static {
    Method m;
    try {
      m = Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class);
    } catch (Exception e) {
      m = null;
    }
    addSuppressedExceptionMethod = m;
  }

  private IOException lastException;

  public RouteException(IOException cause) {
    super(cause);
    lastException = cause;
  }

  public IOException getLastConnectException() {
    return lastException;
  }

  public void addConnectException(IOException e) {
    addSuppressedIfPossible(e, lastException);
    lastException = e;
  }

  private void addSuppressedIfPossible(IOException e, IOException suppressed) {
    if (addSuppressedExceptionMethod != null) {
      try {
        addSuppressedExceptionMethod.invoke(e, suppressed);
      } catch (InvocationTargetException | IllegalAccessException ignored) {
      }
    }
  }
}